package br.com.tecsobral.estoque.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.tecsobral.estoque.dto.ServiceDtos.PageResponse;
import br.com.tecsobral.estoque.dto.ServiceDtos.ProdutoCreateRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.ProdutoFiltro;
import br.com.tecsobral.estoque.dto.ServiceDtos.ProdutoResponse;
import br.com.tecsobral.estoque.dto.ServiceDtos.ProdutoUpdateRequest;
import br.com.tecsobral.estoque.service.ExportacaoService;
import br.com.tecsobral.estoque.service.ProdutoService;
import jakarta.validation.Valid;

/** T03 (lista e pesquisa) e T04 (cadastro e edição): RF03, RF04, RF05, RF14, RF15, RF20. */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

	private final ProdutoService service;
	private final ExportacaoService exportacao;
	private final AtorResolver ator;

	public ProdutoController(ProdutoService service, ExportacaoService exportacao, AtorResolver ator) {
		this.service = service;
		this.exportacao = exportacao;
		this.ator = ator;
	}

	/**
	 * Pesquisa e listagem paginada (RF14, RF15).
	 * termo: procura em nome, código e categoria. apenasAlertas=true mostra só estoque baixo ou negativo.
	 * Atenção: "tamanho" é o tamanho do PRODUTO (P, M, G...); o tamanho da página é "tamanhoPagina".
	 */
	@GetMapping
	public PageResponse<ProdutoResponse> pesquisar(
			@RequestParam(name = "termo", required = false) String termo,
			@RequestParam(name = "categoriaId", required = false) Integer categoriaId,
			@RequestParam(name = "cor", required = false) String cor,
			@RequestParam(name = "tamanho", required = false) String tamanho,
			@RequestParam(name = "apenasAlertas", defaultValue = "false") boolean apenasAlertas,
			@RequestParam(name = "pagina", defaultValue = "0") int pagina,
			@RequestParam(name = "tamanhoPagina", defaultValue = "20") int tamanhoPagina) {
		return service.pesquisar(new ProdutoFiltro(termo, categoriaId, cor, tamanho, apenasAlertas, pagina, tamanhoPagina));
	}

	/** RF20: baixa a lista de produtos em CSV (abre no Excel). */
	@GetMapping("/exportacao/csv")
	public ResponseEntity<byte[]> exportarCsv() {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"produtos.csv\"")
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.body(exportacao.produtosCsv());
	}

	@GetMapping("/{id}")
	public ProdutoResponse buscar(@PathVariable("id") Integer id) {
		return service.buscar(id);
	}

	/** RF03: o saldo inicial, se maior que zero, vira uma entrada no histórico. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProdutoResponse criar(@Valid @RequestBody ProdutoCreateRequest request, Authentication auth) {
		return service.criar(request, ator.id(auth));
	}

	/** RF04: edita os dados cadastrais; o saldo não muda por aqui. */
	@PutMapping("/{id}")
	public ProdutoResponse atualizar(@PathVariable("id") Integer id,
			@Valid @RequestBody ProdutoUpdateRequest request, Authentication auth) {
		return service.atualizar(id, request, ator.id(auth));
	}

	/**
	 * RF05, RN11, RN14: exclusão definitiva, só para o proprietário e com confirmação explícita
	 * (?confirmar=true). O histórico é preservado.
	 */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable("id") Integer id,
			@RequestParam(name = "confirmar", defaultValue = "false") boolean confirmar, Authentication auth) {
		service.excluir(id, confirmar, ator.id(auth));
	}
}