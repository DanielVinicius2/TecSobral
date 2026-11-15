package br.com.tecsobral.estoque.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.tecsobral.estoque.dto.ServiceDtos.LoginRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioAutenticado;
import br.com.tecsobral.estoque.service.AuthService;
import jakarta.validation.Valid;

/** T01 - Login (RF01, RN01). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final AtorResolver ator;

	public AuthController(AuthService authService, AtorResolver ator) {
		this.authService = authService;
		this.ator = ator;
	}

	/**
	 * Confere login e senha e devolve os dados do usuário (id, nome, login e perfil).
	 * Rota pública. Login ou senha errados, ou usuário desativado, resultam em 401.
	 * Depois do login, o front envia "Authorization: Basic base64(login:senha)" nas demais rotas.
	 */
	@PostMapping("/login")
	public UsuarioAutenticado login(@Valid @RequestBody LoginRequest request) {
		return authService.autenticar(request);
	}

	/** Dados do usuário que está logado (útil para o front saber o perfil e esconder menus). */
	@GetMapping("/me")
	public UsuarioAutenticado me(Authentication auth) {
		return ator.dados(auth);
	}
}