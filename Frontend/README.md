# Frontend TecSobral

Frontend web independente, baseado no projeto `TecSobral.rar` e conectado à API Spring Boot desse projeto.

## Executar localmente

1. Inicie o PostgreSQL e o backend em `Backend/estoque`.
2. Abra um terminal nesta pasta `TecSobral-Frontend`.
3. Execute `python -m http.server 5173`.
4. Acesse `http://localhost:5173` e entre com o login e a senha cadastrados no banco.

O backend deve estar disponível em `http://localhost:8080`. A configuração CORS do arquivo permite por padrão o frontend em `http://localhost:5173`. Se alterar a porta ou o endereço, ajuste `app.cors.origins` no `application.properties` do backend. O endereço da API pode ser alterado na constante `API_BASE` em `assets/js/core/api.js`.

O login é validado por `POST /api/auth/login`; as rotas protegidas usam HTTP Basic. Para manter a sessão ao atualizar a página, o cabeçalho de autenticação fica no `sessionStorage` até sair ou fechar a aba. Esse valor é reversível e acessível ao JavaScript da página; use apenas em um dispositivo confiável e mantenha o frontend protegido contra XSS.

## Publicar no Vercel e Render

- Publique `Backend/estoque` como Web Service Docker no Render, usando essa pasta como **Root Directory**. O `Dockerfile` está nessa pasta.
- Configure no Render os segredos `DB_URL`, `DB_USUARIO` e `DB_SENHA` para o banco Supabase. Configure também `APP_CORS_ORIGINS` com a origem do site Vercel e, se necessário, `http://localhost:5173`.
- Depois que o Render fornecer a URL HTTPS do backend, atualize `Frontend/api-config.js` para `https://SUA-API.onrender.com/api`, faça commit e redeploy do frontend no Vercel.
- O backup local usa o disco do serviço; em instâncias Render com sistema de arquivos efêmero, os arquivos podem sumir ao reiniciar. Configure armazenamento persistente antes de depender desses backups.

## Estrutura

- `index.html`: página inicial.
- `assets/css/app.css`: estilos responsivos.
- `assets/js/app.js`: navegação e eventos da aplicação.
- `assets/js/core/api.js`: comunicação, autenticação e exportação CSV.
- `assets/js/components/ui.js`: elementos e formatação compartilhados.
- `assets/js/pages`: telas de dashboard, produtos, categorias, movimentações, usuários, backups e auditoria.

O projeto usa HTML, CSS e JavaScript nativo; não requer `npm install` ou compilação do frontend.
