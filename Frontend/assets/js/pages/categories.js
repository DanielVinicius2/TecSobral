import { esc, pageHeading, tableEmpty } from '../components/ui.js';

export async function renderCategories({ api, state }) {
  const categories = await api.request('/categorias');
  state.categories = categories;
  return `${pageHeading('Categorias', 'Organize os produtos com categorias claras.', `<span class="badge">${categories.length} categoria(s)</span>`)}
    <div class="grid-2"><section class="panel"><div class="panel-head"><div><h2>Nova categoria</h2><p>Use nomes curtos e fáceis de identificar.</p></div><span class="stat-icon">＋</span></div><form id="category-form" class="inline-form"><label class="field"><span>Nome da categoria</span><input name="nome" maxlength="60" required placeholder="Ex.: Acessórios"></label><button type="submit" class="btn primary">Cadastrar</button></form></section>
    <section class="table-panel"><div class="toolbar"><strong style="font:700 14px Manrope">Categorias cadastradas</strong><span class="toolbar-note">${categories.length} no total</span></div><div class="table-wrap"><table><thead><tr><th>Nome</th><th class="text-right">Ações</th></tr></thead><tbody>${categories.map(category => `<tr><td><div class="product-name">${esc(category.nome)}</div><div class="subtext">ID ${category.id}</div></td><td><div class="row-actions"><button class="btn small" data-action="rename-category" data-id="${category.id}" data-name="${esc(category.nome)}">Renomear</button></div></td></tr>`).join('') || tableEmpty('Nenhuma categoria cadastrada.')}</tbody></table></div></section></div>`;
}
