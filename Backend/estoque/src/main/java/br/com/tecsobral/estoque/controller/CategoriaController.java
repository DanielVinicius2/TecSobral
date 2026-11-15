package br.com.tecsobral.estoque.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.tecsobral.estoque.dto.ServiceDtos.CategoriaRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.CategoriaResponse;
import br.com.tecsobral.estoque.service.CategoriaService;
import jakarta.validation.Valid;

/** Categorias de produtos (RF06), usadas no cadastro (T04) e nos filtros (T03). */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService service;
	private final AtorResolver ator;

	public CategoriaController(CategoriaService service, AtorResolver ator) {
		this.service = service;
		this.ator = ator;
	}

	@GetMapping
	public List<CategoriaResponse> listar() {
		return service.listar();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoriaResponse criar(@Valid @RequestBody CategoriaRequest request, Authentication auth) {
		return service.criar(request, ator.id(auth));
	}

	@PutMapping("/{id}")
	public CategoriaResponse renomear(@PathVariable("id") Integer id,
			@Valid @RequestBody CategoriaRequest request, Authentication auth) {
		return service.renomear(id, request, ator.id(auth));
	}
}