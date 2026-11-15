import { ApiError, createApiClient } from './core/api.js';
import { closeDialog, errorMessage, esc, initials, notify, openDialog } from './components/ui.js';
import { renderDashboard } from './pages/dashboard.js';
import { renderProducts, productDialog } from './pages/products.js';
import { renderCategories } from './pages/categories.js';
import { allProducts, renderMovements, saleRow } from './pages/movements.js';
import { renderUsers, userDialog } from './pages/users.js';
import { renderBackups } from './pages/backups.js';
import { renderLogs } from './pages/logs.js';

const state = { auth: null, user: null, route: 'dashboard', productPage: 0, productQuery: '', lowOnly: false, categoryId: '', movementMode: 'history', historyPage: 0, historyFilters: {}, logPage: 0, categories: [], products: [], users: [] };
const app = document.querySelector('#app');
const icons = { dashboard: '◫', products: '▦', movements: '⇄', categories: '⌑', users: '♙', backups: '◷', logs: '≡' };
const labels = { dashboard: 'Visão geral', products: 'Produtos', movements: 'Movimentações', categories: 'Categorias', users: 'Usuários', backups: 'Backups', logs: 'Auditoria' };
const isOwner = () => state.user?.perfil === 'PROPRIETARIO';

function renderLogin(error = '') {
  app.innerHTML = `<main class="login-page"><section class="login-card" aria-labelledby="login-title">
    <div class="login-brand"><div class="brand-mark">T</div><div><div class="brand-title">TECSOBRAL</div><div class="brand-sub">Gestão de estoque</div></div></div>
    <h1 id="login-title">Acesse sua conta</h1><p class="intro">Entre com seu usuário para acompanhar produtos, movimentações e alertas do estoque.</p>
    ${error ? `<div class="login-error" role="alert">${esc(error)}</div>` : ''}
    <form id="login-form"><div class="field"><label for="login">Usuário</label><input id="login" name="login" autocomplete="username" required maxlength="40" autofocus></div>
    <div class="field"><label for="password">Senha</label><input id="password" name="password" type="password" autocomplete="current-password" required></div>
    <button class="btn primary" type="submit">Entrar <span aria-hidden="true">→</span></button></form>
    <div class="login-note">Suas credenciais são usadas apenas nesta aba e não ficam salvas no navegador.</div>
  </section></main>`;
}

const api = createApiClient(state, () => renderLogin('Sua autenticação expirou ou foi recusada. Entre novamente.'));

function shell(content) {
  const user = state.user || {};
  const navItems = ['dashboard', 'products', 'movements', 'categories', ...(isOwner() ? ['users', 'backups', 'logs'] : [])];
  app.innerHTML = `<div class="app-shell"><aside class="sidebar" id="sidebar">
    <div class="sidebar-brand"><div class="brand-mark">T</div><div><div class="brand-title">TECSOBRAL</div><div class="brand-sub">Controle de estoque</div></div></div>
    <div class="nav-label">Menu principal</div><nav class="nav-list" aria-label="Navegação principal">${navItems.map(key => `<button class="nav-item ${state.route === key ? 'active' : ''}" data-route="${key}"><span class="nav-icon">${icons[key]}</span>${labels[key]}</button>`).join('')}</nav>
    <div class="sidebar-bottom"><div class="profile-mini"><div class="avatar">${esc(initials(user.nome))}</div><div class="profile-meta"><div class="profile-name">${esc(user.nome)}</div><div class="profile-role">${user.perfil === 'PROPRIETARIO' ? 'Proprietário' : 'Funcionário'}</div></div><button class="logout-btn" data-action="logout" title="Sair" aria-label="Sair">↗</button></div></div>
    </aside><main class="main-area"><header class="topbar"><div style="display:flex;align-items:center;gap:12px"><button class="mobile-menu" data-action="menu" aria-label="Abrir menu">☰</button><div><div class="crumb">TecSobral <span aria-hidden="true">/</span> ${labels[state.route]}</div><div class="top-title">${labels[state.route]}</div></div></div><div class="top-actions"><span class="online-pill"><span aria-hidden="true">●</span> Conectado</span><div class="avatar">${esc(initials(user.nome))}</div></div></header><section class="content">${content}</section></main></div><dialog id="app-dialog"></dialog>`;
}

