# Histórias de Usuário

Sistema de Aluguel de Carros (LAB02 · DIAW · PUC Minas). Versão 1 dos modelos.

Cada história aponta o caso de uso correspondente ([diagrama de casos de uso](v1/casos-de-uso.png)) e a etapa do processo de desenvolvimento em que entra.

**Perfis**
- **Visitante:** pessoa ainda sem conta.
- **Cliente:** pessoa física que aluga o automóvel.
- **Agente:** empresa (locadora) ou banco que avalia os pedidos.

**Status do pedido**

`ENVIADO → EM_ANALISE → APROVADO | REPROVADO → CONTRATADO`. O pedido fica `CANCELADO` quando o cliente cancela antes do contrato.

| ID | História | Caso de uso | Prioridade | Etapa |
|---|---|---|---|---|
| US01 | Cadastrar-se como cliente | UC02 | Alta | Autenticação / CRUD de cliente |
| US02 | Cadastrar-se como agente | UC02 | Média | Autenticação |
| US03 | Fazer login | UC01 | Alta | Autenticação |
| US04 | Consultar e alterar meus dados | UC03 | Alta | CRUD de cliente |
| US05 | Excluir minha conta | UC03 | Média | CRUD de cliente |
| US06 | Criar pedido de aluguel | UC04 | Alta | Protótipo |
| US07 | Acompanhar o status dos pedidos | UC05 | Alta | Protótipo |
| US08 | Modificar pedido | UC06 | Média | Protótipo |
| US09 | Cancelar pedido | UC07 | Média | Protótipo |
| US10 | Solicitar crédito bancário | UC08 | Baixa | Evolução |
| US11 | Avaliar pedido (parecer financeiro) | UC09 | Alta | Protótipo |
| US12 | Conceder contrato de crédito | UC10 | Baixa | Evolução |
| US13 | Executar contrato | UC11 | Média | Evolução |
| US14 | Consultar contratos | UC12 | Baixa | Evolução |
| US15 | Manter automóveis | UC13 | Média | Protótipo |

---

## US01 · Cadastrar-se como cliente
**Como** visitante, **quero** criar uma conta de cliente **para** poder pedir o aluguel de um automóvel.

Critérios de aceitação
- [ ] O cadastro pede nome, CPF, RG, endereço (CEP, rua, número, bairro, cidade, UF), profissão, usuário e senha.
- [ ] É possível informar de 0 a 3 entidades empregadoras, cada uma com o rendimento mensal. A partir da 3ª, o botão "Adicionar" fica desabilitado.
- [ ] O CPF precisa ser válido e único. O usuário (login) também é único.
- [ ] A senha é guardada criptografada (hash), nunca em texto puro.
- [ ] Depois do cadastro, o cliente consegue entrar com o usuário e a senha criados.

## US02 · Cadastrar-se como agente
**Como** representante de uma locadora ou de um banco, **quero** cadastrar a minha instituição **para** avaliar os pedidos de aluguel.

Critérios de aceitação
- [ ] O cadastro pede o tipo (Empresa ou Banco), a razão social, o CNPJ, o usuário e a senha.
- [ ] O CNPJ precisa ser válido e único.
- [ ] O tipo escolhido define o que o agente vê: só o banco concede crédito, e só a empresa mantém a frota de automóveis.

## US03 · Fazer login
**Como** usuário cadastrado, **quero** entrar com o meu usuário e a minha senha **para** acessar as funções do meu perfil.

Critérios de aceitação
- [ ] Sem login, nenhuma tela interna nem endpoint da API pode ser acessado.
- [ ] Com usuário ou senha errados, aparece "Usuário ou senha inválidos", sem dizer qual dos dois está errado.
- [ ] O cliente cai na área do cliente e o agente cai no painel do agente.
- [ ] É possível sair (logout) por qualquer tela interna.

## US04 · Consultar e alterar meus dados
**Como** cliente, **quero** ver e editar os meus dados cadastrais **para** manter a análise financeira correta.

Critérios de aceitação
- [ ] O cliente vê os dados pessoais, o endereço, a profissão e os rendimentos.
- [ ] Ele pode alterar tudo, menos o CPF.
- [ ] O limite de 3 rendimentos continua valendo na edição.
- [ ] Um cliente não consegue ver nem alterar os dados de outro cliente.

## US05 · Excluir minha conta
**Como** cliente, **quero** excluir a minha conta **para** deixar de usar o sistema.

Critérios de aceitação
- [ ] O sistema pede confirmação antes de excluir.
- [ ] Os pedidos ainda em análise são cancelados.
- [ ] Os contratos já executados continuam registrados.

