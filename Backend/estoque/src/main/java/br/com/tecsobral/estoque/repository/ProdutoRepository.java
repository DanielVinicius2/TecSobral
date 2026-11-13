package br.com.tecsobral.estoque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.tecsobral.estoque.entity.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    
    // RN08: O código não pode se repetir
    boolean existsByCodigo(String codigo);
    
    Optional<Produto> findByCodigo(String codigo);
    
    // Consultas para o Painel Inicial (T02)
    
    // Busca produtos onde a quantidade_atual é menor que o estoque_minimo (e o mínimo não é nulo)
    @Query("SELECT p FROM Produto p WHERE p.estoqueMinimo IS NOT NULL AND p.quantidadeAtual < p.estoqueMinimo")
    List<Produto> findProdutosComEstoqueBaixo();
    
    // RN05: Busca produtos com saldo negativo
    @Query("SELECT p FROM Produto p WHERE p.quantidadeAtual < 0")
    List<Produto> findProdutosComSaldoNegativo();
    
    // Pesquisa de produtos por nome, código, categoria, cor ou tamanho (RF14 / T03)
    @Query("SELECT p FROM Produto p WHERE " +
           "LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.categoria.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.cor) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.tamanho) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Produto> pesquisarProdutos(String termo);
}