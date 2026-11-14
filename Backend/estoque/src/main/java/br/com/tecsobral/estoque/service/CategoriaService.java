package br.com.tecsobral.estoque.service;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.tecsobral.estoque.dto.ServiceDtos.CategoriaRequest;
import br.com.tecsobral.estoque.dto.ServiceDtos.CategoriaResponse;
import br.com.tecsobral.estoque.entity.Categoria;
import br.com.tecsobral.estoque.exception.RegraNegocioException;
import br.com.tecsobral.estoque.exception.RecursoNaoEncontrado;
import br.com.tecsobral.estoque.repository.CategoriaRepository;

@Service @Transactional @PreAuthorize("hasAnyRole('PROPRIETARIO','FUNCIONARIO')")
public class CategoriaService {
    private final CategoriaRepository categorias; private final LogAlteracaoService auditoria;
    public CategoriaService(CategoriaRepository categorias, LogAlteracaoService auditoria) { this.categorias = categorias; this.auditoria = auditoria; }
    @Transactional(readOnly = true) public List<CategoriaResponse> listar() { return categorias.findAll().stream().map(this::toResponse).toList(); }
    public CategoriaResponse criar(CategoriaRequest req, Integer atorId) {
        String nome = req.nome().trim(); if (categorias.existsByNomeIgnoreCase(nome)) throw new RegraNegocioException("Já existe uma categoria com esse nome.");
        Categoria c = new Categoria(); c.setNome(nome); c = categorias.saveAndFlush(c);
        auditoria.registrar(atorId, "CATEGORIA", c.getIdCategoria(), "CRIACAO", "Categoria criada: " + nome); return toResponse(c);
    }
    public CategoriaResponse renomear(Integer id, CategoriaRequest req, Integer atorId) {
        Categoria c = categorias.findById(id).orElseThrow(() -> new RecursoNaoEncontrado("Categoria não encontrada."));
        String nome = req.nome().trim();
        if (categorias.existsByNomeIgnoreCase(nome) && !nome.equalsIgnoreCase(c.getNome())) throw new RegraNegocioException("Já existe uma categoria com esse nome.");
        c.setNome(nome); auditoria.registrar(atorId, "CATEGORIA", id, "ATUALIZACAO", "Categoria renomeada para: " + nome); return toResponse(c);
    }
    private CategoriaResponse toResponse(Categoria c) { return new CategoriaResponse(c.getIdCategoria(), c.getNome()); }
}
