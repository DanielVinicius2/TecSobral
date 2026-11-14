package br.com.tecsobral.estoque.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.tecsobral.estoque.entity.Backup;

@Repository
public interface BackupRepository extends JpaRepository<Backup, Integer> {
    
    // RN13: Lista os backups, geralmente usado para listar os dos últimos 30 dias (T10)
    List<Backup> findByDataHoraAfterOrderByDataHoraDesc(LocalDateTime data);

    List<Backup> findByDataHoraBefore(LocalDateTime data);

    boolean existsByArquivo(String arquivo);

    java.util.Optional<Backup> findByArquivo(String arquivo);
}
