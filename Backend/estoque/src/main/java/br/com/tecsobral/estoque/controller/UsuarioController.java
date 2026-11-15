package br.com.tecsobral.estoque.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.tecsobral.estoque.dto.ServiceDtos.SenhaResetRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioCreateRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioResponse;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioStatusRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.UsuarioUpdateRequest;
import br.com.tecsobral.estoque.service.UsuarioService;
import jakarta.validation.Valid;

/** T09 - Usuários: somente o proprietário (RF02, RN14, RN16). */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('PROPRIETARIO')")
public class UsuarioController {

	private final UsuarioService service;
	private final AtorResolver ator;

	public UsuarioController(UsuarioService service, AtorResolver ator) {
		this.service = service;
		this.ator = ator;
	}

	@GetMapping
	public List<UsuarioResponse> listar() {
		return service.listar();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioResponse criar(@Valid @RequestBody UsuarioCreateRequest request, Authentication auth) {
		return service.criar(request, ator.id(auth));
	}

	@PutMapping("/{id}")
	public UsuarioResponse atualizar(@PathVariable("id") Integer id,
			@Valid @RequestBody UsuarioUpdateRequest request, Authentication auth) {
		return service.atualizar(id, request, ator.id(auth));
	}

	/** RN16: usuário não é excluído; é ativado ou desativado. Body: {"ativo": true|false}. */
	@PutMapping("/{id}/status")
	public UsuarioResponse alterarStatus(@PathVariable("id") Integer id,
			@Valid @RequestBody UsuarioStatusRequest request, Authentication auth) {
		return service.alterarStatus(id, request, ator.id(auth));
	}

	@PutMapping("/{id}/senha")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void redefinirSenha(@PathVariable("id") Integer id,
			@Valid @RequestBody SenhaResetRequest request, Authentication auth) {
		service.redefinirSenha(id, request, ator.id(auth));
	}
}