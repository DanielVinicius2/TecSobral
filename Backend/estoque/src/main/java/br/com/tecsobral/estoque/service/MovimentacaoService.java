package br.com.tecsobral.estoque.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.tecsobral.estoque.dto.ServiceDtos.*;
import br.com.tecsobral.estoque.entity.Movimentacao;
import br.com.tecsobral.estoque.entity.Produto;
import br.com.tecsobral.estoque.entity.Usuario;
import br.com.tecsobral.estoque.exception.ConfirmacaoSaldoNegativo;
import br.com.tecsobral.estoque.exception.RegraNegocioException;
import br.com.tecsobral.estoque.exception.RecursoNaoEncontrado;
import br.com.tecsobral.estoque.model.enums.TipoMovimentacao;
import br.com.tecsobral.estoque.repository.MovimentacaoRepository;
import br.com.tecsobral.estoque.repository.ProdutoRepository;
import br.com.tecsobral.estoque.repository.UsuarioRepository;

@Service @Transactional @PreAuthorize("hasAnyRole('PROPRIETARIO','FUNCIONARIO')")
public class MovimentacaoService {
    private final ProdutoRepository produtos; private final UsuarioRepository usuarios; private final MovimentacaoRepository movimentacoes; private final LogAlteracaoService auditoria;
    public MovimentacaoService(ProdutoRepository produtos, UsuarioRepository usuarios, MovimentacaoRepository movimentacoes, LogAlteracaoService auditoria) { this.produtos=produtos;this.usuarios=usuarios;this.movimentacoes=movimentacoes;this.auditoria=auditoria; }

    public MovimentacaoResponse entrada(EntradaRequest req,Integer atorId) {
        Produto p=travarProduto(req.produtoId()); validarPreco(req.precoCompra()); int saldo=somar(p.getQuantidadeAtual(),req.quantidade()); p.setQuantidadeAtual(saldo);p.setPrecoCompra(req.precoCompra());
        Movimentacao m=criarMov(p,atorId,TipoMovimentacao.ENTRADA,req.quantidade(),req.precoCompra(),saldo,null);m=movimentacoes.saveAndFlush(m);
        auditoria.registrar(atorId,"PRODUTO",p.getIdProduto(),"ENTRADA_ESTOQUE","Entrada de "+req.quantidade()+" unidade(s); saldo="+saldo);
        return toResponse(m);
    }

    public VendaResponse vender(VendaRequest req,Integer atorId) {
        if(req.itens()==null||req.itens().isEmpty())throw new RegraNegocioException("Informe ao menos um item para a venda.");
        Map<Integer,Integer> quantidades=new TreeMap<>();
        for(VendaItemRequest item:req.itens()) { if(item==null||item.produtoId()==null||item.quantidade()==null||item.quantidade()<=0)throw new RegraNegocioException("Produto e quantidade positiva são obrigatórios."); quantidades.merge(item.produtoId(),item.quantidade(),this::somar); }
        Map<Integer,Produto> travados=new LinkedHashMap<>(); for(Integer id:quantidades.keySet()) travados.put(id,travarProduto(id));
        List<SaldoNegativoItem> negativos=new ArrayList<>();
        for(var item:quantidades.entrySet()){Produto p=travados.get(item.getKey());int depois=subtrair(p.getQuantidadeAtual(),item.getValue());if(depois<0)negativos.add(new SaldoNegativoItem(p.getIdProduto(),p.getCodigo(),p.getNome(),p.getQuantidadeAtual(),item.getValue(),depois));}
        if(!negativos.isEmpty()&&!req.confirmarSaldoNegativo())throw new ConfirmacaoSaldoNegativo(negativos);
        Usuario ator=obterUsuario(atorId);List<Movimentacao> salvas=new ArrayList<>();
        for(var item:quantidades.entrySet()){Produto p=travados.get(item.getKey());int saldo=subtrair(p.getQuantidadeAtual(),item.getValue());p.setQuantidadeAtual(saldo);Movimentacao m=criarMov(p,ator,TipoMovimentacao.SAIDA,item.getValue(),p.getPrecoVenda(),saldo,null);salvas.add(movimentacoes.save(m)); auditoria.registrar(atorId,"PRODUTO",p.getIdProduto(),"SAIDA_ESTOQUE","Venda de "+item.getValue()+" unidade(s); saldo="+saldo);}
        movimentacoes.flush(); return new VendaResponse(salvas.stream().map(this::toResponse).toList(),negativos);
    }

