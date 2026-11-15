package br.com.tecsobral.estoque.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.tecsobral.estoque.dto.ServiceDtos.BackupResponse;
import br.com.tecsobral.estoque.service.BackupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/** T10 - Backup e restauração: somente o proprietário (RF18, RF19, RN13, RN14). */
@RestController
@RequestMapping("/api/backups")
@PreAuthorize("hasRole('PROPRIETARIO')")
public class BackupController {

	/** Corpo do pedido de restauração: o nome do arquivo, exatamente como listado em GET /api/backups. */
	public record RestauracaoRequest(@NotBlank String arquivo) {}

	private final BackupService service;
	private final AtorResolver ator;

	public BackupController(BackupService service, AtorResolver ator) {
		this.service = service;
		this.ator = ator;
	}

	/** Cópias dos últimos 30 dias (o backup diário é automático). */
	@GetMapping
	public List<BackupResponse> listar() {
		return service.listar();
	}

	/** Backup manual extra, útil para testar. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BackupResponse criarManual() {
		return service.criarBackupManual();
	}

	/** RF19: restaura a cópia escolhida (o sistema guarda um backup do estado atual antes de restaurar). */
	@PostMapping("/restauracao")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void restaurar(@Valid @RequestBody RestauracaoRequest request, Authentication auth) {
		service.restaurar(request.arquivo(), ator.id(auth));
	}
}