async function loadRoute() {
  if (!state.auth) return renderLogin();
  const renderers = { dashboard: renderDashboard, products: renderProducts, movements: renderMovements, categories: renderCategories, users: renderUsers, backups: renderBackups, logs: renderLogs };
  if (['users', 'backups', 'logs'].includes(state.route) && !isOwner()) state.route = 'dashboard';
  const render = renderers[state.route] || renderDashboard;
  if (!renderers[state.route]) state.route = 'dashboard';
  try {
    shell(await render({ api, state }));
    document.querySelector('#sidebar')?.classList.remove('open');
  } catch (error) {
    if (error.status === 401) return;
    shell(`<div class="page-heading"><div><div class="eyebrow">Ops</div><h1>Não foi possível carregar</h1><p>${esc(errorMessage(error))}</p></div><button class="btn" data-action="retry">Tentar novamente</button></div>`);
  }
}

function formPayload(form) { return Object.fromEntries(new FormData(form)); }

async function submitSale(form) {
  const byProduct = new Map();
  for (const row of form.querySelectorAll('.sale-row')) {
    const produtoId = Number(row.querySelector('[name="produtoId"]').value);
    const quantidade = Number(row.querySelector('[name="quantidade"]').value);
    if (!produtoId || !quantidade) throw new ApiError('Selecione o produto e a quantidade de cada item.', 400);
    byProduct.set(produtoId, (byProduct.get(produtoId) || 0) + quantidade);
  }
  const itens = [...byProduct].map(([produtoId, quantidade]) => ({ produtoId, quantidade }));
  const send = confirmarSaldoNegativo => api.request('/movimentacoes/vendas', { method: 'POST', body: JSON.stringify({ itens, confirmarSaldoNegativo }) });
  try {
    await send(false); notify('Venda registrada.'); state.movementMode = 'history'; await loadRoute();
  } catch (error) {
    if (error.status === 409 && error.data?.codigo === 'CONFIRMACAO_SALDO_NEGATIVO') {
      const details = (error.data.itens || []).map(item => `${item.nome}: saldo ${item.saldoAtual}, venda ${item.quantidadeSaida}, ficará ${item.saldoDepois}`).join('\n');
      if (window.confirm(`${error.data.mensagem || 'A venda deixará saldo negativo.'}\n\n${details}\n\nDeseja confirmar a venda?`)) {
        await send(true); notify('Venda confirmada e registrada com saldo negativo.'); state.movementMode = 'history'; await loadRoute();
      }
      return;
    }
    throw error;
  }
}

