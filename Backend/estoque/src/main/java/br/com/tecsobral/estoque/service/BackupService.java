package br.com.tecsobral.estoque.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.tecsobral.estoque.dto.ServiceDtos.BackupResponse;
import br.com.tecsobral.estoque.entity.Backup;
import br.com.tecsobral.estoque.exception.RegraNegocioException;
import br.com.tecsobral.estoque.exception.RecursoNaoEncontrado;
import br.com.tecsobral.estoque.model.enums.StatusBackup;
import br.com.tecsobral.estoque.repository.BackupRepository;

@Service
public class BackupService {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private final BackupRepository backups;
    private final LogAlteracaoService auditoria;
    private final Path diretorio;
    private final String dbUrl, dbUsuario, dbSenha, pgDump, pgRestore;

    public BackupService(BackupRepository backups, LogAlteracaoService auditoria,
            @Value("${app.backup.directory:./backups}") String diretorio,
            @Value("${DB_URL:}") String dbUrl,
            @Value("${DB_USERNAME:}") String dbUsuario,
            @Value("${DB_PASSWORD:}") String dbSenha,
            @Value("${app.backup.pg-dump:pg_dump}") String pgDump,
            @Value("${app.backup.pg-restore:pg_restore}") String pgRestore) {
        this.backups=backups; this.auditoria=auditoria; this.diretorio=Path.of(diretorio).toAbsolutePath().normalize(); this.dbUrl=dbUrl;
        this.dbUsuario=dbUsuario; this.dbSenha=dbSenha; this.pgDump=pgDump; this.pgRestore=pgRestore;
    }

    @Scheduled(cron="${app.backup.cron:0 0 2 * * *}", zone="America/Sao_Paulo")
    public void backupDiario() { executarBackup(); }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    public BackupResponse criarBackupManual() { return toResponse(executarBackup()); }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @Transactional(readOnly=true)
    public List<BackupResponse> listar() {
        return backups.findByDataHoraAfterOrderByDataHoraDesc(LocalDateTime.now(ZONA).minusDays(30)).stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void restaurar(String arquivo, Integer solicitanteId) {
        Path selecionado=resolverArquivo(arquivo);
        if(!Files.isRegularFile(selecionado)) throw new RecursoNaoEncontrado("Arquivo de backup não encontrado.");
        // Guarda o estado atual antes de qualquer operação destrutiva.
        Backup seguranca=executarBackup();
        if(seguranca.getStatus()!=StatusBackup.SUCESSO) throw new RegraNegocioException("Restauração cancelada: não foi possível criar o backup de segurança do estado atual.");
        List<String> args=new ArrayList<>(conexao()); args.addAll(List.of(pgRestore,"--clean","--if-exists","--no-owner","--exit-on-error","--single-transaction",selecionado.toString()));
        // pg_restore options must precede the archive name; move the executable to argv[0].
        args.remove(pgRestore); args.add(0,pgRestore);
        executar(args,"restore");
        Integer idBackup=backups.findByArquivo(selecionado.getFileName().toString()).map(b->b.getIdBackup()).orElse(0);
        auditoria.registrar(solicitanteId,"BACKUP",idBackup,"RESTAURACAO","Restauração executada a partir de "+selecionado.getFileName());
    }

    private synchronized Backup executarBackup() {
        Backup registro=new Backup(); registro.setArquivo("backup_"+LocalDateTime.now(ZONA).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS", Locale.ROOT))+".dump"); registro.setStatus(StatusBackup.FALHA);
        try {
            Files.createDirectories(diretorio); Path destino=resolverArquivo(registro.getArquivo());
            List<String> args=new ArrayList<>(); args.add(pgDump); args.addAll(conexao()); args.addAll(List.of("--format=custom","--no-owner","--file",destino.toString()));
            executar(args,"backup"); registro.setTamanhoBytes(Files.size(destino)); registro.setStatus(StatusBackup.SUCESSO);
        } catch(Exception e) {
            registro.setTamanhoBytes(null);
        }
        return backups.save(registro);
    }

    private List<String> conexao() {
        if(dbUrl==null||!dbUrl.startsWith("jdbc:postgresql://")) throw new RegraNegocioException("DB_URL deve apontar para PostgreSQL para usar backup/restore.");
        URI uri=URI.create(dbUrl.substring("jdbc:".length()).split("\\?",2)[0]);
        String host=uri.getHost(); if(host==null) throw new RegraNegocioException("DB_URL inválida.");
        int port=uri.getPort()>0?uri.getPort():5432; String database=uri.getPath()==null?"":uri.getPath().replaceFirst("^/","");
        if(database.isBlank()) throw new RegraNegocioException("DB_URL precisa informar o nome do banco.");
        String user=dbUsuario;
        if((user==null||user.isBlank())&&uri.getUserInfo()!=null){String[] info=uri.getUserInfo().split(":",2);user=info[0];}
        if(user==null||user.isBlank()) throw new RegraNegocioException("Configure DB_USERNAME para executar backups PostgreSQL.");
        return new ArrayList<>(List.of("--host",host,"--port",Integer.toString(port),"--username",user,"--dbname",database));
    }

    private void executar(List<String> args,String operacao) {
        try {
            ProcessBuilder pb=new ProcessBuilder(args); pb.redirectErrorStream(true);
            String password=dbSenha;
            if((password==null||password.isBlank())&&dbUrl!=null&&dbUrl.startsWith("jdbc:postgresql://")) { String ui=URI.create(dbUrl.substring(5).split("\\?",2)[0]).getUserInfo(); if(ui!=null&&ui.contains(":"))password=ui.substring(ui.indexOf(':')+1); }
            if(password!=null&&!password.isBlank())pb.environment().put("PGPASSWORD",password);
            Process p=pb.start(); byte[] output=p.getInputStream().readAllBytes();
            if(!p.waitFor(20,TimeUnit.MINUTES)){p.destroyForcibly();throw new RegraNegocioException("Tempo limite excedido durante "+operacao+" PostgreSQL.");}
            if(p.exitValue()!=0)throw new RegraNegocioException("Falha no "+operacao+" PostgreSQL (código "+p.exitValue()+"). Consulte o log do processo para diagnóstico.");
        } catch(IOException e){throw new RegraNegocioException("Não foi possível executar "+operacao+". Configure pg_dump/pg_restore e permissões do diretório.");}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new RegraNegocioException("Operação de "+operacao+" interrompida.");}
    }

    private Path resolverArquivo(String nome) {
        if(nome==null||!nome.matches("backup_[0-9T_-]+\\.dump"))throw new RegraNegocioException("Nome de backup inválido.");
        Path alvo=diretorio.resolve(nome).normalize();if(!alvo.getParent().equals(diretorio))throw new RegraNegocioException("Caminho de backup inválido.");return alvo;
    }
    private BackupResponse toResponse(Backup b){return new BackupResponse(b.getIdBackup(),b.getDataHora(),b.getArquivo(),b.getTamanhoBytes(),b.getStatus());}
}
