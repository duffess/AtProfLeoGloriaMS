# Documentação - Entrega 3 (Autenticação JWT)

## 1. Arquitetura da Solução e Tecnologia Escolhida
Para atender à rubrica de segurança da Entrega 3, criamos um microsserviço dedicado chamado **`auth-service`**.
A tecnologia escolhida para a autenticação foi o **JWT (JSON Web Token)** utilizando o *Spring Security* de forma nativa. 
Escolhemos o JWT por ser leve, *stateless* e altamente integrado ao ecossistema do Spring Boot, permitindo uma arquitetura moderna sem a necessidade de manter containers pesados (como seria o caso do Keycloak) para avaliação.

- **`auth-service`:** Responsável exclusivo por validar credenciais e emitir tokens (`Access Token` e `Refresh Token`). Ele não possui banco de dados relacional nesta entrega, utilizando um usuário hardcoded (`admin` / `123456`) apenas para validação acadêmica do fluxo.
- **`supply-service`:** Microsserviço existente que agora atua como um *Resource Server*. Suas rotas foram protegidas por um `JwtAuthenticationFilter`, que exige o cabeçalho `Authorization: Bearer <token>`.
- **`api-gateway`:** Roteia as requisições `/api/auth/**` para o `auth-service`.

---

## 2. Como Executar os Serviços
1. Suba a infraestrutura base:
   ```bash
   docker-compose up -d
   ```
2. Inicialize o `discovery-server` e aguarde subir.
3. Inicialize o `api-gateway`.
4. Inicialize o `auth-service`.
5. Inicialize o `supply-service`.

---

## 3. Endpoints

### 3.1. Rotas Públicas
Apenas as rotas de autenticação (e rotas do Actuator para healthcheck) estão liberadas sem token.
- `POST /api/auth/login`
- `POST /api/auth/refresh`

### 3.2. Rotas Protegidas
Todas as rotas de negócio do sistema foram protegidas.
- `POST /api/supplies/checkout` (Necessita Token válido)

---

## 4. Testando o Fluxo (Exemplos Práticos)

### Passo 1: Tentativa de acesso sem autenticação
Se você tentar realizar um checkout sem token, será rejeitado (`401 Unauthorized` ou `403 Forbidden`).
```bash
curl -X POST http://localhost:8080/api/supplies/checkout \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "customerId=12345&amount=200&literage=35"
```
> **Resultado esperado:** Erro de autenticação.

### Passo 2: Realizando o Login (Obtenção do Token)
Faça a requisição com o usuário padrão.
```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d "{\"username\":\"admin\", \"password\":\"123456\"}"
```
> **Resultado esperado:** Retorno de um JSON contendo o `accessToken` e o `refreshToken`.

### Passo 3: Acessando Rota Protegida utilizando o Token
Copie o `accessToken` retornado no passo anterior e coloque no cabeçalho `Authorization`.
```bash
curl -X POST http://localhost:8080/api/supplies/checkout \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -H "Authorization: Bearer <COLE_SEU_ACCESS_TOKEN_AQUI>" \
     -d "customerId=12345&amount=200&literage=35"
```
> **Resultado esperado:** Abastecimento registrado com sucesso (HTTP 200).

### Passo 4: Obtendo novo token por meio do Refresh
Quando o `accessToken` expirar (após 5 minutos), você não precisa pedir a senha do usuário de novo. Basta enviar o `refreshToken`.
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
     -H "Content-Type: application/json" \
     -d "{\"refreshToken\":\"<COLE_SEU_REFRESH_TOKEN_AQUI>\"}"
```
> **Resultado esperado:** Retorno de um novo par de `accessToken` e `refreshToken`.
