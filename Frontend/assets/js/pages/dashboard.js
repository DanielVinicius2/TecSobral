import { esc, pageHeading, productTable } from '../components/ui.js';

export async function renderDashboard({ api, state }) {
  const [dashboard, products, categories] = await Promise.all([
    api.request('/dashboard'),
    api.request('/produtos?pagina=0&tamanhoPagina=8'),
    api.request('/categorias'),
  ]);
  const alerts = (dashboard.alertas || []).slice(0, 5);
  const isOwner = state.user?.perfil === 'PROPRIETARIO';

  return `${pageHeading('Visão geral', 'Acompanhe o estoque e as atividades da sua loja.', '<button class="btn" data-route="movements">＋ Registrar movimento</button><button class="btn primary" data-action="new-product">＋ Novo produto</button>')}
    <div class="cards">
      <article class="stat-card"><div class="stat-top">Produtos cadastrados <span class="stat-icon">▦</span></div><div class="stat-value">${dashboard.totalProdutos ?? 0}</div><div class="stat-foot">Itens no catálogo</div></article>
      <article class="stat-card warn"><div class="stat-top">Estoque baixo <span class="stat-icon">⌁</span></div><div class="stat-value">${dashboard.produtosEstoqueBaixo ?? 0}</div><div class="stat-foot">Abaixo do mínimo definido</div></article>
      <article class="stat-card alert"><div class="stat-top">Saldo negativo <span class="stat-icon">↘</span></div><div class="stat-value">${dashboard.produtosSaldoNegativo ?? 0}</div><div class="stat-foot">Precisam de conferência</div></article>
      <article class="stat-card"><div class="stat-top">Categorias <span class="stat-icon">⌑</span></div><div class="stat-value">${categories.length}</div><div class="stat-foot">Grupos do catálogo</div></article>
    </div>
    <div class="grid-2">
      <section class="panel"><div class="panel-head"><div><h2>Produtos recentes</h2><p>Visão rápida do catálogo</p></div><button class="link-btn" data-route="products">Ver todos →</button></div>${productTable((products.conteudo || []).slice(0, 5), isOwner, { empty: 'Cadastre o primeiro produto para começar.' })}</section>
      <section class="panel"><div class="panel-head"><div><h2>Alertas de estoque</h2><p>Saldo negativo ou menor que o mínimo</p></div><button class="link-btn" data-route="products" data-low-only="true">Ver lista →</button></div><div class="alert-list">${alerts.length ? alerts.map(product => `<div class="alert-row"><div><div class="product-name">${esc(product.nome)}</div><div class="subtext">${esc(product.codigo)} · ${esc(product.categoria)}</div></div><div class="alert-qty">${product.quantidadeAtual}</div></div>`).join('') : '<div class="empty-state"><div class="empty-icon">✓</div>Nenhum alerta por enquanto.</div>'}</div></section>
    </div>
    <section class="panel mt-16"><div class="panel-head"><div><h2>Ações rápidas</h2><p>Atalhos para as tarefas mais comuns</p></div></div><div class="quick-list" style="grid-template-columns:repeat(auto-fit,minmax(180px,1fr))"><button class="quick-action" data-action="movement-mode" data-mode="entry"><span>＋</span>Registrar entrada</button><button class="quick-action" data-action="movement-mode" data-mode="sale"><span>↗</span>Registrar venda</button><button class="quick-action" data-action="movement-mode" data-mode="correction"><span>⌁</span>Corrigir saldo</button><button class="quick-action" data-route="categories"><span>⌑</span>Gerenciar categorias</button></div></section>`;
}
