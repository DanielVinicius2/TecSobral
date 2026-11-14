package br.com.tecsobral.estoque.config;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import br.com.tecsobral.estoque.entity.Usuario;
import br.com.tecsobral.estoque.model.enums.Perfil;
import br.com.tecsobral.estoque.repository.UsuarioRepository;

@Configuration
public class DataInitializer {
    @Bean
    ApplicationRunner criarProprietarioInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${app.bootstrap.owner-login:}") String login,
            @Value("${app.bootstrap.owner-password:}") String senha) {
        return args -> inicializar(usuarios, encoder, login, senha);
    }

    @Transactional
    void inicializar(UsuarioRepository usuarios, PasswordEncoder encoder, String login, String senha) {
        if (usuarios.countByPerfilAndAtivoTrue(Perfil.PROPRIETARIO) > 0) return;
        if (login == null || login.isBlank() || senha == null || senha.isBlank()) return;
        if (senha.length() < 12 || senha.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalStateException("APP_BOOTSTRAP_OWNER_PASSWORD precisa ter ao menos 12 caracteres e até 72 bytes UTF-8.");
        if (usuarios.existsByLoginIgnoreCase(login.trim())) throw new IllegalStateException("O login de bootstrap já pertence a outro usuário; revise a configuração do proprietário inicial.");
        Usuario u = new Usuario(); u.setNome("Proprietário"); u.setLogin(login.trim()); u.setSenhaHash(encoder.encode(senha)); u.setPerfil(Perfil.PROPRIETARIO); u.setAtivo(true); usuarios.save(u);
    }
}
