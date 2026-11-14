package br.com.tecsobral.estoque.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.tecsobral.estoque.dto.ServiceDtos.LoginRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioAutenticado;
import br.com.tecsobral.estoque.entity.Usuario;
import br.com.tecsobral.estoque.repository.UsuarioRepository;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder) { this.usuarios = usuarios; this.encoder = encoder; }

    @Transactional(readOnly = true)
    public UsuarioAutenticado autenticar(LoginRequest req) {
        Usuario u = usuarios.findByLoginIgnoreCase(req.login().trim()).orElseThrow(AuthService::credenciaisInvalidas);
        if (!Boolean.TRUE.equals(u.getAtivo()) || !encoder.matches(req.senha(), u.getSenhaHash())) throw credenciaisInvalidas();
        return new UsuarioAutenticado(u.getIdUsuario(), u.getNome(), u.getLogin(), u.getPerfil());
    }
    private static BadCredentialsException credenciaisInvalidas() { return new BadCredentialsException("Login ou senha inválidos."); }
}
