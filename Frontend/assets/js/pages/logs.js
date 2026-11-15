import { dateTime, esc, pageHeading, tableEmpty } from '../components/ui.js';

const PAGE_SIZE = 20;

export async function renderLogs({ api, state }) {
  const result = await api.request(`/logs?pagina=${state.logPage}&tamanhoPagina=${PAGE_SIZE}`);
  const logs = result.conteudo || [];
  return `${pageHeading('Auditoria', 'Consulte as ações registradas no sistema.', '')}
    <section class="table-panel">
      <div class="toolbar"><strong style="font:700 14px Manrope">Registro de auditoria</strong><span class="toolbar-note">${result.totalElementos} evento(s)</span></div>
      <div class="table-wrap"><table><thead><tr><th>Data</th><th>Usuário</th><th>Entidade</th><th>Registro</th><th>Ação</th><th>Detalhes</th></tr></thead>
      <tbody>${logs.map(log => `<tr><td>${dateTime(log.dataHora)}</td><td>${esc(log.usuarioLogin || 'Sistema')}</td><td>${esc(log.entidade)}</td><td>${log.registroId ?? '—'}</td><td><span class="badge">${esc(log.acao)}</span></td><td>${esc(log.detalhes || '—')}</td></tr>`).join('') || tableEmpty('Nenhum evento registrado.')}</tbody></table></div>
      <div class="pagination"><span>Página ${result.totalElementos ? result.pagina + 1 : 0}</span><div class="pagination-actions"><button class="btn small" data-action="log-page" data-page="${Math.max(0, state.logPage - 1)}" ${state.logPage <= 0 ? 'disabled' : ''}>← Anterior</button><button class="btn small" data-action="log-page" data-page="${state.logPage + 1}" ${(state.logPage + 1) * PAGE_SIZE >= result.totalElementos ? 'disabled' : ''}>Próxima →</button></div></div>
    </section>`;
}
