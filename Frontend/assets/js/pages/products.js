import { esc, pageHeading, productTable } from '../components/ui.js';

const PAGE_SIZE = 20;

export async function renderProducts({ api, state }) {
  const params = new URLSearchParams({ pagina: String(state.productPage), tamanhoPagina: String(PAGE_SIZE) });
  if (state.productQuery) params.set('termo', state.productQuery);
  if (state.categoryId) params.set('categoriaId', state.categoryId);
  if (state.lowOnly) params.set('apenasAlertas', 'true');
  const [page, categories] = await Promise.all([api.request(`/produtos?${params}`), api.request('/categorias')]);
  state.categories = categories;
  const options = categories.map(category => `<option value="${category.id}" ${String(category.id) === state.categoryId ? 'selected' : ''}>${esc(category.nome)}</option>`).join('');
  const isOwner = state.user?.perfil === 'PROPRIETARIO';

  return `${pageHeading('Produtos', 'Pesquise, cadastre e mantenha os produtos do estoque.', '<button class="btn" data-action="export-csv">⇩ Exportar CSV</button><button class="btn primary" data-action="new-product">＋ Novo produto</button>')}
    <section class="table-panel"><form class="toolbar" id="product-filter-form"><div class="filter-row"><label class="field search-field"><input name="q" placeholder="Buscar por nome ou código…" value="${esc(state.productQuery)}" aria-label="Buscar por nome ou código"></label><label class="field"><select name="categoriaId" aria-label="Filtrar categoria"><option value="">Todas as categorias</option>${options}</select></label><label class="check-field"><input type="checkbox" name="estoqueBaixo" ${state.lowOnly ? 'checked' : ''}> Estoque baixo</label><button class="btn small" type="submit">Filtrar</button></div><span class="toolbar-note">${page.totalElementos} produto(s)</span></form>
    ${productTable(page.conteudo || [], isOwner)}
    <div class="pagination"><span>Página ${page.totalElementos ? page.pagina + 1 : 0} · ${page.totalElementos} registros</span><div class="pagination-actions"><button class="btn small" data-action="product-page" data-page="${Math.max(0, state.productPage - 1)}" ${state.productPage <= 0 ? 'disabled' : ''}>← Anterior</button><button class="btn small" data-action="product-page" data-page="${state.productPage + 1}" ${((state.productPage + 1) * PAGE_SIZE) >= page.totalElementos ? 'disabled' : ''}>Próxima →</button></div></div></section>`;
}

export function productDialog(product, categories) {
  const categoryOptions = categories.map(category => `<option value="${category.id}" ${product?.categoriaId === category.id ? 'selected' : ''}>${esc(category.nome)}</option>`).join('');
  return `<div class="dialog-inner"><div class="dialog-head"><div><h2>${product ? 'Editar produto' : 'Novo produto'}</h2><p>${product ? 'Atualize os dados cadastrais; o saldo é alterado em movimentações.' : 'Cadastre o produto e informe o saldo inicial.'}</p></div><button class="close-dialog" data-action="close-dialog" aria-label="Fechar">×</button></div>
    <form id="product-form" data-id="${product?.id || ''}" class="two-column-form">
      <div class="field"><label>Código *</label><input name="codigo" required maxlength="30" value="${esc(product?.codigo)}"></div><div class="field"><label>Nome *</label><input name="nome" required maxlength="120" value="${esc(product?.nome)}"></div>
      <div class="field"><label>Categoria *</label><select name="categoriaId" required><option value="">Selecione</option>${categoryOptions}</select></div><div class="field"><label>Cor</label><input name="cor" maxlength="40" value="${esc(product?.cor)}"></div>
      <div class="field"><label>Tamanho</label><input name="tamanho" maxlength="20" value="${esc(product?.tamanho)}"></div><div class="field"><label>Estoque mínimo</label><input type="number" min="0" name="estoqueMinimo" value="${product?.estoqueMinimo ?? ''}"></div>
      <div class="field"><label>Preço de compra *</label><input type="number" min="0" step="0.01" name="precoCompra" required value="${product?.precoCompra ?? ''}"></div><div class="field"><label>Preço de venda *</label><input type="number" min="0" step="0.01" name="precoVenda" required value="${product?.precoVenda ?? ''}"></div>
      ${product ? '' : '<div class="field"><label>Saldo inicial *</label><input type="number" min="0" step="1" name="quantidadeInicial" value="0" required></div>'}<div class="field ${product ? 'form-span' : ''}"><label>Outras características</label><textarea name="outrasCaracteristicas" maxlength="255">${esc(product?.outrasCaracteristicas)}</textarea></div>
      <div class="form-actions form-span"><button type="button" class="btn" data-action="close-dialog">Cancelar</button><button type="submit" class="btn primary">${product ? 'Salvar alterações' : 'Cadastrar produto'}</button></div>
    </form></div>`;
}