async function handleSubmit(event) {
  const form = event.target;
  if (form.id === 'login-form') {
    event.preventDefault(); const data = new FormData(form); const button = form.querySelector('button[type="submit"]');
    button.disabled = true; button.textContent = 'Entrando…';
    try { await api.login(String(data.get('login')).trim(), data.get('password')); state.route = 'dashboard'; await loadRoute(); }
    catch (error) { renderLogin(error.status === 401 ? 'Usuário ou senha inválidos. Confira as credenciais da conta ativa.' : errorMessage(error)); }
    finally { button.disabled = false; }
    return;
  }
  event.preventDefault();
  try {
    if (form.id === 'product-filter-form') { const d = new FormData(form); state.productQuery = String(d.get('q') || '').trim(); state.categoryId = String(d.get('categoriaId') || ''); state.lowOnly = d.get('estoqueBaixo') === 'on'; state.productPage = 0; return loadRoute(); }
    if (form.id === 'product-form') {
      const d = new FormData(form), id = form.dataset.id;
      const payload = { codigo: d.get('codigo').trim(), nome: d.get('nome').trim(), categoriaId: Number(d.get('categoriaId')), cor: d.get('cor') || null, tamanho: d.get('tamanho') || null, outrasCaracteristicas: d.get('outrasCaracteristicas') || null, precoCompra: Number(d.get('precoCompra')), precoVenda: Number(d.get('precoVenda')), estoqueMinimo: d.get('estoqueMinimo') === '' ? null : Number(d.get('estoqueMinimo')) };
      if (id) await api.request(`/produtos/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await api.request('/produtos', { method: 'POST', body: JSON.stringify({ ...payload, quantidadeInicial: Number(d.get('quantidadeInicial')) }) });
      closeDialog(); notify(id ? 'Produto atualizado.' : 'Produto cadastrado.'); state.route = 'products'; return loadRoute();
    }
    if (form.id === 'category-form') { const d = formPayload(form); await api.request('/categorias', { method: 'POST', body: JSON.stringify({ nome: d.nome.trim() }) }); notify('Categoria cadastrada.'); return loadRoute(); }
    if (form.id === 'entry-form') { const d = formPayload(form); await api.request('/movimentacoes/entradas', { method: 'POST', body: JSON.stringify({ produtoId: Number(d.produtoId), quantidade: Number(d.quantidade), precoCompra: Number(d.precoCompra) }) }); notify('Entrada registrada.'); state.movementMode = 'history'; return loadRoute(); }
    if (form.id === 'correction-form') { const d = formPayload(form); await api.request('/movimentacoes/correcoes', { method: 'POST', body: JSON.stringify({ produtoId: Number(d.produtoId), novoSaldo: Number(d.novoSaldo), justificativa: d.justificativa.trim() }) }); notify('Correção registrada.'); state.movementMode = 'history'; return loadRoute(); }
    if (form.id === 'sale-form') return await submitSale(form);
    if (form.id === 'history-filter') { const d = formPayload(form); state.historyPage = 0; state.historyFilters = Object.fromEntries(['produtoId', 'tipo', 'inicio', 'fim'].filter(key => d[key]).map(key => [key, d[key]])); return loadRoute(); }
    if (form.id === 'user-form') {
      const d = formPayload(form), id = form.dataset.id, payload = { nome: d.nome.trim(), login: d.login.trim(), perfil: d.perfil };
      if (id) await api.request(`/usuarios/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await api.request('/usuarios', { method: 'POST', body: JSON.stringify({ ...payload, senha: d.senha }) });
      closeDialog(); notify(id ? 'Usuário atualizado.' : 'Usuário criado.'); return loadRoute();
    }
  } catch (error) { if (error.status !== 401 && error.data?.codigo !== 'CONFIRMACAO_SALDO_NEGATIVO') notify(errorMessage(error), 'error'); }
}

async function handleClick(event) {
  const route = event.target.closest('[data-route]');
  if (route) { if (route.dataset.lowOnly === 'true') { state.lowOnly = true; state.productPage = 0; } if (route.dataset.mode) state.movementMode = route.dataset.mode; state.route = route.dataset.route; return loadRoute(); }
  const button = event.target.closest('[data-action]'); if (!button) return;
  const action = button.dataset.action;
  try {
    if (action === 'logout') { api.logout(); state.route = 'dashboard'; return renderLogin(); }
    if (action === 'retry') return loadRoute();
    if (action === 'menu') return document.querySelector('#sidebar')?.classList.toggle('open');
    if (action === 'close-dialog') return closeDialog();
    if (action === 'new-product') { state.categories = await api.request('/categorias'); return openDialog(productDialog(null, state.categories)); }
    if (action === 'edit-product') { state.categories = await api.request('/categorias'); const product = await api.request(`/produtos/${button.dataset.id}`); return openDialog(productDialog(product, state.categories)); }
    if (action === 'delete-product') { if (!confirm('Excluir este produto? O histórico das movimentações será preservado.')) return; await api.request(`/produtos/${button.dataset.id}?confirmar=true`, { method: 'DELETE' }); notify('Produto excluído; histórico mantido.'); return loadRoute(); }
    if (action === 'product-page') { state.productPage = Number(button.dataset.page); return loadRoute(); }
    if (action === 'export-csv') { await api.downloadCsv(); return notify('Arquivo CSV baixado.'); }
    if (action === 'rename-category') { const value = prompt('Novo nome da categoria:', button.dataset.name); if (value === null || !value.trim()) return; await api.request(`/categorias/${button.dataset.id}`, { method: 'PUT', body: JSON.stringify({ nome: value.trim() }) }); notify('Categoria renomeada.'); return loadRoute(); }
    if (action === 'movement-mode') { state.movementMode = button.dataset.mode || 'history'; state.route = 'movements'; state.historyPage = 0; return loadRoute(); }
    if (action === 'history-page') { state.historyPage = Number(button.dataset.page); return loadRoute(); }
    if (action === 'log-page') { state.logPage = Number(button.dataset.page); return loadRoute(); }
    if (action === 'clear-history-filters') { state.historyFilters = {}; state.historyPage = 0; return loadRoute(); }
    if (action === 'add-sale-row') { if (!state.products.length) state.products = await allProducts(api); document.querySelector('#sale-items')?.insertAdjacentHTML('beforeend', saleRow(state.products, document.querySelectorAll('.sale-row').length)); return; }
    if (action === 'remove-sale-row') { const rows = document.querySelectorAll('.sale-row'); if (rows.length <= 1) return notify('A venda precisa ter ao menos um item.', 'info'); button.closest('.sale-row').remove(); document.querySelectorAll('#sale-items .sale-row').forEach((row, index) => { row.querySelector('.field span').textContent = `Produto ${index + 1}`; }); return; }
    if (action === 'new-user') return openDialog(userDialog());
    if (action === 'edit-user') { const user = state.users.find(item => item.id === Number(button.dataset.id)); if (user) return openDialog(userDialog(user)); return; }
    if (action === 'toggle-user') { const active = button.dataset.active === 'true'; if (!confirm(`${active ? 'Desativar' : 'Ativar'} este usuário?`)) return; await api.request(`/usuarios/${button.dataset.id}/status`, { method: 'PUT', body: JSON.stringify({ ativo: !active }) }); notify(`Usuário ${active ? 'desativado' : 'ativado'}.`); return loadRoute(); }
    if (action === 'reset-user-password') { const value = prompt(`Nova senha para ${button.dataset.login} (mínimo 12 caracteres):`); if (value === null) return; if (value.length < 12) return notify('A senha deve ter pelo menos 12 caracteres.', 'error'); await api.request(`/usuarios/${button.dataset.id}/senha`, { method: 'PUT', body: JSON.stringify({ novaSenha: value }) }); return notify('Senha redefinida.'); }
    if (action === 'create-backup') { button.disabled = true; const result = await api.request('/backups', { method: 'POST' }); notify(result.status === 'SUCESSO' ? 'Backup criado.' : 'Backup falhou. Confira a configuração do PostgreSQL.'); return loadRoute(); }
    if (action === 'restore-backup') { if (!confirm(`Restaurar o arquivo ${button.dataset.file}? Os dados atuais serão substituídos; o sistema criará uma cópia preventiva.`)) return; await api.request('/backups/restauracao', { method: 'POST', body: JSON.stringify({ arquivo: button.dataset.file }) }); notify('Backup restaurado.'); return loadRoute(); }
  } catch (error) { if (error.status !== 401) notify(errorMessage(error), 'error'); }
}

document.addEventListener('submit', handleSubmit);
document.addEventListener('click', event => { handleClick(event).catch(error => { if (error.status !== 401) notify(errorMessage(error), 'error'); }); });
document.addEventListener('keydown', event => { if (event.key === 'Escape') closeDialog(); });
renderLogin();
