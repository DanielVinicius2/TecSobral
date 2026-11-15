package br.com.tecsobral.estoque.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.tecsobral.estoque.dto.ServiceDtos.LogResponse;
import br.com.tecsobral.estoque.dto.ServiceDtos.PageResponse;
import br.com.tecsobral.estoque.service.LogAlteracaoService;

/** Trilha de auditoria (RF17, RN02, RNF05): somente leitura e somente para o proprietário. */
@RestController
@RequestMapping("/api/logs")
@PreAuthorize("hasRole('PROPRIETARIO')")
public class LogAlteracaoController {

	private final LogAlteracaoService service;

	public LogAlteracaoController(LogAlteracaoService service) {
		this.service = service;
	}

	@GetMapping
	public PageResponse<LogResponse> listar(
			@RequestParam(name = "pagina", defaultValue = "0") int pagina,
			@RequestParam(name = "tamanhoPagina", defaultValue = "20") int tamanhoPagina) {
		Page<LogResponse> resultado = service.listar(pagina, tamanhoPagina);
		return new PageResponse<>(resultado.getContent(), resultado.getNumber(), resultado.getSize(),
				resultado.getTotalElements());
	}

	@GetMapping("/usuario/{usuarioId}")
	public List<LogResponse> porUsuario(@PathVariable("usuarioId") Integer usuarioId) {
		return service.porUsuario(usuarioId);
	}

	/** Ex.: /api/logs/registro?entidade=PRODUTO&registroId=3 */
	@GetMapping("/registro")
	public List<LogResponse> porRegistro(@RequestParam(name = "entidade") String entidade,
			@RequestParam(name = "registroId") Integer registroId) {
		return service.porRegistro(entidade, registroId);
	}
}