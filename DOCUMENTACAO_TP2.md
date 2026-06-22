# Documentação de Evolução da Arquitetura (TP2) - GasStation Hub

Esta documentação detalha a evolução arquitetural realizada no **GasStation Hub** para atender aos requisitos da **Entrega 2 (TP2)**, focando na introdução de comunicação assíncrona, programação reativa e observabilidade.

---

## 1. Comunicação Assíncrona com Kafka (Fila de Contingência)

### Justificativa Arquitetural
Na Entrega 1, a estratégia de resiliência implementada foi o Circuit Breaker no `supply-service`. Quando o `loyalty-service` caía, o abastecimento não parava, mas os pontos conquistados eram apenas "simulados" via log no Fallback.

Nesta entrega, transformamos o Fallback em uma **Fila de Contingência de Alta Disponibilidade** usando o **Apache Kafka**.
- O fluxo *Principal* continua sendo síncrono via Feign para garantir consistência imediata (feedback na tela do frentista).
- O fluxo *Alternativo (Fallback)* agora cria um evento de domínio **`AbastecimentoRegistradoEvent`** e o publica no tópico Kafka `abastecimentos.registrados`. O uso do verbo no passado indica claramente um "Fato Consumado": o abastecimento já ocorreu no mundo real e no banco de dados local.

O **Kafka** foi escolhido porque retém mensagens de forma distribuída mesmo se os consumidores estiverem offline. Quando o `loyalty-service` é reiniciado, o seu `LoyaltyKafkaConsumer` consome todos os eventos acumulados e atualiza o saldo dos clientes no MongoDB. Isso demonstra um excelente desacoplamento temporal, resolvendo o problema de ter uma chamada HTTP perdida caso o destino caia.

### 1.1. Cuidados e Resiliência na Mensageria
Para atender às boas práticas de sistemas distribuídos, implementamos os seguintes cuidados no fluxo assíncrono:

* **O que acontece se o consumidor estiver fora do ar?** O Kafka armazena as mensagens localmente em disco. Assim que o `loyalty-service` reiniciar, ele buscará as mensagens a partir do último `offset` processado e as consumirá normalmente, sem perda de dados.
* **Consumo em duplicidade e Inconsistências (Idempotência):** Garantimos que uma mesma mensagem não duplique pontos do cliente. O documento `CustomerLoyalty` no MongoDB armazena um array `processedSupplyIds`. Antes de creditar os pontos, o consumidor verifica se aquele ID de abastecimento já foi processado. Se sim, a mensagem é silenciosamente ignorada.
* **Registro de Erros (DLT - Dead Letter Topic):** Em um ambiente real avançado, usaríamos o `@RetryableTopic` do Spring Kafka. Para esta entrega, se houver um erro de infraestrutura na hora de processar os pontos reativamente, a mensagem não fará *commit* no Kafka até que seja processada com sucesso.
* **Visualização de Mensagens e Tópicos:** Adicionamos o **Kafdrop** na stack do `docker-compose.yml`. Ele provê uma interface web (acessível na porta `9000`) para inspecionar os tópicos, partições e visualizar o conteúdo em JSON das mensagens que estão na fila.

---

## 2. Programação Reativa (Spring WebFlux + Reactive Mongo)

### Justificativa Arquitetural
Foi exigido o uso do modelo reativo "em pelo menos uma parte da solução". Decidimos migrar todo o **`loyalty-service`** para uma stack 100% reativa e não bloqueante:
- Substituímos o Tomcat pelo **Netty** (`spring-boot-starter-webflux`).
- Substituímos os drivers síncronos pelo **ReactiveMongoRepository** (`spring-boot-starter-data-mongodb-reactive`).

**Por que no Loyalty Service?**
Sistemas de fidelidade sofrem muitos acessos de leitura (consultas de saldo) e escrita (atualização concorrente de pontos). O modelo reativo (baseado em Event Loop) permite que o serviço gerencie milhares de conexões simultâneas usando pouquíssimas threads de CPU, pois as threads nunca bloqueiam esperando o I/O do banco de dados MongoDB.

---

## 3. Observabilidade (Métricas, Logs e Rastreamento)

### 3.1. Métricas Expostas
Adicionamos o `spring-boot-starter-actuator` e o `micrometer-registry-prometheus` nos microsserviços. O Micrometer automaticamente exporta métricas vitais da JVM e do Spring no formato do Prometheus (`/actuator/prometheus`).

**Métricas relevantes expostas e por que ajudam:**
* `http_server_requests_seconds_count`: Total de requisições web. Ajuda a monitorar o volume de tráfego.
* `resilience4j_circuitbreaker_state`: O estado do Circuit Breaker. Essencial para criar alertas se muitos serviços estiverem caindo (circuito OPEN).
* `kafka_producer_record_send_total`: Total de mensagens enviadas pro Kafka (neste contexto, quantos abastecimentos precisaram de contingência).

### 3.2. Logs e Rastreamento (Trace ID)
Para não ter apenas "logs jogados", introduzimos o **Micrometer Tracing** (com a ponte Brave). 
Essa biblioteca insere dinamicamente o `traceId` (ID único de rastreamento) e o `spanId` (ID do trecho da execução) em cada log gerado pelo Slf4j.

**Exemplo Prático de Correlação:**
Quando uma requisição chega no Gateway e passa pelos serviços, o log conterá um hash como `[666a0122... , 666a0122...]`. 
Se a mesma requisição causar um Fallback e o envio ao Kafka, o produtor no `supply-service` logará:
`INFO [supply-service,666a0122e...,666a0122e...] Fallback acionado... Publicando evento no Kafka.`

E quando o `loyalty-service` consumir o evento do Kafka, ele compartilhará da mesma cadeia de rastreamento:
`INFO [loyalty-service,666a0122e...,127f8a9b...] Mensagem recebida do Kafka! Reprocessando pontos...`

Isso permite rastrear "A Jornada do Abastecimento" ponta a ponta, mesmo envolvendo filas assíncronas e bancos de dados distintos!
