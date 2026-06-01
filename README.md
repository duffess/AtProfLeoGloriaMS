# GasStation Hub - Sistema Inteligente para Postos de Combustível

## Integrantes
- [Seu Nome] - [Sua Turma]

## Descrição do Projeto
O sistema visa resolver a gestão de abastecimentos em postos de combustível e a integração automática com programas de fidelidade. A arquitetura garante que o posto continue operando o registro de vendas (abastecimentos) mesmo se o sistema de fidelidade estiver temporariamente indisponível.

## Arquitetura
A aplicação adota uma arquitetura baseada em microservices:
- **Discovery Server (Eureka):** Responsável por registrar dinamicamente os serviços.
- **API Gateway:** Ponto único de entrada e roteamento.
- **Microservices de Negócio:** Responsáveis por domínios específicos.
- **Bancos de Dados:** Cada microservice possui seu próprio banco de dados lógico e isolado (Poliglotismo de persistência com Relacional e NoSQL).
- **Resiliência:** Uso de Circuit Breaker (Resilience4J) para garantir o funcionamento do abastecimento em caso de falha na fidelidade.

## Microservices

| Serviço | Responsabilidade | Porta | Banco |
|---|---|---|---|
| discovery-server | Service Registry | 8761 | N/A |
| api-gateway | Roteamento | 8080 | N/A |
| supply-service | Registro de abastecimentos | 8081 | PostgreSQL |
| loyalty-service | Gestão de clientes e pontos | 8082 | MongoDB |

## Como executar

1. **Subir os Bancos de Dados (Docker):**
   Na raiz do projeto, execute o comando para iniciar o PostgreSQL e MongoDB:
   ```bash
   docker-compose up -d
   ```

2. **Subir os Microservices:**
   Abra 4 terminais diferentes. Navegue até a pasta de cada microservice e inicie-os na seguinte ordem usando Maven ou executando pelas suas IDEs:
   1. `cd discovery-server` e depois `./mvnw spring-boot:run` (Aguarde subir)
   2. `cd api-gateway` e depois `./mvnw spring-boot:run`
   3. `cd loyalty-service` e depois `./mvnw spring-boot:run`
   4. `cd supply-service` e depois `./mvnw spring-boot:run`

## Discovery Server
- **Acesso:** [http://localhost:8761](http://localhost:8761)
- **Serviços Registrados:** `API-GATEWAY`, `SUPPLY-SERVICE`, `LOYALTY-SERVICE`.

## API Gateway
O Gateway roda na porta **8080** e atua como porta de entrada.
Rotas configuradas:
- `/api/supplies/**` -> `supply-service`
- `/api/loyalty/**` -> `loyalty-service`

## Exemplos de requisições (Via Terminal/Postman)

**Testar Abastecimento (Passando pelo Gateway e usando Circuit Breaker):**
```bash
curl -X POST http://localhost:8080/api/supplies/checkout
```

**Testar Consulta de Pontos (Direto no Loyalty através do Gateway):**
```bash
curl -X GET http://localhost:8080/api/loyalty/123/points
```
