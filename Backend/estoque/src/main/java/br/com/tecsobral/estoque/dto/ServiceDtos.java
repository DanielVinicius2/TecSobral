package br.com.tecsobral.estoque.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.tecsobral.estoque.model.enums.Perfil;
import br.com.tecsobral.estoque.model.enums.StatusBackup;
import br.com.tecsobral.estoque.model.enums.TipoMovimentacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class ServiceDtos {
	private ServiceDtos() {}

	public record LoginRequest(@NotBlank @Size(max = 40) String login, @NotBlank String senha) {}
	public record UsuarioAutenticado(Integer id, String nome, String login, Perfil perfil) {}
	public record UsuarioCreateRequest(@NotBlank @Size(max = 100) String nome,
			@NotBlank @Size(max = 40) String login,
			@NotBlank @Size(min = 12, max = 72) String senha,
			@NotNull Perfil perfil) {}
	public record UsuarioUpdateRequest(@NotBlank @Size(max = 100) String nome,
			@NotBlank @Size(max = 40) String login, @NotNull Perfil perfil) {}
	public record UsuarioStatusRequest(@NotNull Boolean ativo) {}
	public record SenhaResetRequest(@NotBlank @Size(min = 12, max = 72) String novaSenha) {}
	public record UsuarioResponse(Integer id, String nome, String login, Perfil perfil,
			boolean ativo, LocalDateTime criadoEm) {}

	public record CategoriaRequest(@NotBlank @Size(max = 60) String nome) {}
	public record CategoriaResponse(Integer id, String nome) {}

	public record ProdutoCreateRequest(@NotBlank @Size(max = 30) String codigo,
			@NotBlank @Size(max = 120) String nome, @NotNull @Positive Integer categoriaId,
			@Size(max = 40) String cor, @Size(max = 20) String tamanho,
			@Size(max = 255) String outrasCaracteristicas,
			@NotNull @Min(0) Integer quantidadeInicial,
			@NotNull @Digits(integer = 8, fraction = 2) BigDecimal precoCompra,
			@NotNull @Digits(integer = 8, fraction = 2) BigDecimal precoVenda,
			@Min(0) Integer estoqueMinimo) {}
	public record ProdutoUpdateRequest(@NotBlank @Size(max = 30) String codigo,
			@NotBlank @Size(max = 120) String nome, @NotNull @Positive Integer categoriaId,
			@Size(max = 40) String cor, @Size(max = 20) String tamanho,
			@Size(max = 255) String outrasCaracteristicas,
			@NotNull @Digits(integer = 8, fraction = 2) BigDecimal precoCompra,
			@NotNull @Digits(integer = 8, fraction = 2) BigDecimal precoVenda,
			@Min(0) Integer estoqueMinimo) {}
	public record ProdutoFiltro(String termo, Integer categoriaId, String cor, String tamanho,
			boolean apenasAlertas, int pagina, int tamanhoPagina) {}
	public record ProdutoResponse(Integer id, String codigo, String nome, Integer categoriaId,
			String categoria, String cor, String tamanho, String outrasCaracteristicas,
			Integer quantidadeAtual, BigDecimal precoCompra, BigDecimal precoVenda,
			Integer estoqueMinimo, LocalDateTime cadastradoEm) {}

	public record EntradaRequest(@NotNull @Positive Integer produtoId,
			@NotNull @Positive Integer quantidade,
			@NotNull @Digits(integer = 8, fraction = 2) BigDecimal precoCompra) {}
	public record VendaItemRequest(@NotNull @Positive Integer produtoId,
			@NotNull @Positive Integer quantidade) {}
	public record VendaRequest(@NotEmpty List<@Valid VendaItemRequest> itens,
			boolean confirmarSaldoNegativo) {}
	public record CorrecaoRequest(@NotNull @Positive Integer produtoId,
			@NotNull @Min(0) Integer novoSaldo,
			@NotBlank @Size(max = 255) String justificativa) {}
	public record MovimentacaoFiltro(LocalDate inicio, LocalDate fim, TipoMovimentacao tipo,
			Integer produtoId, String produto, Integer usuarioId, int pagina, int tamanhoPagina) {}
	public record SaldoNegativoItem(Integer produtoId, String codigo, String nome,
			int saldoAtual, int quantidadeSaida, int saldoDepois) {}
	public record MovimentacaoResponse(Integer id, Integer produtoId, Integer usuarioId,
			String usuarioLogin, TipoMovimentacao tipo, Integer quantidade,
			BigDecimal precoUnitario, Integer saldoApos, String justificativa,
			String codigoProduto, String nomeProduto, LocalDateTime dataHora) {}
	public record VendaResponse(List<MovimentacaoResponse> movimentacoes,
			List<SaldoNegativoItem> saldosNegativos) {}
	public record PageResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos) {}

	public record DashboardResponse(long totalProdutos, long produtosEstoqueBaixo,
			long produtosSaldoNegativo, List<ProdutoResponse> alertas) {}
	public record LogResponse(Integer id, Integer usuarioId, String usuarioLogin,
			String entidade, Integer registroId, String acao, String detalhes,
			LocalDateTime dataHora) {}
	public record BackupResponse(Integer id, LocalDateTime dataHora, String arquivo,
			Long tamanhoBytes, StatusBackup status) {}
}
