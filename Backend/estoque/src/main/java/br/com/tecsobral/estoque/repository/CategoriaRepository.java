package br.com.tecsobral.estoque.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.tecsobral.estoque.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    
    // Verifica se já existe uma categoria com o mesmo nome para evitar duplicidade
    boolean existsByNomeIgnoreCase(String nome);
    
    Optional<Categoria> findByNomeIgnoreCase(String nome);
}