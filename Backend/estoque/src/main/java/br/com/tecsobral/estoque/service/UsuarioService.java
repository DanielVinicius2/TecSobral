package br.com.tecsobral.estoque.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.tecsobral.estoque.dto.ServiceDtos.SenhaResetRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioCreateRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioResponse;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioStatusRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioUpdateRequest;
import br.com.tecsobral.estoque.entity.Usuario;
import br.com.tecsobral.estoque.exception.RegraNegocioException;
import br.com.tecsobral.estoque.exception.RecursoNaoEncontrado;
import br.com.tecsobral.estoque.model.enums.Perfil;
import br.com.tecsobral.estoque.repository.UsuarioRepository;

@Service
@Transactional
@PreAuthorize("hasRole('PROPRIETARIO')")
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final LogAlteracaoService auditoria;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder, LogAlteracaoService auditoria) {
        this.usuarios = usuarios; this.encoder = encoder; this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll(Sort.by("nome").ascending()).stream().map(this::toResponse).toList();
    }

    public UsuarioResponse criar(UsuarioCreateRequest req, Integer atorId) {
        validarSenha(req.senha());
        String login = req.login().trim();
        if (usuarios.existsByLoginIgnoreCase(login)) throw new RegraNegocioException("Este login já está em uso.");
        Usuario u = new Usuario(); u.setNome(req.nome().trim()); u.setLogin(login);
        u.setSenhaHash(encoder.encode(req.senha())); u.setPerfil(req.perfil()); u.setAtivo(true);
        u = usuarios.saveAndFlush(u);
        auditoria.registrar(atorId, "USUARIO", u.getIdUsuario(), "CRIACAO", "Usuário criado: " + u.getLogin());
        return toResponse(u);
    }

    public UsuarioResponse atualizar(Integer id, UsuarioUpdateRequest req, Integer atorId) {
        Usuario u = obter(id); String login = req.login().trim();
        if (usuarios.existsByLoginIgnoreCase(login) && !login.equalsIgnoreCase(u.getLogin()))
            throw new RegraNegocioException("Este login já está em uso.");
        if (u.getPerfil() == Perfil.PROPRIETARIO && req.perfil() != Perfil.PROPRIETARIO && Boolean.TRUE.equals(u.getAtivo()))
            exigirOutroProprietarioAtivo(u);
        u.setNome(req.nome().trim()); u.setLogin(login); u.setPerfil(req.perfil());
        auditoria.registrar(atorId, "USUARIO", id, "ATUALIZACAO", "Dados e/ou perfil do usuário atualizados.");
        return toResponse(u);
    }

    public UsuarioResponse alterarStatus(Integer id, UsuarioStatusRequest req, Integer atorId) {
        Usuario u = obter(id);
        if (!req.ativo() && u.getIdUsuario().equals(atorId)) throw new RegraNegocioException("Não é permitido desativar a própria conta.");
        if (!req.ativo() && u.getPerfil() == Perfil.PROPRIETARIO && Boolean.TRUE.equals(u.getAtivo())) exigirOutroProprietarioAtivo(u);
        u.setAtivo(req.ativo());
        auditoria.registrar(atorId, "USUARIO", id, req.ativo() ? "REATIVACAO" : "DESATIVACAO", "Status ativo=" + req.ativo());
        return toResponse(u);
    }

    public void redefinirSenha(Integer id, SenhaResetRequest req, Integer atorId) {
        validarSenha(req.novaSenha()); Usuario u = obter(id); u.setSenhaHash(encoder.encode(req.novaSenha()));
        auditoria.registrar(atorId, "USUARIO", id, "REDEFINICAO_SENHA", "Senha redefinida por proprietário.");
    }

    private void exigirOutroProprietarioAtivo(Usuario u) {
        if (usuarios.countByPerfilAndAtivoTrue(Perfil.PROPRIETARIO) <= 1)
            throw new RegraNegocioException("O sistema precisa manter ao menos um proprietário ativo.");
    }
    private Usuario obter(Integer id) { return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontrado("Usuário não encontrado.")); }
    private void validarSenha(String s) {
        if (s == null || s.length() < 12 || s.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new RegraNegocioException("A senha deve ter ao menos 12 caracteres e até 72 bytes UTF-8.");
    }
    private UsuarioResponse toResponse(Usuario u) { return new UsuarioResponse(u.getIdUsuario(), u.getNome(), u.getLogin(), u.getPerfil(), Boolean.TRUE.equals(u.getAtivo()), u.getDataCriacao()); }
}
