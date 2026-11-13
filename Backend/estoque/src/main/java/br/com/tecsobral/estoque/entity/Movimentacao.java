package br.com.tecsobral.estoque.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.tecsobral.estoque.model.enums.TipoMovimentacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "movimentacao")
@Getter
@Setter
@NoArgsConstructor
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimentacao")
    private Integer idMovimentacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto")
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "saldo_apos", nullable = false)
    private Integer saldoApos;

    @Column(length = 255)
    private String justificativa;

    @Column(name = "codigo_produto", nullable = false)
    private String codigoProduto;

    @Column(name = "nome_produto", nullable = false)
    private String nomeProduto;

    @Column(name = "data_hora",
            insertable = false, updatable = false)
    private LocalDateTime dataHora;
}