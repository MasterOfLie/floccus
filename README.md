# Floccus

**Floccus** é um MVP de uma plataforma **PaaS (Platform as a Service)** desenvolvida para simplificar o processo de implantação e gerenciamento de aplicações utilizando **Docker**.

## Arquitetura

O Floccus é dividido em três componentes principais:

```text
┌──────────────────────┐
│      Frontend        │
│      Angular         │
└──────────┬───────────┘
           │ HTTP
           ▼
┌──────────────────────┐
│       Backend        │
│    Spring Boot       │
└──────────┬───────────┘
           │ gRPC
           ▼
┌──────────────────────┐
│       Agente         │
│     Java Puro        │
└──────────┬───────────┘
           │ Docker API
           ▼
┌──────────────────────┐
│    Docker Engine     │
└──────────────────────┘
```

### Frontend

O frontend é responsável pela interface de gerenciamento da plataforma.

**Tecnologias principais:**

- Angular
- TypeScript
- HTML
- CSS
- Bootstrap Icons

O painel permite ao usuário visualizar e gerenciar os recursos disponibilizados pelo Floccus.

### Backend

O backend é responsável por disponibilizar a API utilizada pelo frontend, gerenciar as regras de negócio e realizar a comunicação com os agentes instalados nos servidores.

**Tecnologias principais:**

- Java
- Spring Boot
- gRPC
- Maven

### Agente

O agente é um serviço independente desenvolvido em **Java**, responsável por executar as operações no servidor onde está instalado. Ele recebe comandos do backend através de **gRPC** e realiza as operações correspondentes no Docker Engine.

---

# Requisitos

Antes de executar o projeto, certifique-se de possuir os seguintes componentes instalados:

- Java 21 ou superior.
- Maven;
- Node.js;
- npm;
- Angular CLI;
- Docker;
- Git.

---

# Estrutura do projeto

O projeto é dividido em três aplicações:

```text
floccus/
├── Floccus/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── FloccusAgente/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
└── FloccusFrontend/
    ├── src/
    ├── package.json
    └── ...
```
---

# 1. Executar o Agente

Entre no diretório do agente:

```bash
cd FloccusAgente
```

Compile o projeto:

```bash
mvn clean package
```

Depois execute o JAR gerado:

```bash
java -jar target/<arquivo-do-agente>.jar
```

O agente deverá iniciar seu servidor gRPC e ficar aguardando conexões do Backend.

> O agente deve ser executado no servidor que possui acesso ao Docker Engine que será gerenciado pelo Floccus.

---

# 2. Executar o Backend

Entre no diretório do Backend:

```bash
cd floccus
```

Compile o projeto:

```bash
mvn clean package
```

Execute a aplicação:

```bash
java -jar target/<arquivo-do-backend>.jar
```

O Backend disponibilizará a API responsável pela comunicação com o frontend e pelos comandos enviados aos agentes.

---

# 3. Configurar o Frontend

Entre no diretório do frontend:

```bash
cd FloccusFrontend
```

Instale as dependências:

```bash
npm install
```

Execute o servidor de desenvolvimento:

```bash
npm start
```

---

# Licença
Este projeto é distribuído sob a Apache License 2.0.

A licença permite o uso, modificação e distribuição do software, inclusive para fins comerciais, de acordo com os termos estabelecidos pela licença.

Consulte o arquivo [`LICENSE`](LICENSE) para obter o texto completo da licença.
