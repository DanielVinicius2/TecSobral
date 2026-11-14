package br.com.tecsobral.estoque.service;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.tecsobral.estoque.dto.ServiceDtos.DashboardResponse;
import br.com.tecsobral.estoque.dto.ServiceDtos.ProdutoResponse;
import br.com.tecsobral.estoque.repository.ProdutoRepository;

@Service @PreAuthorize("hasAnyRole('PROPRIETARIO','FUNCIONARIO')")
public class DashboardService {
    private final ProdutoRepository produtos;
    public DashboardService(ProdutoRepository produtos){this.produtos=produtos;}
    @Transactional(readOnly=true) public DashboardResponse resumo(){
        List<ProdutoResponse> alertas=produtos.findProdutosParaAlerta().stream().map(p->new ProdutoResponse(p.getIdProduto(),p.getCodigo(),p.getNome(),p.getCategoria().getIdCategoria(),p.getCategoria().getNome(),p.getCor(),p.getTamanho(),p.getOutrasCaracteristicas(),p.getQuantidadeAtual(),p.getPrecoCompra(),p.getPrecoVenda(),p.getEstoqueMinimo(),p.getDataCadastro())).toList();
        long negativos=alertas.stream().filter(p->p.quantidadeAtual()<0).count();return new DashboardResponse(produtos.count(),alertas.size(),negativos,alertas);
    }
}