    public MovimentacaoResponse corrigir(CorrecaoRequest req,Integer atorId) {
        if(req.justificativa()==null||req.justificativa().isBlank())throw new RegraNegocioException("A justificativa da correção é obrigatória.");
        Produto p=travarProduto(req.produtoId()); int diferenca=Math.subtractExact(req.novoSaldo(),p.getQuantidadeAtual());
        if(diferenca==0)throw new RegraNegocioException("O saldo informado já é o saldo atual; nenhuma correção necessária.");
        p.setQuantidadeAtual(req.novoSaldo()); Movimentacao m=criarMov(p,atorId,TipoMovimentacao.CORRECAO,diferenca,null,req.novoSaldo(),req.justificativa().trim());m=movimentacoes.saveAndFlush(m);
        auditoria.registrar(atorId,"PRODUTO",p.getIdProduto(),"CORRECAO_ESTOQUE","Ajuste de "+diferenca+" unidade(s). Motivo: "+req.justificativa().trim());return toResponse(m);
    }

    @Transactional(readOnly=true)
    public PageResponse<MovimentacaoResponse> historico(MovimentacaoFiltro f) {
        validarPagina(f.pagina(),f.tamanhoPagina()); if(f.inicio()!=null&&f.fim()!=null&&f.fim().isBefore(f.inicio()))throw new RegraNegocioException("A data final deve ser igual ou posterior à inicial.");
        var inicio=f.inicio()==null?null:f.inicio().atStartOfDay();var fim=f.fim()==null?null:f.fim().plusDays(1).atStartOfDay();
        Page<Movimentacao> pg=movimentacoes.pesquisarHistorico(inicio,fim,f.tipo(),f.produtoId(),f.usuarioId(),limpar(f.produto()),PageRequest.of(f.pagina(),f.tamanhoPagina(),Sort.unsorted()));
        return new PageResponse<>(pg.getContent().stream().map(this::toResponse).toList(),pg.getNumber(),pg.getSize(),pg.getTotalElements());
    }

    private Movimentacao criarMov(Produto p,Integer usuarioId,TipoMovimentacao tipo,int qtd,BigDecimal preco,int saldo,String just){return criarMov(p,obterUsuario(usuarioId),tipo,qtd,preco,saldo,just);}
    private Movimentacao criarMov(Produto p,Usuario u,TipoMovimentacao tipo,int qtd,BigDecimal preco,int saldo,String just){Movimentacao m=new Movimentacao();m.setProduto(p);m.setUsuario(u);m.setTipo(tipo);m.setQuantidade(qtd);m.setPrecoUnitario(preco);m.setSaldoApos(saldo);m.setJustificativa(just);m.setCodigoProduto(p.getCodigo());m.setNomeProduto(p.getNome());return m;}
    private Produto travarProduto(Integer id){return produtos.findByIdForUpdate(id).orElseThrow(()->new RecursoNaoEncontrado("Produto não encontrado: "+id));}
    private Usuario obterUsuario(Integer id){Usuario u=usuarios.findById(id).orElseThrow(()->new RecursoNaoEncontrado("Usuário não encontrado."));if(!Boolean.TRUE.equals(u.getAtivo()))throw new RegraNegocioException("Usuário inativo não pode movimentar estoque.");return u;}
    private int somar(int a,int b){return Math.addExact(a,b);} private int subtrair(int a,int b){return Math.subtractExact(a,b);}
    private void validarPreco(BigDecimal p){if(p==null||p.signum()<0)throw new RegraNegocioException("O preço de compra deve ser informado e não negativo.");}
    private void validarPagina(int p,int s){if(p<0||s<1||s>100)throw new RegraNegocioException("Paginação inválida (tamanho máximo: 100).");}
    private String limpar(String s){return s==null||s.isBlank()?null:s.trim();}
    private MovimentacaoResponse toResponse(Movimentacao m){return new MovimentacaoResponse(m.getIdMovimentacao(),m.getProduto()==null?null:m.getProduto().getIdProduto(),m.getUsuario().getIdUsuario(),m.getUsuario().getLogin(),m.getTipo(),m.getQuantidade(),m.getPrecoUnitario(),m.getSaldoApos(),m.getJustificativa(),m.getCodigoProduto(),m.getNomeProduto(),m.getDataHora());}
}
