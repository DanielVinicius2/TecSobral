package br.com.tecsobral.estoque.entity;

import java.time.LocalDateTime;

import br.com.tecsobral.estoque.model.enums.StatusBackup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "backup")
@Getter
@Setter
@NoArgsConstructor
public class Backup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_backup")
    private Integer idBackup;

    @Column(name = "data_hora",
            insertable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String arquivo;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusBackup status;
}