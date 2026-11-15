package br.com.tecsobral.estoque.controller;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioAutenticado;
import br.com.tecsobral.estoque.entity.Usuario;
import br.com.tecsobral.estoque.repository.UsuarioRepository;

/**
 * Descobre quem é o usuário logado na requisição (o "ator").
 * Os services recebem o id do ator para gravar o log de auditoria (RF17, RN02).
 */
@Component
public class AtorResolver {

	private final UsuarioRepository usuarios;

	public AtorResolver(UsuarioRepository usuarios) {
		this.usuarios = usuarios;
	}

	/** Id do usuário autenticado. */
	public Integer id(Authentication auth) {
		return buscar(auth).getIdUsuario();
	}

	/** Dados básicos do usuário autenticado (sem a senha). */
	public UsuarioAutenticado dados(Authentication auth) {
		Usuario u = buscar(auth);
		return new UsuarioAutenticado(u.getIdUsuario(), u.getNome(), u.getLogin(), u.getPerfil());
	}

	private Usuario buscar(Authentication auth) {
		if (auth == null || auth.getName() == null) {
			throw new BadCredentialsException("Login ou senha inválidos.");
		}
		return usuarios.findByLoginIgnoreCase(auth.getName())
				.orElseThrow(() -> new BadCredentialsException("Login ou senha inválidos."));
	}
}