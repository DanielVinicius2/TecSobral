package br.com.tecsobral.estoque.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.tecsobral.estoque.entity.Movimentacao;
import br.com.tecsobral.estoque.model.enums.TipoMovimentacao;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Integer> {
    
    // Filtros para o Histórico (T08)
    
    // Busca movimentações por produto
    List<Movimentacao> findByProdutoIdProdutoOrderByDataHoraDesc(Integer idProduto);
    
    // Busca movimentações por produto (mesmo se excluído, usando o código salvo)
    List<Movimentacao> findByCodigoProdutoOrderByDataHoraDesc(String codigoProduto);
    
    // Busca por tipo (Entrada, Saída, Correção)
    List<Movimentacao> findByTipoOrderByDataHoraDesc(TipoMovimentacao tipo);
    
    // Busca por período
    List<Movimentacao> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);

    @Query(value = "SELECT m FROM Movimentacao m LEFT JOIN m.produto p JOIN m.usuario u WHERE " +
           "(:inicio IS NULL OR m.dataHora >= :inicio) AND (:fim IS NULL OR m.dataHora < :fim) " +
           "AND (:tipo IS NULL OR m.tipo = :tipo) " +
           "AND (:produtoId IS NULL OR p.idProduto = :produtoId) " +
           "AND (:usuarioId IS NULL OR u.idUsuario = :usuarioId) " +
           "AND (:termo IS NULL OR LOWER(m.codigoProduto) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(m.nomeProduto) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
           "ORDER BY m.dataHora DESC, m.idMovimentacao DESC",
           countQuery = "SELECT COUNT(m) FROM Movimentacao m LEFT JOIN m.produto p JOIN m.usuario u WHERE " +
           "(:inicio IS NULL OR m.dataHora >= :inicio) AND (:fim IS NULL OR m.dataHora < :fim) " +
           "AND (:tipo IS NULL OR m.tipo = :tipo) " +
           "AND (:produtoId IS NULL OR p.idProduto = :produtoId) " +
           "AND (:usuarioId IS NULL OR u.idUsuario = :usuarioId) " +
           "AND (:termo IS NULL OR LOWER(m.codigoProduto) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(m.nomeProduto) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Movimentacao> pesquisarHistorico(@Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("tipo") TipoMovimentacao tipo,
            @Param("produtoId") Integer produtoId,
            @Param("usuarioId") Integer usuarioId,
            @Param("termo") String termo,
            Pageable pageable);
}
