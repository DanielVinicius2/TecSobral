package br.com.tecsobral.estoque.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.tecsobral.estoque.dto.ServiceDtos.LogResponse;
import br.com.tecsobral.estoque.entity.LogAlteracao;
import br.com.tecsobral.estoque.entity.Usuario;
import br.com.tecsobral.estoque.exception.RecursoNaoEncontrado;
import br.com.tecsobral.estoque.repository.LogAlteracaoRepository;
import br.com.tecsobral.estoque.repository.UsuarioRepository;

@Service
public class LogAlteracaoService {
	private final LogAlteracaoRepository logs;
	private final UsuarioRepository usuarios;

	public LogAlteracaoService(LogAlteracaoRepository logs, UsuarioRepository usuarios) {
		this.logs = logs;
		this.usuarios = usuarios;
	}

	@Transactional
	public LogAlteracao registrar(Integer usuarioId, String entidade, Integer registroId,
			String acao, String detalhes) {
		if (entidade == null || entidade.isBlank() || acao == null || acao.isBlank() || registroId == null) {
			throw new IllegalArgumentException("Entidade, registro e ação são obrigatórios para auditoria.");
		}
		Usuario usuario = usuarios.findById(usuarioId)
				.orElseThrow(() -> new RecursoNaoEncontrado("Usuário responsável pela alteração não encontrado."));
		if (!Boolean.TRUE.equals(usuario.getAtivo())) {
			throw new IllegalArgumentException("Usuário desativado não pode registrar alterações.");
		}
		LogAlteracao log = new LogAlteracao();
		log.setUsuario(usuario);
		log.setEntidade(entidade.trim().toUpperCase(java.util.Locale.ROOT));
		log.setIdRegistro(registroId);
		log.setAcao(acao.trim().toUpperCase(java.util.Locale.ROOT));
		log.setDetalhes(limitar(detalhes, 500));
		return logs.save(log);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public Page<LogResponse> listar(int pagina, int tamanho) {
		validarPagina(pagina, tamanho);
		Page<LogAlteracao> resultado = logs.findAll(PageRequest.of(pagina, tamanho,
				Sort.by(Sort.Order.desc("dataHora"), Sort.Order.desc("idLog"))));
		return resultado.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public List<LogResponse> porUsuario(Integer usuarioId) {
		if (!usuarios.existsById(usuarioId)) throw new RecursoNaoEncontrado("Usuário não encontrado.");
		return logs.findByUsuarioIdUsuarioOrderByDataHoraDesc(usuarioId).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public List<LogResponse> porRegistro(String entidade, Integer registroId) {
		return logs.findByEntidadeAndIdRegistroOrderByDataHoraDesc(entidade.trim().toUpperCase(java.util.Locale.ROOT), registroId)
				.stream().map(this::toResponse).toList();
	}

	private LogResponse toResponse(LogAlteracao log) {
		return new LogResponse(log.getIdLog(), log.getUsuario().getIdUsuario(), log.getUsuario().getLogin(),
				log.getEntidade(), log.getIdRegistro(), log.getAcao(), log.getDetalhes(), log.getDataHora());
	}

	private String limitar(String value, int max) {
		if (value == null || value.isBlank()) return null;
		String normalized = value.trim();
		return normalized.length() <= max ? normalized : normalized.substring(0, max);
	}

	private void validarPagina(int pagina, int tamanho) {
		if (pagina < 0 || tamanho < 1 || tamanho > 100) {
			throw new IllegalArgumentException("A página deve ser não negativa e o tamanho deve ficar entre 1 e 100.");
		}
	}
}
