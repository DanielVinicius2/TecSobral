import { esc, movementTable, pageHeading, productOptions, saleRow } from '../components/ui.js';

const HISTORY_PAGE_SIZE = 12;

export async function allProducts(api) {
  const products = [];
  let page = 0;
  let total = Infinity;
  while (products.length < total && page < 50) {
    const result = await api.request(`/produtos?pagina=${page}&tamanhoPagina=100`);
    products.push(...(result.conteudo || []));
    total = result.totalElementos || 0;
    page += 1;
  }
  return products;
}

export async function renderMovements({ api, state }) {
  const historyParams = new URLSearchParams({ pagina: String(state.historyPage), tamanhoPagina: String(HISTORY_PAGE_SIZE) });
  Object.entries(state.historyFilters).forEach(([key, value]) => { if (value) historyParams.set(key, value); });
  const [products, history, categories] = await Promise.all([
    allProducts(api), api.request(`/movimentacoes?${historyParams}`), api.request('/categorias'),
  ]);
  state.products = products;
  state.categories = categories;

  const modes = [['history', 'Histórico'], ['entry', 'Entrada'], ['sale', 'Venda'], ['correction', 'Correção']];
  const tabs = modes.map(([mode, label]) => `<button class="btn ${state.movementMode === mode ? 'primary' : ''}" data-action="movement-mode" data-mode="${mode}">${label}</button>`).join('');
  let form = '';

  if (state.movementMode === 'entry') form = `<section class="panel form-card"><h2>Registrar entrada</h2><p>Atualiza o saldo e o preço de compra atual.</p><form id="entry-form" class="form-stack"><label class="field"><span>Produto</span><select name="produtoId" required>${productOptions(products)}</select></label><label class="field"><span>Quantidade</span><input type="number" name="quantidade" min="1" step="1" required></label><label class="field"><span>Preço de compra unitário</span><input type="number" name="precoCompra" min="0" step="0.01" required></label><button class="btn primary" type="submit">Registrar entrada</button></form></section>`;
  if (state.movementMode === 'correction') form = `<section class="panel form-card"><h2>Corrigir saldo</h2><p>A correção registra a diferença e exige justificativa.</p><form id="correction-form" class="form-stack"><label class="field"><span>Produto</span><select name="produtoId" required>${productOptions(products)}</select></label><label class="field"><span>Novo saldo contado</span><input type="number" name="novoSaldo" min="0" step="1" required></label><label class="field"><span>Justificativa</span><textarea name="justificativa" maxlength="255" required placeholder="Ex.: Contagem física do estoque"></textarea></label><button class="btn primary" type="submit">Salvar correção</button></form></section>`;
  if (state.movementMode === 'sale') form = `<section class="panel form-card"><h2>Registrar venda</h2><p>Adicione os produtos vendidos. Se o saldo ficar negativo, você verá uma confirmação antes de concluir.</p><form id="sale-form" class="form-stack"><div id="sale-items" class="sale-items">${saleRow(products, 0)}</div><button type="button" class="btn ghost" data-action="add-sale-row">＋ Adicionar produto</button><button class="btn primary" type="submit">Revisar e registrar venda</button></form></section>`;
  if (state.movementMode === 'history') form = `<section class="panel form-card"><h2>Histórico de estoque</h2><p>Filtre por período, tipo ou produto.</p><form id="history-filter" class="form-stack"><label class="field"><span>Produto</span><select name="produtoId"><option value="">Todos os produtos</option>${products.map(product => `<option value="${product.id}" ${String(state.historyFilters.produtoId || '') === String(product.id) ? 'selected' : ''}>${esc(product.codigo)} · ${esc(product.nome)}</option>`).join('')}</select></label><label class="field"><span>Tipo</span><select name="tipo"><option value="">Todos</option><option value="ENTRADA" ${state.historyFilters.tipo === 'ENTRADA' ? 'selected' : ''}>Entrada</option><option value="SAIDA" ${state.historyFilters.tipo === 'SAIDA' ? 'selected' : ''}>Saída</option><option value="CORRECAO" ${state.historyFilters.tipo === 'CORRECAO' ? 'selected' : ''}>Correção</option></select></label><div class="two-column-form"><label class="field"><span>De</span><input type="date" name="inicio" value="${esc(state.historyFilters.inicio)}"></label><label class="field"><span>Até</span><input type="date" name="fim" value="${esc(state.historyFilters.fim)}"></label></div><button class="btn" type="submit">Aplicar filtros</button><button class="btn ghost" type="button" data-action="clear-history-filters">Limpar filtros</button></form></section>`;

  return `${pageHeading('Movimentações', 'Registre entradas, vendas e correções com histórico auditável.', tabs)}
    <div class="movement-layout">${form}<section class="table-panel"><div class="toolbar"><div><strong style="font:700 14px Manrope">${state.movementMode === 'history' ? 'Movimentações recentes' : 'Últimos registros'}</strong><div class="subtext">As movimentações não podem ser editadas ou apagadas.</div></div><span class="toolbar-note">${history.totalElementos} registro(s)</span></div>${movementTable(history.conteudo || [])}<div class="pagination"><span>Página ${history.totalElementos ? history.pagina + 1 : 0}</span><div class="pagination-actions"><button class="btn small" data-action="history-page" data-page="${Math.max(0, state.historyPage - 1)}" ${state.historyPage === 0 ? 'disabled' : ''}>←</button><button class="btn small" data-action="history-page" data-page="${state.historyPage + 1}" ${(state.historyPage + 1) * HISTORY_PAGE_SIZE >= history.totalElementos ? 'disabled' : ''}>→</button></div></div></section></div>`;
}

export { saleRow };
