export const esc = value => String(value ?? '').replace(/[&<>"']/g, char => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
}[char]));

export const money = value => new Intl.NumberFormat('pt-BR', {
  style: 'currency', currency: 'BRL',
}).format(Number(value || 0));

export const dateTime = value => value
  ? new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
  : '—';

export const dateOnly = value => {
  if (!value) return '—';
  const date = /^\d{4}-\d{2}-\d{2}$/.test(value)
    ? new Date(`${value}T12:00:00`)
    : new Date(value);
  return new Intl.DateTimeFormat('pt-BR').format(date);
};

export const initials = name => (name || '?').trim().split(/\s+/).slice(0, 2)
  .map(part => part[0]).join('').toUpperCase();

export function notify(message, type = 'success') {
  const root = document.querySelector('#toast-root');
  const toast = document.createElement('div');
  toast.className = `toast ${type === 'error' ? 'error' : type === 'info' ? 'info' : ''}`;
  toast.textContent = message;
  root.append(toast);
  setTimeout(() => toast.remove(), 4200);
}

export function errorMessage(error) {
  if (error?.data?.detalhes?.length) return `${error.message} ${error.data.detalhes.join(' · ')}`;
  return error?.message || 'Ocorreu um erro inesperado.';
}

export function pageHeading(title, description, actions = '') {
  return `<div class="page-heading"><div><div class="eyebrow">TecSobral · Estoque</div><h1>${title}</h1><p>${description}</p></div><div class="heading-actions">${actions}</div></div>`;
}

export function tableEmpty(text = 'Nenhum registro encontrado.') {
  return `<tr><td colspan="8"><div class="empty-state"><div class="empty-icon">⌁</div>${esc(text)}</div></td></tr>`;
}

export function stockBadge(product) {
  if (product.quantidadeAtual < 0) return `<span class="badge negative">${product.quantidadeAtual} · negativo</span>`;
  if (product.estoqueMinimo != null && product.quantidadeAtual < product.estoqueMinimo) {
    return `<span class="badge low">${product.quantidadeAtual} · baixo</span>`;
  }
  return `<span class="badge">${product.quantidadeAtual} em estoque</span>`;
}

export function productTable(products, isOwner, options = {}) {
  const rows = products.map(product => `<tr>
    <td><span class="code-pill">${esc(product.codigo)}</span></td>
    <td><div class="product-name">${esc(product.nome)}</div><div class="subtext">${esc([product.cor, product.tamanho].filter(Boolean).join(' · ') || product.outrasCaracteristicas || 'Sem detalhes adicionais')}</div></td>
    <td>${esc(product.categoria)}</td><td>${stockBadge(product)}</td><td>${money(product.precoVenda)}</td>
    <td>${product.estoqueMinimo ?? '—'}</td>
    <td class="text-right"><div class="row-actions"><button class="btn small" data-action="edit-product" data-id="${product.id}">Editar</button>${isOwner ? `<button class="btn small danger" data-action="delete-product" data-id="${product.id}">Excluir</button>` : ''}</div></td>
  </tr>`).join('');

  return `<div class="table-wrap"><table><thead><tr><th>Código</th><th>Produto</th><th>Categoria</th><th>Saldo</th><th>Preço venda</th><th>Mínimo</th><th></th></tr></thead><tbody>${rows || tableEmpty(options.empty || 'Nenhum produto encontrado.')}</tbody></table></div>`;
}

export function productOptions(products, selected = '') {
  return '<option value="">Selecione um produto</option>' + products.map(product => `<option value="${product.id}" ${String(product.id) === String(selected) ? 'selected' : ''}>${esc(product.codigo)} · ${esc(product.nome)} (saldo ${product.quantidadeAtual})</option>`).join('');
}

export function saleRow(products, index) {
  return `<div class="sale-row"><label class="field"><span>Produto ${index + 1}</span><select name="produtoId" required>${productOptions(products)}</select></label><label class="field"><span>Qtd.</span><input name="quantidade" type="number" min="1" step="1" required></label><button type="button" class="icon-remove" data-action="remove-sale-row" title="Remover">×</button></div>`;
}

export function movementTable(rows) {
  const content = rows.map(movement => `<tr><td>${dateTime(movement.dataHora)}</td>
    <td><div class="product-name">${esc(movement.nomeProduto)}</div><div class="subtext">${esc(movement.codigoProduto)}</div></td>
    <td><span class="badge ${movement.tipo === 'ENTRADA' ? 'entry' : movement.tipo === 'SAIDA' ? 'exit' : 'correction'}">${movement.tipo === 'ENTRADA' ? 'Entrada' : movement.tipo === 'SAIDA' ? 'Saída' : 'Correção'}</span></td>
    <td>${movement.quantidade}</td><td>${movement.saldoApos}</td><td>${esc(movement.usuarioLogin || movement.usuario)}</td></tr>`).join('');
  return `<div class="table-wrap"><table><thead><tr><th>Data</th><th>Produto</th><th>Tipo</th><th>Quantidade</th><th>Saldo após</th><th>Usuário</th></tr></thead><tbody>${content || tableEmpty('Ainda não há movimentações.')}</tbody></table></div>`;
}

export function openDialog(html) {
  const node = document.querySelector('#app-dialog');
  if (!node) return;
  node.innerHTML = html;
  node.showModal();
}

export function closeDialog() {
  document.querySelector('#app-dialog')?.close();
}
