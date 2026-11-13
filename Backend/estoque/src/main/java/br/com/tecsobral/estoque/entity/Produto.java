package br.com.tecsobral.estoque.entity;

import java.math.BigDecimal;
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
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Integer idProduto;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(length = 40)
    private String cor;

    @Column(length = 20)
    private String tamanho;

    @Column(name = "outras_caracteristicas")
    private String outrasCaracteristicas;

    @Column(name = "quantidade_atual", nullable = false)
    private Integer quantidadeAtual = 0;

    @Column(name = "preco_compra", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal precoCompra;

    @Column(name = "preco_venda", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo;

    @Column(name = "data_cadastro",
            insertable = false, updatable = false)
    private LocalDateTime dataCadastro;
}