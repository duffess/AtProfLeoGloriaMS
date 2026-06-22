# ⛽ GasStation Hub

O **GasStation Hub** é uma plataforma distribuída baseada em Microsserviços para gerenciamento de postos de combustíveis. O sistema não apenas controla o processo de abastecimento em si (Supply), como também conta com um programa de fidelidade (Loyalty) integrado, além de uma arquitetura segura, resiliente e escalável.

---

## 🏛️ Arquitetura e Evolução

O projeto evoluiu ao longo de três grandes entregas (TP1, TP2 e TP3), agregando diferentes tecnologias para resolver problemas de sistemas distribuídos:

### 1. Comunicação Síncrona e Roteamento (TP1)
* **API Gateway (Spring Cloud Gateway):** Ponto único de entrada do sistema.
* **Service Discovery (Eureka Server):** Registro dinâmico de serviços, permitindo balanceamento de carga automático.
* **Circuit Breaker (Resilience4j):** Implementado no `supply-service`. Caso o sistema de fidelidade falhe, o caixa do posto não pode parar de abastecer carros.

### 2. Mensageria, Reatividade e Observabilidade (TP2)
* **Fila de Contingência (Apache Kafka):** O fluxo de fallback foi aprimorado. Caso o `loyalty-service` falhe, os pontos pendentes são jogados no Kafka (`abastecimentos.registrados`) e consumidos de forma **idempotente** assim que o serviço volta.
* **Programação Reativa:** O `loyalty-service` foi migrado para o **Spring WebFlux** (Netty) e banco de dados **Reactive Mongo**. Dessa forma ele suporta uma alta concorrência de escritas e leituras sem bloquear as threads.
* **Observabilidade:** Coleta de métricas da JVM com **Prometheus/Actuator** e rastreamento distribuído injetando o *traceId* através do **Micrometer Tracing**.

### 3. Segurança (TP3)
* **Auth Service:** Microsserviço independente e focado exclusivamente na emissão de **JWT** (JSON Web Token), fornecendo *Access Token* e *Refresh Token*.
* **Resource Server:** O `supply-service` agora protege rotas críticas (como o checkout de um abastecimento), validando os cabeçalhos `Authorization: Bearer <token>`.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 17
* **Framework:** Spring Boot 3.x
* **Bancos de Dados:** PostgreSQL (Supply) e MongoDB (Loyalty)
* **Mensageria:** Apache Kafka (com interface Kafdrop)
* **Segurança:** Spring Security + JWT
* **Cloud & Resiliência:** Spring Cloud Gateway, Netflix Eureka, Resilience4j, OpenFeign
* **Infraestrutura:** Docker e Docker Compose

---

## 🚀 Como Executar

O projeto depende de infraestrutura externa (PostgreSQL, MongoDB e Kafka) que está contida no arquivo `docker-compose.yml`.

### Passo 1: Subir a Infraestrutura
Abra o terminal na raiz do projeto e execute:
```bash
docker-compose up -d
```

### Passo 2: Inicializar os Microsserviços
A ordem recomendada de inicialização dos serviços é a seguinte:
1. `discovery-server` *(Aguarde até estar pronto na porta 8761)*
2. `api-gateway` *(Porta 8080)*
3. `auth-service`
4. `loyalty-service`
5. `supply-service`

### Passo 3: Utilização

Para testar as requisições facilmente, um arquivo **`postman.json`** foi exportado e está na raiz do projeto. Você pode importá-lo no seu Postman/Insomnia.

1. **Faça o Login:** Requisite na rota de Auth (`POST /api/auth/login`) enviando `admin` e `123456` para obter seu Token.
2. **Abasteça e Ganhe Pontos:** Adicione o Token no Header da rota (`POST /api/supplies/checkout`). Se o sistema de pontos cair durante essa etapa, o Kafka salvará seu abastecimento!

---
*Desenvolvido por Guilherme Duffes - Projeto de Arquitetura de Microsserviços.*
