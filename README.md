---

# 🚗 Rota · Sistema de Aluguel de Carros 👨‍💻

> [!NOTE]
> Sistema web para **efetuar, modificar, consultar e cancelar pedidos de aluguel de automóveis**, com análise financeira feita por agentes (empresas e bancos) e execução de contratos, inclusive com crédito bancário.

<table>
  <tr>
    <td width="800px">
      <div align="justify">
        O <b>Rota</b> é o Laboratório 02 da disciplina <i>Desenvolvimento e Integração de Aplicações Web (DIAW)</i> do curso de Engenharia de Software da PUC Minas. Clientes cadastrados montam pedidos de aluguel escolhendo o automóvel e o período, podendo pedir crédito a um banco. Os agentes (locadoras e bancos) avaliam cada pedido do ponto de vista financeiro e, com parecer positivo, executam o contrato. O back-end segue a arquitetura <b>MVC</b> com <b>Spring Boot</b>, o front-end usa <b>React + Vite</b> e os dados ficam no <b>Supabase (PostgreSQL)</b>, com acesso restrito a usuários autenticados.
      </div>
    </td>
    <td>
      <div>
        <img src="docs/img/logo.svg" alt="Logo do Rota" width="120px"/>
      </div>
    </td>
  </tr>
</table>

---

## 🚧 Status do Projeto

