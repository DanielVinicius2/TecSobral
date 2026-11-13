package br.com.tecsobral.estoque.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.tecsobral.estoque.entity.LogAlteracao;

@Repository
public interface LogAlteracaoRepository extends JpaRepository<LogAlteracao, Integer> {
    
    // Consulta logs de um usuário específico
    List<LogAlteracao> findByUsuarioIdUsuarioOrderByDataHoraDesc(Integer idUsuario);
    
    // Consulta histórico de alterações de um registro específico (ex: todas as edições de um Produto)
    List<LogAlteracao> findByEntidadeAndIdRegistroOrderByDataHoraDesc(String entidade, Integer idRegistro);
}