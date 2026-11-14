package br.com.tecsobral.estoque.exception;

import java.util.List;

import br.com.tecsobral.estoque.dto.ServiceDtos.SaldoNegativoItem;

public class ConfirmacaoSaldoNegativo extends RegraNegocioException {
	private final List<SaldoNegativoItem> itens;

	public ConfirmacaoSaldoNegativo(List<SaldoNegativoItem> itens) {
		super("A venda deixará saldo negativo. Confirme a operação para continuar.");
		this.itens = List.copyOf(itens);
	}

	public List<SaldoNegativoItem> getItens() { return itens; }
}
