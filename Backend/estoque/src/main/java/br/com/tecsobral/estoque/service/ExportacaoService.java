package br.com.tecsobral.estoque.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.tecsobral.estoque.entity.Produto;
import br.com.tecsobral.estoque.repository.ProdutoRepository;

@Service @PreAuthorize("hasAnyRole('PROPRIETARIO','FUNCIONARIO')")
public class ExportacaoService {
    private final ProdutoRepository produtos;
    public ExportacaoService(ProdutoRepository produtos){this.produtos=produtos;}
    @Transactional(readOnly=true) public byte[] produtosCsv(){
        List<Produto> lista=produtos.findAllByOrderByNomeAsc(org.springframework.data.domain.Pageable.unpaged()).getContent();
        StringBuilder csv=new StringBuilder("\uFEFFcodigo;nome;categoria;cor;tamanho;quantidade_atual;estoque_minimo;preco_compra;preco_venda\r\n");
        for(Produto p:lista)csv.append(linha(p.getCodigo())).append(';').append(linha(p.getNome())).append(';').append(linha(p.getCategoria().getNome())).append(';').append(linha(p.getCor())).append(';').append(linha(p.getTamanho())).append(';').append(p.getQuantidadeAtual()).append(';').append(p.getEstoqueMinimo()==null?"":p.getEstoqueMinimo()).append(';').append(p.getPrecoCompra()).append(';').append(p.getPrecoVenda()).append("\r\n");
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    private String linha(String valor){if(valor==null)return "\"\"";String seguro=valor.stripLeading().matches("(?i)^[=+@-].*")?"'"+valor:valor;return "\""+seguro.replace("\"","\"\"")+"\"";}
}
