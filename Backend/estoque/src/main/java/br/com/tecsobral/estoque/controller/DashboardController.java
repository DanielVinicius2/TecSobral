package br.com.tecsobral.estoque.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.tecsobral.estoque.dto.ServiceDtos.DashboardResponse;
import br.com.tecsobral.estoque.service.DashboardService;

/** T02 - Painel inicial: produtos com estoque baixo ou saldo negativo (RF12, RF13). */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private final DashboardService service;

	public DashboardController(DashboardService service) {
		this.service = service;
	}

	@GetMapping
	public DashboardResponse resumo() {
		return service.resumo();
	}
}