package br.com.tecsobral.estoque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import br.com.tecsobral.estoque.entity.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    
    // RN08: O código não pode se repetir
    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdProdutoNot(String codigo, Integer idProduto);
    
    Optional<Produto> findByCodigo(String codigo);
    
    Page<Produto> findAllByOrderByNomeAsc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Produto p WHERE p.idProduto = :id")
    Optional<Produto> findByIdForUpdate(@Param("id") Integer id);

    @Query("SELECT p FROM Produto p WHERE p.quantidadeAtual < 0 OR " +
           "(p.estoqueMinimo IS NOT NULL AND p.quantidadeAtual < p.estoqueMinimo) ORDER BY p.nome")
    List<Produto> findProdutosParaAlerta();

    @Query(value = "SELECT p FROM Produto p LEFT JOIN p.categoria c WHERE " +
           "(:termo IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(COALESCE(p.cor, '')) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(COALESCE(p.tamanho, '')) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
           "AND (:categoriaId IS NULL OR c.idCategoria = :categoriaId) " +
           "AND (:cor IS NULL OR LOWER(COALESCE(p.cor, '')) LIKE LOWER(CONCAT('%', :cor, '%'))) " +
           "AND (:tamanho IS NULL OR LOWER(COALESCE(p.tamanho, '')) LIKE LOWER(CONCAT('%', :tamanho, '%'))) " +
           "AND (:apenasAlertas = FALSE OR p.quantidadeAtual < 0 OR " +
           "(p.estoqueMinimo IS NOT NULL AND p.quantidadeAtual < p.estoqueMinimo))",
           countQuery = "SELECT COUNT(p) FROM Produto p LEFT JOIN p.categoria c WHERE " +
           "(:termo IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(COALESCE(p.cor, '')) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR LOWER(COALESCE(p.tamanho, '')) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
           "AND (:categoriaId IS NULL OR c.idCategoria = :categoriaId) " +
           "AND (:cor IS NULL OR LOWER(COALESCE(p.cor, '')) LIKE LOWER(CONCAT('%', :cor, '%'))) " +
           "AND (:tamanho IS NULL OR LOWER(COALESCE(p.tamanho, '')) LIKE LOWER(CONCAT('%', :tamanho, '%'))) " +
           "AND (:apenasAlertas = FALSE OR p.quantidadeAtual < 0 OR " +
           "(p.estoqueMinimo IS NOT NULL AND p.quantidadeAtual < p.estoqueMinimo))")
    Page<Produto> pesquisarProdutos(@Param("termo") String termo,
            @Param("categoriaId") Integer categoriaId,
            @Param("cor") String cor,
            @Param("tamanho") String tamanho,
            @Param("apenasAlertas") boolean apenasAlertas,
            Pageable pageable);
}
