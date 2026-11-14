package br.com.tecsobral.estoque.exception;

public class RecursoNaoEncontrado extends RuntimeException {
	public RecursoNaoEncontrado(String mensagem) { super(mensagem); }

	public static RecursoNaoEncontrado produto(Integer id) {
		return new RecursoNaoEncontrado("Produto " + id + " não encontrado.");
	}
}