## US06 · Criar pedido de aluguel
**Como** cliente, **quero** escolher um automóvel e um período **para** pedir o aluguel.

Critérios de aceitação
- [ ] O cliente escolhe um automóvel da lista (marca, modelo, ano, placa e valor da diária).
- [ ] A data de devolução precisa ser posterior à data de retirada, e a retirada não pode estar no passado.
- [ ] O valor estimado é calculado como diárias × valor da diária.
- [ ] O pedido é criado com o status `ENVIADO` e ligado ao cliente logado.

## US07 · Acompanhar o status dos pedidos
**Como** cliente, **quero** ver os meus pedidos e o status de cada um **para** saber em que pé está a análise.

Critérios de aceitação
- [ ] A lista mostra o número, o automóvel, o período, o valor e o status.
- [ ] Dá para filtrar por status.
- [ ] O detalhe mostra a linha do tempo (Enviado, Em análise, Parecer, Contrato) e, quando houver, o parecer do agente com a justificativa.
- [ ] Sem pedidos, aparece um estado vazio com o atalho "Fazer meu primeiro pedido".

## US08 · Modificar pedido
**Como** cliente, **quero** alterar o automóvel ou as datas de um pedido **para** corrigir o pedido antes da análise terminar.

Critérios de aceitação
- [ ] Só dá para modificar pedidos com o status `ENVIADO` ou `EM_ANALISE`.
- [ ] O agente responsável pela análise também pode modificar o pedido.
- [ ] O valor estimado é recalculado.

## US09 · Cancelar pedido
**Como** cliente, **quero** cancelar um pedido **para** desistir do aluguel.

Critérios de aceitação
- [ ] Só dá para cancelar pedidos com o status `ENVIADO` ou `EM_ANALISE`.
- [ ] O sistema pede confirmação.
- [ ] O pedido passa para `CANCELADO` e não pode mais ser alterado.

## US10 · Solicitar crédito bancário
**Como** cliente, **quero** pedir crédito a um banco junto com o pedido **para** financiar o aluguel.

Critérios de aceitação
- [ ] No pedido, o cliente liga a opção "Solicitar crédito bancário", escolhe o banco e informa o valor.
- [ ] O banco escolhido passa a ver o pedido na fila de avaliação dele.

## US11 · Avaliar pedido
**Como** agente, **quero** dar um parecer financeiro sobre um pedido **para** aprovar ou reprovar o aluguel.

Critérios de aceitação
- [ ] O agente vê a fila de pedidos, do mais antigo para o mais novo.
- [ ] Ao abrir o pedido, ele vê os dados do cliente, a renda total (soma dos rendimentos) e os dados do pedido.
- [ ] O parecer é Positivo ou Negativo, com justificativa obrigatória.
- [ ] Com o parecer positivo, o pedido passa para `APROVADO`. Com o negativo, passa para `REPROVADO`.
- [ ] Ao abrir o pedido para análise, o status muda de `ENVIADO` para `EM_ANALISE`.

## US12 · Conceder contrato de crédito
**Como** banco, **quero** definir as condições do crédito ao aprovar um pedido **para** formalizar o financiamento.

Critérios de aceitação
- [ ] Só aparece para agentes do tipo Banco e em pedidos com crédito solicitado.
- [ ] O banco informa o valor, o número de parcelas e a taxa de juros ao mês.
- [ ] O sistema calcula o valor da parcela (tabela Price).

## US13 · Executar contrato
**Como** agente, **quero** executar o contrato de um pedido aprovado **para** concluir o aluguel.

Critérios de aceitação
- [ ] Só dá para executar contrato de pedidos `APROVADO`.
- [ ] O agente define em nome de quem o automóvel fica registrado: cliente, empresa ou banco.
- [ ] O contrato recebe um número (ex.: `CTR-2026-0087`) e o pedido passa para `CONTRATADO`, somente leitura.
- [ ] Se houver contrato de crédito, ele fica associado ao contrato de aluguel.

## US14 · Consultar contratos
**Como** agente, **quero** ver os contratos executados **para** acompanhar os aluguéis em vigor.

Critérios de aceitação
- [ ] A lista mostra o número, o cliente, o automóvel, o proprietário registrado, se tem crédito e a data.
- [ ] Dá para filtrar por com crédito e sem crédito.

## US15 · Manter automóveis
**Como** empresa (agente), **quero** cadastrar, editar e remover os automóveis da frota **para** que os clientes possam pedi-los.

Critérios de aceitação
- [ ] O automóvel tem matrícula (RENAVAM), ano, marca, modelo, placa (padrão Mercosul `ABC1D23`) e valor da diária.
- [ ] A placa e a matrícula são únicas.
- [ ] Um automóvel com contrato em vigor não pode ser removido.
