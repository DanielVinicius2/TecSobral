package br.com.tecsobral.estoque.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
}