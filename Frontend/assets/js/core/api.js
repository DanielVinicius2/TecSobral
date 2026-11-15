const API_BASE = 'http://localhost:8080/api';
const SESSION_KEY = 'tecsobral.session.v1';

export class ApiError extends Error {
  constructor(message, status, data = null) {
    super(message);
    this.status = status;
    this.data = data;
  }
}

function basicHeader(login, password) {
  const bytes = new TextEncoder().encode(`${login}:${password}`);
  let binary = '';
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return `Basic ${btoa(binary)}`;
}

function restoreSession(state) {
  const saved = sessionStorage.getItem(SESSION_KEY);
  if (!saved) return;

  let session;
  try {
    session = JSON.parse(saved);
  } catch (error) {
    if (!(error instanceof SyntaxError)) throw error;
    sessionStorage.removeItem(SESSION_KEY);
    return;
  }

  if (typeof session?.header !== 'string'
      || !session.header.startsWith('Basic ')
      || !session.user
      || typeof session.user.id !== 'number'
      || typeof session.user.nome !== 'string'
      || typeof session.user.login !== 'string'
      || !['PROPRIETARIO', 'FUNCIONARIO'].includes(session.user.perfil)) {
    sessionStorage.removeItem(SESSION_KEY);
    return;
  }

  state.auth = { header: session.header };
  state.user = session.user;
}

async function parseResponse(response) {
  if (response.status === 204) return null;
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    try { return await response.json(); } catch { return null; }
  }
  try { return await response.text(); } catch { return null; }
}

export function createApiClient(state, onUnauthorized) {
  restoreSession(state);

  function clearSession() {
    state.auth = null;
    state.user = null;
    sessionStorage.removeItem(SESSION_KEY);
  }

  async function rawRequest(path, options = {}, auth = state.auth, readBody = true) {
    const headers = new Headers(options.headers || {});
    if (auth?.header) headers.set('Authorization', auth.header);
    if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
    let response;
    try {
      response = await fetch(`${API_BASE}${path}`, { ...options, headers, mode: 'cors' });
    } catch {
      throw new ApiError('Não foi possível conectar à API. Confira se o backend está rodando em http://localhost:8080.', 0);
    }

    const data = readBody ? await parseResponse(response) : null;
    if (response.status === 401) {
      if (state.auth && auth === state.auth) {
        clearSession();
        onUnauthorized();
      }
      throw new ApiError(data?.mensagem || 'Login ou senha inválidos.', 401, data);
    }
    if (!response.ok) {
      const message = data?.mensagem || data?.message || data?.error
        || (typeof data === 'string' && data) || `A solicitação falhou (${response.status}).`;
      throw new ApiError(message, response.status, data);
    }
    return { data, response };
  }

  async function request(path, options = {}) {
    if (!state.auth) throw new ApiError('Faça login para continuar.', 401);
    return (await rawRequest(path, options)).data;
  }

  async function login(login, password) {
    const credentials = { login, password, header: basicHeader(login, password) };
    const { data: user } = await rawRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ login, senha: password }),
    }, null);
    sessionStorage.setItem(SESSION_KEY, JSON.stringify({
      header: credentials.header,
      user,
    }));
    state.auth = credentials;
    state.user = user;
    return user;
  }

  async function downloadCsv() {
    const { response } = await rawRequest('/produtos/exportacao/csv', {}, state.auth, false);
    const objectUrl = URL.createObjectURL(await response.blob());
    const link = document.createElement('a');
    link.href = objectUrl;
    link.download = 'produtos.csv';
    link.click();
    URL.revokeObjectURL(objectUrl);
  }

  function logout() {
    clearSession();
  }

  return { request, login, logout, downloadCsv };
}
