package br.com.tecsobral.estoque.service;

import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.tecsobral.estoque.dto.ServiceDtos.*;
import br.com.tecsobral.estoque.entity.Categoria;
import br.com.tecsobral.estoque.entity.Movimentacao;
import br.com.tecsobral.estoque.entity.Produto;
import br.com.tecsobral.estoque.entity.Usuario;
import br.com.tecsobral.estoque.exception.RecursoNaoEncontrado;
import br.com.tecsobral.estoque.exception.RegraNegocioException;
import br.com.tecsobral.estoque.model.enums.TipoMovimentacao;
import br.com.tecsobral.estoque.repository.CategoriaRepository;
import br.com.tecsobral.estoque.repository.MovimentacaoRepository;
import br.com.tecsobral.estoque.repository.ProdutoRepository;
import br.com.tecsobral.estoque.repository.UsuarioRepository;

@Service @Transactional @PreAuthorize("hasAnyRole('PROPRIETARIO','FUNCIONARIO')")
public class ProdutoService {
    private final ProdutoRepository produtos; private final CategoriaRepository categorias; private final UsuarioRepository usuarios;
    private final MovimentacaoRepository movimentacoes; private final LogAlteracaoService auditoria;
    public ProdutoService(ProdutoRepository produtos, CategoriaRepository categorias, UsuarioRepository usuarios, MovimentacaoRepository movimentacoes, LogAlteracaoService auditoria) {
        this.produtos=produtos; this.categorias=categorias; this.usuarios=usuarios; this.movimentacoes=movimentacoes; this.auditoria=auditoria;
    }
    public ProdutoResponse criar(ProdutoCreateRequest req, Integer atorId) {
        String codigo=req.codigo().trim(); if (produtos.existsByCodigo(codigo)) throw new RegraNegocioException("Código de produto já cadastrado.");
        validarPrecos(req.precoCompra(),req.precoVenda()); Produto p=new Produto(); preencher(p,req.codigo(),req.nome(),req.categoriaId(),req.cor(),req.tamanho(),req.outrasCaracteristicas(),req.precoCompra(),req.precoVenda(),req.estoqueMinimo());
        p.setQuantidadeAtual(0); p=produtos.saveAndFlush(p);
        if(req.quantidadeInicial()>0) registrarEntradaInicial(p,req.quantidadeInicial(),atorId);
        auditoria.registrar(atorId,"PRODUTO",p.getIdProduto(),"CRIACAO","Produto cadastrado: "+p.getCodigo()); return toResponse(p);
    }
    public ProdutoResponse atualizar(Integer id, ProdutoUpdateRequest req, Integer atorId) {
        Produto p=obter(id); String codigo=req.codigo().trim();
        if(produtos.existsByCodigoAndIdProdutoNot(codigo,id)) throw new RegraNegocioException("Código de produto já cadastrado.");
        validarPrecos(req.precoCompra(),req.precoVenda()); preencher(p,req.codigo(),req.nome(),req.categoriaId(),req.cor(),req.tamanho(),req.outrasCaracteristicas(),req.precoCompra(),req.precoVenda(),req.estoqueMinimo());
        auditoria.registrar(atorId,"PRODUTO",id,"ATUALIZACAO","Dados cadastrais atualizados; saldo mantido."); return toResponse(p);
    }
    @PreAuthorize("hasRole('PROPRIETARIO')")
    public void excluir(Integer id, boolean confirmar, Integer atorId) {
        if(!confirmar) throw new RegraNegocioException("A exclusão exige confirmação explícita."); Produto p=obter(id);
        auditoria.registrar(atorId,"PRODUTO",id,"EXCLUSAO","Produto excluído; histórico preservado em snapshots."); produtos.delete(p); produtos.flush();
    }
    @Transactional(readOnly=true) public ProdutoResponse buscar(Integer id) { return toResponse(obter(id)); }
    @Transactional(readOnly=true) public PageResponse<ProdutoResponse> pesquisar(ProdutoFiltro f) {
        validarPagina(f.pagina(),f.tamanhoPagina()); Page<Produto> pg=produtos.pesquisarProdutos(limpar(f.termo()),f.categoriaId(),limpar(f.cor()),limpar(f.tamanho()),f.apenasAlertas(),PageRequest.of(f.pagina(),f.tamanhoPagina(),Sort.by("nome").ascending()));
        return new PageResponse<>(pg.getContent().stream().map(this::toResponse).toList(),pg.getNumber(),pg.getSize(),pg.getTotalElements());
    }
    private void registrarEntradaInicial(Produto p,int qtd,Integer atorId) {
        Usuario u=usuarios.findById(atorId).orElseThrow(()->new RecursoNaoEncontrado("Usuário não encontrado.")); p.setQuantidadeAtual(qtd);
        Movimentacao m=new Movimentacao(); m.setProduto(p);m.setUsuario(u);m.setTipo(TipoMovimentacao.ENTRADA);m.setQuantidade(qtd);m.setPrecoUnitario(p.getPrecoCompra());m.setSaldoApos(qtd);m.setJustificativa("Estoque inicial no cadastro do produto.");m.setCodigoProduto(p.getCodigo());m.setNomeProduto(p.getNome());movimentacoes.save(m);
    }
    private void preencher(Produto p,String codigo,String nome,Integer categoriaId,String cor,String tamanho,String outras,BigDecimal compra,BigDecimal venda,Integer minimo) {
        Categoria c=categorias.findById(categoriaId).orElseThrow(()->new RecursoNaoEncontrado("Categoria não encontrada."));
        p.setCodigo(codigo.trim());p.setNome(nome.trim());p.setCategoria(c);p.setCor(limpar(cor));p.setTamanho(limpar(tamanho));p.setOutrasCaracteristicas(limpar(outras));p.setPrecoCompra(compra);p.setPrecoVenda(venda);p.setEstoqueMinimo(minimo);
    }
    private Produto obter(Integer id){return produtos.findById(id).orElseThrow(()->new RecursoNaoEncontrado("Produto não encontrado."));}
    private void validarPrecos(BigDecimal a,BigDecimal b){if(a==null||b==null||a.signum()<0||b.signum()<0)throw new RegraNegocioException("Os preços devem ser informados e não podem ser negativos.");}
    private void validarPagina(int p,int s){if(p<0||s<1||s>100)throw new RegraNegocioException("Paginação inválida (tamanho máximo: 100).");}
    private String limpar(String s){return s==null||s.isBlank()?null:s.trim();}
    private ProdutoResponse toResponse(Produto p){return new ProdutoResponse(p.getIdProduto(),p.getCodigo(),p.getNome(),p.getCategoria().getIdCategoria(),p.getCategoria().getNome(),p.getCor(),p.getTamanho(),p.getOutrasCaracteristicas(),p.getQuantidadeAtual(),p.getPrecoCompra(),p.getPrecoVenda(),p.getEstoqueMinimo(),p.getDataCadastro());}
}
