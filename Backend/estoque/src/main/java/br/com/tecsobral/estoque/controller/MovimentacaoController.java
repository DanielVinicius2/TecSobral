package br.com.tecsobral.estoque.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.tecsobral.estoque.dto.ServiceDtos.CorrecaoRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.EntradaRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.MovimentacaoFiltro;
import br.com.tecsobral.estoque.dto.ServiceDtos.MovimentacaoResponse;
import br.com.tecsobral.estoque.dto.ServiceDtos.PageResponse;
import br.com.tecsobral.estoque.dto.ServiceDtos.VendaRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.VendaResponse;
import br.com.tecsobral.estoque.model.enums.TipoMovimentacao;
import br.com.tecsobral.estoque.service.MovimentacaoService;
import jakarta.validation.Valid;

/** T05 (entrada), T06 (venda), T07 (correção) e T08 (histórico): RF08 a RF11, RF16. */
@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {

	private final MovimentacaoService service;
	private final AtorResolver ator;

	public MovimentacaoController(MovimentacaoService service, AtorResolver ator) {
		this.service = service;
		this.ator = ator;
	}

	/** RF08, RN09: registra a entrada de mercadoria e soma ao saldo. */
	@PostMapping("/entradas")
	@ResponseStatus(HttpStatus.CREATED)
	public MovimentacaoResponse entrada(@Valid @RequestBody EntradaRequest request, Authentication auth) {
		return service.entrada(request, ator.id(auth));
	}

	/**
	 * RF09, RF10, RN04: registra a venda (um ou mais itens).
	 * Se algum item deixar o saldo negativo e confirmarSaldoNegativo for false, responde 409
	 * (CONFIRMACAO_SALDO_NEGATIVO) com os itens afetados. Para seguir, reenvie com confirmarSaldoNegativo=true.
	 */
	@PostMapping("/vendas")
	@ResponseStatus(HttpStatus.CREATED)
	public VendaResponse vender(@Valid @RequestBody VendaRequest request, Authentication auth) {
		return service.vender(request, ator.id(auth));
	}

	/** RF11, RN03: corrige o saldo; a justificativa é obrigatória. */
	@PostMapping("/correcoes")
	@ResponseStatus(HttpStatus.CREATED)
	public MovimentacaoResponse corrigir(@Valid @RequestBody CorrecaoRequest request, Authentication auth) {
		return service.corrigir(request, ator.id(auth));
	}

	/**
	 * RF16: histórico somente leitura. Datas no formato AAAA-MM-DD.
	 * tipo: ENTRADA, SAIDA ou CORRECAO. produto: texto (código ou nome).
	 */
	@GetMapping
	public PageResponse<MovimentacaoResponse> historico(
			@RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
			@RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
			@RequestParam(name = "tipo", required = false) TipoMovimentacao tipo,
			@RequestParam(name = "produtoId", required = false) Integer produtoId,
			@RequestParam(name = "produto", required = false) String produto,
			@RequestParam(name = "usuarioId", required = false) Integer usuarioId,
			@RequestParam(name = "pagina", defaultValue = "0") int pagina,
			@RequestParam(name = "tamanhoPagina", defaultValue = "20") int tamanhoPagina) {
		return service.historico(new MovimentacaoFiltro(inicio, fim, tipo, produtoId, produto, usuarioId, pagina, tamanhoPagina));
	}
}