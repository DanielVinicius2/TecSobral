# Frontend TecSobral

Frontend web independente, baseado no projeto `TecSobral.rar` e conectado à API Spring Boot desse projeto.

## Executar localmente

1. Inicie o PostgreSQL e o backend em `Backend/estoque`.
2. Abra um terminal nesta pasta `TecSobral-Frontend`.
3. Execute `python -m http.server 5173`.
4. Acesse `http://localhost:5173` e entre com o login e a senha cadastrados no banco.

O backend deve estar disponível em `http://localhost:8080`. A configuração CORS do arquivo permite por padrão o frontend em `http://localhost:5173`. Se alterar a porta ou o endereço, ajuste `app.cors.origins` no `application.properties` do backend. O endereço da API pode ser alterado na constante `API_BASE` em `assets/js/core/api.js`.

O login é validado por `POST /api/auth/login`; as rotas protegidas usam HTTP Basic. Para manter a sessão ao atualizar a página, o cabeçalho de autenticação fica no `sessionStorage` até sair ou fechar a aba. Esse valor é reversível e acessível ao JavaScript da página; use apenas em um dispositivo confiável e mantenha o frontend protegido contra XSS.

## Estrutura

- `index.html`: página inicial.
- `assets/css/app.css`: estilos responsivos.
- `assets/js/app.js`: navegação e eventos da aplicação.
- `assets/js/core/api.js`: comunicação, autenticação e exportação CSV.
- `assets/js/components/ui.js`: elementos e formatação compartilhados.
- `assets/js/pages`: telas de dashboard, produtos, categorias, movimentações, usuários, backups e auditoria.

O projeto usa HTML, CSS e JavaScript nativo; não requer `npm install` ou compilação do frontend.
