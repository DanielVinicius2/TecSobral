package br.com.tecsobral.estoque.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "log_alteracao")
@Getter
@Setter
@NoArgsConstructor
public class LogAlteracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Integer idLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 30)
    private String entidade;

    @Column(name = "id_registro", nullable = false)
    private Integer idRegistro;

    @Column(nullable = false, length = 30)
    private String acao;

    @Column(length = 500)
    private String detalhes;

    @Column(name = "data_hora",
            insertable = false, updatable = false)
    private LocalDateTime dataHora;
}