![Status](https://img.shields.io/badge/Status-Em_desenvolvimento-F5B700?style=for-the-badge) ![Versão](https://img.shields.io/badge/Versão-v0.1.0-1F4FD8?style=for-the-badge) ![React](https://img.shields.io/badge/React-18.3-1F4FD8?style=for-the-badge&logo=react&logoColor=white) ![Vite](https://img.shields.io/badge/Vite-5.4-1F4FD8?style=for-the-badge&logo=vite&logoColor=white) ![Java](https://img.shields.io/badge/Java-25-1F4FD8?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.9-1F4FD8?style=for-the-badge&logo=springboot&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-3.9-1F4FD8?style=for-the-badge&logo=apachemaven&logoColor=white) ![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-1F4FD8?style=for-the-badge&logo=supabase&logoColor=white)

| Etapa do processo | Situação |
|---|---|
| Modelagem (casos de uso, histórias, classes e pacotes) | ✅ v1, revisada na v2 (pacotes), na v3 (classes) e na v5 (classes e pacotes) |
| Autenticação (login de clientes e agentes) | ✅ |
| CRUD de cliente | ✅ |
| Protótipo (criar pedido e ver o status) | ✅ cliente (criar, modificar, consultar e cancelar) e agente: fila, parecer, crédito do banco, contrato e frota |
| Revisão dos diagramas | contínua |

---

## 📚 Índice
- [Links Úteis](#-links-úteis)
- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades Principais](#-funcionalidades-principais)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura](#-arquitetura)
  - [Modelagem UML](#modelagem-uml)
  - [Versões dos modelos](#versões-dos-modelos)
- [Instalação e Execução](#-instalação-e-execução)
  - [Pré-requisitos](#pré-requisitos)
  - [Variáveis de Ambiente](#-variáveis-de-ambiente)
  - [Instalação de Dependências](#-instalação-de-dependências)
  - [Banco de Dados (Supabase)](#-banco-de-dados-supabase)
  - [Como Executar a Aplicação](#-como-executar-a-aplicação)
- [Deploy](#-deploy)
- [Estrutura de Pastas](#-estrutura-de-pastas)
- [Demonstração](#-demonstração)
- [Testes](#-testes)
- [Documentações utilizadas](#-documentações-utilizadas)
- [Autores](#-autores)
- [Contribuição](#-contribuição)
- [Agradecimentos](#-agradecimentos)
- [Licença](#-licença)

---

## 🔗 Links Úteis
* 🎨 **Wireframes no Figma:** [Rota – Aluguel de Carros (LAB02)](https://www.figma.com/design/vPTstldZsygAGqDP3DTjqx)
  > 18 telas (área pública, cliente e agente) e a folha de componentes.
* 🖼️ **Wireframes em HTML:** [`docs/wireframes/wireframes.html`](docs/wireframes/wireframes.html)
  > Mesmas telas, geradas por `docs/wireframes/gerar_wireframes.py`. É a referência visual do front-end.
* 📐 **Modelagem:** [`docs/modelagem`](docs/modelagem)
  > Histórias de usuário e diagramas UML (fonte PlantUML + imagem).

---

## 📝 Sobre o Projeto
Locadoras e bancos precisam de um jeito simples de receber pedidos de aluguel pela internet, analisar a situação financeira de quem pede e formalizar o contrato. O Rota junta os três lados no mesmo sistema:

- **Clientes** (pessoas físicas) se cadastram com RG, CPF, endereço, profissão e até 3 rendimentos, e então criam, modificam, consultam e cancelam pedidos de aluguel.
- **Agentes** (empresas e bancos) avaliam os pedidos com um parecer financeiro. Com o parecer positivo, o agente executa o contrato e define em nome de quem o automóvel fica registrado (cliente, empresa ou banco).
- **Bancos** também podem conceder um **contrato de crédito** associado ao aluguel.

O sistema só pode ser usado depois do cadastro e do login com usuário e senha.

---

## ✨ Funcionalidades Principais
- 🔐 **Autenticação:** cadastro e login de clientes e agentes. Todas as telas e rotas da API exigem login.
- 👤 **CRUD de cliente:** dados pessoais, endereço, profissão e até 3 rendimentos.
- 📝 **Pedidos de aluguel:** escolha do automóvel e do período, valor estimado pelas diárias, e alteração ou cancelamento enquanto o pedido está em análise.
- 🔎 **Acompanhamento do status:** `Enviado → Em análise → Aprovado / Reprovado → Contratado`, ou `Cancelado`.
- ⚖️ **Parecer financeiro:** o agente vê a renda total do cliente e quanto o aluguel pesa nela, e aprova ou reprova o pedido com uma justificativa. Ele também pode modificar o pedido (automóvel e datas) durante a análise.
- 🏦 **Crédito bancário:** o cliente pede crédito a um banco, e o banco define o valor, as parcelas e a taxa (tabela Price).
- 📄 **Contratos:** execução do contrato (número `CTR-AAAA-NNNN`), registro do proprietário do automóvel e lista de contratos com filtro por crédito.
- 🚙 **Frota:** a empresa cadastra, edita e remove os automóveis (matrícula, ano, marca, modelo, placa e diária).

---

## 🛠 Tecnologias Utilizadas

### 💻 Front-end
* **Biblioteca:** React 18 e React Router 6
* **Linguagem:** JavaScript (ES2022+)
* **Estado de login:** Context API (`context/AuthContext.jsx`), com o token guardado no `localStorage`
* **Estilização:** CSS com tokens próprios (`src/styles/tokens.css`), fontes Sora, Inter e JetBrains Mono
* **Build Tool:** Vite 5

### 🖥️ Back-end
* **Linguagem/Runtime:** Java 25 (JDK 25)
* **Framework:** Spring Boot 4.0.9 (Web MVC, Data JPA, Validation)
* **Build e dependências:** Apache Maven
* **Banco de Dados:** Supabase (PostgreSQL)
* **ORM:** Hibernate / JPA
* **Autenticação:** login com usuário e senha (Spring Security + BCrypt) e token JWT HS256 (`spring-boot-starter-security-oauth2-resource-server`)

### ⚙️ Infraestrutura & Ferramentas
* **Banco gerenciado:** Supabase
* **Modelagem:** PlantUML
* **Protótipo:** Figma
* **Testes:** JUnit 5, AssertJ e H2 em memória para validar o mapeamento JPA

---

## 🏗 Arquitetura

O sistema tem os **dois subsistemas** do enunciado, ligados por uma API REST:

1. **Gestão de pedidos e contratos (back-end, Spring Boot, MVC):**
   - `model`: entidades JPA com as **regras do domínio dentro delas**. Exemplos: `Pedido#cancelar` só funciona em pedidos enviados ou em análise, `Cliente#adicionarRendimento` limita a 3 e `ContratoCredito#calcularValorParcela` usa a tabela Price.
   - `repository`: acesso aos dados com Spring Data JPA.
   - `service`: casos de uso e transações.
   - `controller`: endpoints REST.
   - `dto`: entrada e saída da API.
   - `security`: login e permissões por perfil.
2. **Construção dinâmica das páginas web (front-end, React + Vite):** é o **V** do MVC. As páginas ficam separadas por perfil (`pages/publico`, `pages/cliente` e `pages/agente`) e consomem a API pelo `services/api.js`.

**Autenticação**
1. No cadastro, a senha é guardada com **BCrypt**. O usuário já sai logado.
2. No login, o Spring Security confere a senha e a API devolve um **JWT** assinado (HS256) que vale 8 horas.
3. O front guarda o token e o envia em toda chamada (`Authorization: Bearer <token>`).
4. Todas as rotas da API exigem o token, menos o login e o cadastro. Dentro do token vão os papéis: `CLIENTE`, ou `AGENTE` mais `EMPRESA` ou `BANCO`.
5. No front, `RotaProtegida` manda para o login quem não está logado e leva cada perfil para a sua área (`/cliente` ou `/agente`).

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| `POST` | `/auth/cadastro/cliente` | livre | Cadastra o cliente (dados pessoais, endereço e até 3 rendimentos) e devolve o token |
| `POST` | `/auth/cadastro/agente` | livre | Cadastra empresa ou banco (razão social e CNPJ) e devolve o token |
| `POST` | `/auth/login` | livre | Confere usuário e senha e devolve o token |
| `GET` | `/auth/me` | token | Dados do usuário logado |
| `GET` | `/clientes/me` | cliente | Cadastro completo: dados pessoais, endereço, rendimentos e renda total |
| `PUT` | `/clientes/me` | cliente | Altera nome, RG, profissão, endereço e a lista de rendimentos (até 3). O CPF e o usuário não mudam |
| `DELETE` | `/clientes/me` | cliente | Exclui a conta (veja a regra abaixo) |
| `GET` | `/automoveis` | token | Frota, com o tipo do proprietário depois do contrato |
| `POST` | `/automoveis` | empresa | Cadastra um automóvel. Placa (padrão `ABC1D23`) e matrícula únicas (409) |
| `PUT` | `/automoveis/{id}` | empresa | Edita o automóvel. A nova diária só vale para os próximos pedidos |
| `DELETE` | `/automoveis/{id}` | empresa | Remove, só se o automóvel nunca apareceu em um pedido (senão 422) |
| `GET` | `/bancos` | token | Bancos para pedir crédito |
| `POST` | `/pedidos` | cliente | Cria o pedido (automóvel, retirada, devolução e crédito opcional). Começa como `ENVIADO` |
| `GET` | `/pedidos` | cliente | Meus pedidos, do mais novo para o mais antigo |
| `GET` | `/pedidos/{id}` | cliente | Detalhe com status, crédito e pareceres. O pedido de outro cliente responde 404 |
| `PUT` | `/pedidos/{id}` | cliente | Modifica o automóvel e as datas, só se estiver `ENVIADO` ou `EM_ANALISE` (senão 422). O valor passa a usar a diária atual e o crédito pedido não muda |
| `POST` | `/pedidos/{id}/cancelar` | cliente | Cancela, só se estiver `ENVIADO` ou `EM_ANALISE` (senão 422) |
| `GET` | `/agente/pedidos` | agente | Pedidos que o agente acompanha, do mais antigo para o mais novo, com o cadastro do cliente. Filtro opcional: `?status=ENVIADO&status=EM_ANALISE` |
| `GET` | `/agente/pedidos/{id}` | agente | Detalhe para a análise: pedido, cliente, renda total, crédito e contrato. Pedido que o agente não acompanha responde 404 |
| `POST` | `/agente/pedidos/{id}/analise` | agente | Abre o pedido para análise: `ENVIADO` passa para `EM_ANALISE` |
| `POST` | `/agente/pedidos/{id}/parecer` | agente | Parecer positivo ou negativo com justificativa. No positivo do banco vão também `credito: {valor, parcelas, taxaMensal}` |
| `PUT` | `/agente/pedidos/{id}` | agente | Modifica o automóvel e as datas enquanto o pedido está em análise |
| `POST` | `/agente/pedidos/{id}/contrato` | agente | Executa o contrato de um pedido `APROVADO` e registra o proprietário (`CLIENTE`, `EMPRESA` ou `BANCO`) |
| `GET` | `/agente/contratos` | agente | Contratos executados, do mais novo para o mais antigo, com o crédito associado |

**Quem avalia cada pedido (lado do agente):**
- A **empresa** (locadora) acompanha todos os pedidos e dá o parecer dos pedidos **sem crédito**.
- O **banco** só vê os pedidos em que o cliente pediu crédito a ele, e é ele quem dá o parecer desses pedidos. O parecer positivo do banco já **concede o crédito** (valor, parcelas e taxa ao mês; a parcela sai pela tabela Price).
- Depois do parecer positivo, o agente **executa o contrato** e escolhe o proprietário do automóvel: o cliente, a própria empresa (só se quem executa for uma empresa) ou o banco do crédito.
- As regras ficam nas entidades: `Agente#podeAcompanhar`, `Agente#podeAvaliar`, `Agente#iniciarAnalise`, `Agente#modificarPedido`, `Agente#avaliarPedido`, `Banco#concederCredito` e `Agente#executarContrato`.
- O pedido guarda a **diária do dia em que foi feito**: se a empresa muda o preço do automóvel, os pedidos e contratos antigos não mudam.

**Frota inicial:** quando a tabela de automóveis está vazia, a API cadastra os 6 carros dos wireframes ao subir (`config/CargaInicial`). Para desligar, use `APP_CARGA_INICIAL=false`.

**CRUD de cliente:**
- O **C** é o próprio cadastro (`/auth/cadastro/cliente`).
- O **R**, o **U** e o **D** ficam em `/clientes/me`. O cliente só enxerga a própria conta, porque o login vem do token. Um agente que tenta acessar recebe 403.
- **Exclusão:** um cliente **sem pedidos** é apagado de verdade, junto com os rendimentos. Um cliente **com histórico** tem os pedidos ainda em análise cancelados e a conta **desativada**: não entra mais, mas os contratos executados continuam registrados (US05).

Os erros voltam no formato `{"mensagem": "...", "campos": {"cpf": "CPF inválido."}}`: 400 para dados inválidos, 401 para login errado ou sem token, 403 para o perfil errado, 404 para o que não existe (ou não é seu), 409 para usuário, CPF, CNPJ, placa ou matrícula repetidos e 422 para regras do domínio (ex.: cancelar um pedido já aprovado).

**Decisões importantes**
- A herança `Usuario → Cliente | Agente → Empresa | Banco` usa a estratégia `JOINED`: uma tabela por classe, que espelha o diagrama de classes.
- As regras ficam nas entidades, não espalhadas pelos controllers. Assim o diagrama de classes (com os métodos) bate com o código.
- O pedido só aceita alteração, cancelamento ou parecer com o status `ENVIADO` ou `EM_ANALISE`.

### Modelagem UML

| Casos de Uso | Pacotes (Visão Lógica) |
| :---: | :---: |
| <img src="docs/modelagem/v1/casos-de-uso.png" alt="Diagrama de casos de uso" width="420px"> | <img src="docs/modelagem/v5/pacotes.png" alt="Diagrama de pacotes" width="420px"> |
| **Classes** | **Histórias de Usuário** |
| <img src="docs/modelagem/v5/classes.png" alt="Diagrama de classes" width="420px"> | [15 histórias com critérios de aceitação](docs/modelagem/historias-de-usuario.md) |

### Versões dos modelos
Cada revisão dos diagramas ganha uma pasta nova (`v2`, `v3`...), e as anteriores ficam guardadas, como pede o enunciado.

| Versão | Data | O que mudou |
|---|---|---|
| [v1](docs/modelagem/v1) | 28/10/2026 | Versão inicial: casos de uso, classes e pacotes |
| [v2](docs/modelagem/v2) | 28/10/2026 | Pacotes: entram as dependências da autenticação (`service → security`, `dto → model`), o tratador de erros passa para `controller` e o front ganha `utils`. Conferido pelos imports, sem ciclos. Casos de uso e classes continuam iguais à v1. |
| [v3](docs/modelagem/v3) | 28/10/2026 | Classes: `Usuario` ganha `ativo` e `desativar()`, para a exclusão de cliente com histórico (a conta é desativada e os contratos ficam). |
| [v4](docs/modelagem/v4) | 28/10/2026 | Pacotes: `config` passa a depender de `repository` e `model` (frota inicial do protótipo). No front entra `hooks`, e as páginas chamam os `services`. Conferido pelos imports. |
| [v5](docs/modelagem/v5) | 29/10/2026 | Lado do agente. Classes: `Agente` ganha `podeAcompanhar` e `podeAvaliar` (implementados por `Empresa` e `Banco`), `iniciarAnalise` e `modificarPedido`; `Pedido` guarda a `valorDiaria` do dia do pedido. Pacotes: `controller → model` (o enum `StatusPedido` no filtro da fila). Conferido pelos imports, sem ciclos. |

Cada pasta guarda só os diagramas que mudaram naquela versão. A versão atual de cada um é: casos de uso na v1, classes e pacotes na v5. Para gerar as imagens de novo a partir dos `.puml`: `java -jar plantuml.jar -tpng docs/modelagem/v5/*.puml`.

---

## 🔧 Instalação e Execução

### Pré-requisitos
* **Java JDK 25** (back-end)
* **Maven 3.9+**, ou o wrapper `mvnw` que já vem no projeto
* **Node.js 18+** (recomendado 20 LTS) e **npm** (front-end)
* Uma conta no **[Supabase](https://supabase.com/)** com um projeto criado

---

### 🔑 Variáveis de Ambiente

#### 1 Back-end (Spring Boot)
Copie `backend/.env.example` para `backend/.env` e preencha:

| Variável | Descrição | Exemplo |
|---|---|---|
| `DB_URL` | JDBC do Supabase (Session pooler) | `jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require` |
| `DB_USERNAME` | Usuário do banco | `postgres.<project-ref>` |
| `DB_PASSWORD` | Senha do banco | `********` |
| `JPA_DDL_AUTO` | Estratégia do Hibernate | `update` |
| `CORS_ALLOWED_ORIGINS` | Origem do front-end | `http://localhost:5173` |
| `JWT_SECRET` | Segredo que assina o token (mínimo de 32 caracteres) | gere com `openssl rand -base64 48` |
| `JWT_VALIDADE_HORAS` | Quanto tempo o login vale | `8` |

#### 2 Front-end (React, Vite)
Copie `frontend/.env.example` para `frontend/.env`:

| Variável | Descrição | Exemplo |
|---|---|---|
| `VITE_API_URL` | Endereço da API | `http://localhost:8080` |

> [!WARNING]
> Os arquivos `.env` estão no `.gitignore`. Nunca suba senhas para o repositório.

---

### 📦 Instalação de Dependências

#### Front-end (React)
```bash
cd frontend
npm install
```

#### Back-end (Spring Boot)
```bash
cd backend
./mvnw clean install
```

---

### 💾 Banco de Dados (Supabase)
1. Crie um projeto no Supabase.
2. Em **Project Settings › Database › Connection string**, copie a conexão **JDBC** do **Session pooler**. Ela funciona em redes IPv4.
3. Preencha `backend/.env`. Com `JPA_DDL_AUTO=update`, o Hibernate cria as tabelas na primeira execução.

---

### ⚡ Como Executar a Aplicação

#### Terminal 1: Back-end (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
```
A API sobe em `http://localhost:8080`.

> [!TIP]
> **Sem Supabase?** Para testar ou demonstrar, dá para subir a API com um banco H2 em memória (os dados somem quando ela para):
> ```bash
> cd backend
> ./mvnw spring-boot:test-run -Dspring-boot.run.profiles=h2
> ```

#### Terminal 2: Front-end (React, Vite)
```bash
cd frontend
npm run dev
```
O front abre em `http://localhost:5173`.

---

## 🚀 Deploy
```bash
# Front-end: gera a pasta frontend/dist com os arquivos estáticos
cd frontend && npm run build

# Back-end: gera o .jar em backend/target
cd backend && ./mvnw clean package
java -jar target/aluguel-carros-0.0.1-SNAPSHOT.jar
```

---

## 📂 Estrutura de Pastas

```
.
├── README.md                     # 📘 Este documento
├── LICENSE                       # ⚖️ Licença MIT
├── .gitignore                    # 🧹 Ignora .env, node_modules, target, dist
│
├── backend/                      # 📁 Subsistema de gestão de pedidos e contratos (Spring Boot)
│   ├── .env.example              # 🧩 Variáveis do back-end
│   ├── pom.xml                   # 🛠️ Build e dependências (Maven)
│   └── src/
│       ├── main/java/br/pucminas/aluguelcarros/
│       │   ├── controller/       # 🎮 Endpoints REST e tratamento de erros (C do MVC)
│       │   ├── service/          # ⚙️ Casos de uso e transações (cadastro, pedidos, avaliação, contratos, frota)
│       │   ├── repository/       # 🗄️ Repositórios Spring Data JPA
│       │   ├── model/            # 🧬 Entidades, enums e regras do domínio (M do MVC)
│       │   ├── dto/              # ✉️ Entrada e saída da API, com Bean Validation
│       │   ├── security/         # 🛡️ Spring Security, BCrypt e emissão do JWT
│       │   ├── config/           # 🔧 CORS e frota inicial do protótipo
│       │   └── exception/        # 💥 Exceções do domínio
│       ├── main/resources/
│       │   └── application.properties
│       ├── test/java/...         # 🧪 Testes do domínio, do JPA e da API
│       └── test/resources/       # 🧪 Perfis "test" e "h2" (banco em memória)
│
├── frontend/                     # 📁 Subsistema de construção das páginas web (React + Vite)
│   ├── .env.example              # 🧩 Variáveis do front-end
│   ├── index.html
│   ├── package.json
│   ├── public/                   # 🖼️ Favicon
│   └── src/
│       ├── pages/                # 📄 Telas por perfil: publico, cliente, agente (V do MVC)
│       ├── components/           # 🧱 Botão, campo, placa Mercosul, menu lateral, rota protegida...
│       ├── context/              # 🔐 Sessão (token e usuário logado)
│       ├── hooks/                # 🎣 Hooks (ex.: sessão expirada → volta ao login)
│       ├── services/             # 🔌 Chamadas HTTP para a API
│       ├── utils/                # 🛠️ Máscaras, validações, datas e status dos pedidos
│       └── styles/               # 🎨 Tokens do design system e estilos
│
└── docs/                         # 📚 Documentação
    ├── modelagem/                # 📐 Histórias de usuário e diagramas UML por versão (v1, v2...)
    ├── wireframes/               # 🖼️ Wireframes em HTML e o gerador
    └── img/                      # 🖼️ Logo e imagens do README
```

---

## 🎥 Demonstração

### 🌐 Aplicação Web
Telas já implementadas (login, cadastro, meu perfil, o protótipo de pedido do cliente e o lado do agente):

| Login (senha errada) | Escolha de perfil |
| :---: | :---: |
| <img src="docs/img/telas/login.png" alt="Tela de login" width="420px"> | <img src="docs/img/telas/escolha-perfil.png" alt="Escolha de perfil" width="420px"> |
| **Cadastro de cliente (renda)** | **Painel do agente (empresa)** |
| <img src="docs/img/telas/cadastro-cliente.png" alt="Cadastro de cliente" width="420px"> | <img src="docs/img/telas/painel-agente.png" alt="Painel do agente" width="420px"> |
| **Meu perfil (alterações por salvar)** | **Início do cliente** |
| <img src="docs/img/telas/meu-perfil.png" alt="Meu perfil" width="420px"> | <img src="docs/img/telas/inicio-cliente.png" alt="Início do cliente" width="420px"> |
| **Novo pedido (com crédito bancário)** | **Detalhe do pedido** |
| <img src="docs/img/telas/novo-pedido.png" alt="Novo pedido" width="420px"> | <img src="docs/img/telas/detalhe-pedido.png" alt="Detalhe do pedido" width="420px"> |
| **Meus pedidos** | **Pedido contratado (visto pelo cliente)** |
| <img src="docs/img/telas/meus-pedidos.png" alt="Meus pedidos" width="420px"> | <img src="docs/img/telas/detalhe-pedido-contratado.png" alt="Pedido contratado com crédito" width="420px"> |
| **Avaliar pedido (banco concede o crédito)** | **Executar contrato** |
| <img src="docs/img/telas/avaliar-pedido.png" alt="Avaliar pedido" width="420px"> | <img src="docs/img/telas/executar-contrato.png" alt="Executar contrato" width="420px"> |
| **Contratos** | **Automóveis (cadastro na gaveta)** |
| <img src="docs/img/telas/contratos.png" alt="Contratos" width="420px"> | <img src="docs/img/telas/automoveis.png" alt="Automóveis" width="420px"> |
| **Modificar pedido (cliente)** | |
| <img src="docs/img/telas/modificar-pedido.png" alt="Cliente modificando o pedido" width="420px"> | |

---

## 🧪 Testes

### Testes Unitários e de Integração
```bash
cd backend
./mvnw test
```
São 64 testes:
- **API do agente** (`AvaliacaoControllerTest`, 7 testes):
  - a empresa vê todos os pedidos e o banco só os de crédito para ele (outro banco não vê nada);
  - filtro da fila por status;
  - abrir para análise muda o status, e o cliente vê;
  - parecer positivo e negativo, justificativa obrigatória e sem segundo parecer;
  - o banco concede o crédito no parecer positivo (parcela pela tabela Price), e a empresa não avalia pedido com crédito;
  - modificar o pedido recalcula o valor, e não vale depois do parecer;
  - cliente barrado nas rotas do agente.
- **API de contratos** (`ContratoControllerTest`, 3 testes): só pedido aprovado vira contrato, número `CTR-AAAA-NNNN`, proprietário registrado no automóvel, crédito associado e lista de contratos por agente.
- **API da frota** (`AutomovelControllerTest`, 4 testes): cadastrar, editar e remover; placa, matrícula e ano inválidos ou repetidos; automóvel com pedido não sai; a nova diária não muda o pedido antigo; só a empresa mexe na frota.
- **API de pedidos** (`PedidoControllerTest`, 9 testes):
  - listas de automóveis e bancos;
  - pedido com e sem crédito;
  - datas inválidas, automóvel ou banco inexistente e campos obrigatórios;
  - um cliente não vê o pedido de outro;
  - modificar o automóvel e as datas recalcula o valor e mantém o crédito pedido;
  - modificar confere as datas, o automóvel, o dono do pedido e o status (pedido cancelado não muda);
  - cancelar e não cancelar de novo;
  - agente barrado nas rotas do cliente.
- **API do CRUD de cliente** (`ClienteControllerTest`, 7 testes):
  - consultar o cadastro completo;
  - alterar dados e rendimentos sem mexer no CPF, ou tirar todos os rendimentos;
  - validação;
  - 401 sem token e 403 para agente;
  - exclusão sem pedidos (apaga);
  - exclusão com histórico (cancela os pedidos abertos e desativa a conta).
- **API de autenticação** (`AuthControllerTest`, 14 testes com o Spring Security ligado):
  - cadastro de cliente, banco e empresa;
  - senha guardada com BCrypt;
  - CPF e CNPJ inválidos ou repetidos, mais de 3 rendimentos e endereço incompleto;
  - login certo, login errado e usuário inexistente;
  - rota protegida sem token e com token adulterado;
  - CORS liberado para o front.
- **Domínio** (`model`): ciclo de status do pedido, limite de 3 rendimentos, parecer com justificativa, quem acompanha e quem avalia cada pedido (`AgenteTest`), execução de contrato e parcela pela tabela Price.
- **Mapeamento JPA** (`repository`): o fluxo inteiro (cliente, pedido, parecer, crédito e contrato) é salvo e lido num H2 em memória.

*Ferramentas: JUnit 5, AssertJ, Spring Boot Test (`@SpringBootTest`, MockMvc, `@DataJpaTest`) e H2.*

---

## 🔗 Documentações utilizadas
* 📖 **Front-end:** [Documentação oficial do **React**](https://react.dev/reference/react)
* 📖 **Build Tool:** [Guia de configuração do **Vite**](https://vitejs.dev/config/)
* 📖 **Back-end:** [Documentação oficial do **Spring Boot**](https://docs.spring.io/spring-boot/index.html)
* 📖 **Banco de dados:** [Documentação do **Supabase**](https://supabase.com/docs)
* 📖 **Modelagem:** [Guia do **PlantUML**](https://plantuml.com/pt/)
* 📖 **Commits:** [**Conventional Commits**](https://www.conventionalcommits.org/pt-br/v1.0.0/)

---

## 👥 Autores

| 👤 Nome | 🖼️ Foto | :octocat: GitHub | 💼 LinkedIn |
|---------|----------|-----------------|-------------|
| Daniel Oliveira de Menezes | <div align="center"><img src="https://github.com/Danielolv14.png" width="70px" height="70px"></div> | <div align="center"><a href="https://github.com/Danielolv14"><img src="https://joaopauloaramuni.github.io/image/github6.png" width="50px" height="50px"></a></div> | <div align="center"><a href="https://www.linkedin.com/in/daniel-oliveira14"><img src="https://joaopauloaramuni.github.io/image/linkedin2.png" width="50px" height="50px"></a></div> |

---

## 🤝 Contribuição
1. Faça um `fork` do projeto.
2. Crie uma branch para a sua feature (`git checkout -b feature/minha-feature`).
3. Faça o commit das mudanças (`git commit -m 'feat: adiciona cadastro de cliente'`), seguindo o [Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/).
4. Faça o `push` para a branch (`git push origin feature/minha-feature`).
5. Abra um **Pull Request**.

---

## 🙏 Agradecimentos
* [**Engenharia de Software PUC Minas**](https://www.instagram.com/engsoftwarepucminas/), pelo apoio institucional e pela estrutura acadêmica.
* [**Prof. Dr. João Paulo Aramuni**](https://github.com/joaopauloaramuni), pelos ensinamentos de **Desenvolvimento e Integração de Aplicações Web**, **Arquitetura de Software** e **Padrões de Projeto**.

---

## 📄 Licença

Este projeto é distribuído sob a **[Licença MIT](LICENSE)**.

---
