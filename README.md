# API de Tarefas

API REST para gerenciamento de tarefas de estudo (matéria, tópico, data/hora e status).

## Sumário

- [Visão Geral](#visão-geral)
- [Base URL](#base-url)
- [Autenticação](#autenticação)
- [Modelos de Dados](#modelos-de-dados)
  - [Tarefa](#tarefa)
  - [Status](#status)
  - [TarefaDTO](#tarefadto)
- [Formato de Data e Hora](#formato-de-data-e-hora)
- [Endpoints](#endpoints)
  - [Cadastrar Tarefa](#1-cadastrar-tarefa)
  - [Listar Todas as Tarefas](#2-listar-todas-as-tarefas)
  - [Filtrar Tarefas por Data](#3-filtrar-tarefas-por-data)
  - [Atualizar Tarefa Parcialmente](#4-atualizar-tarefa-parcialmente)
  - [Atualizar Tarefa Completamente](#5-atualizar-tarefa-completamente)
  - [Deletar Tarefa](#6-deletar-tarefa)
- [Tratamento de Erros](#tratamento-de-erros)
- [Observações Técnicas](#observações-técnicas)

---

## Visão Geral

A API expõe operações de **CRUD** para o recurso `Tarefa`, permitindo cadastro, listagem, filtragem por intervalo de datas, atualização (parcial ou completa) e remoção.

| Item | Detalhe |
|---|---|
| Recurso principal | `Tarefa` |
| Prefixo das rotas | `/tarefas` |
| Formato de dados | JSON |

## Base URL

```
http://localhost:8080/tarefas
```

> Ajuste a porta/host conforme o ambiente de deploy.

## Autenticação

Atualmente **todas as rotas são públicas**. O `SecurityConfig` libera qualquer requisição (`anyRequest().permitAll()`) e desabilita CSRF. Não é necessário enviar token ou credenciais.

⚠️ Não há controle de acesso implementado — ver [Observações Técnicas](#observações-técnicas).

## Modelos de Dados

### Tarefa

Entidade persistida no banco (tabela `Estudos_Tarefas`).

| Campo | Tipo | Descrição |
|---|---|---|
| `ID` | `Long` | Identificador único, gerado automaticamente (`IDENTITY`) |
| `materia` | `String` | Nome da matéria de estudo |
| `topico` | `String` | Tópico específico dentro da matéria |
| `dataHora` | `LocalDateTime` | Data e hora associada à tarefa (formato `dd/MM/yyyy HH:mm:ss`) |
| `status` | `Status` | Situação atual da tarefa |

**Exemplo:**

```json
{
  "ID": 1,
  "materia": "Matemática",
  "topico": "Equações Diferenciais",
  "dataHora": "25/12/2026 14:30:00",
  "status": "PENDENTE"
}
```

### Status

Enum que representa o estado da tarefa.

| Valor | Descrição |
|---|---|
| `PENDENTE` | Tarefa ainda não realizada |
| `CONCLUIDO` | Tarefa finalizada |
| `CANCELADO` | Tarefa cancelada |

### TarefaDTO

Usado no corpo da requisição para **atualização completa** (`PUT`), com validação via Bean Validation.

| Campo | Tipo | Validação |
|---|---|---|
| `materia` | `String` | `@NotBlank` — "Matéria não pode ser vazio" |
| `topico` | `String` | `@NotBlank` — "Tópico não pode ser vazio" |
| `dataHora` | `LocalDateTime` | `@NotNull`, `@Future` — "A data da tarefa deve ser futura" |
| `status` | `Status` | `@NotNull` — "É obrigatorio passar o status" |

**Exemplo:**

```json
{
  "materia": "História",
  "topico": "Revolução Francesa",
  "dataHora": "01/01/2027 09:00:00",
  "status": "PENDENTE"
}
```

## Formato de Data e Hora

Todas as datas seguem o padrão:

```
dd/MM/yyyy HH:mm:ss
```

**Exemplo:** `25/12/2026 14:30:00`

---

## Endpoints

### 1. Cadastrar Tarefa

Cria uma nova tarefa.

| | |
|---|---|
| **Método** | `POST` |
| **Rota** | `/tarefas/cadastro` |
| **Corpo** | objeto `Tarefa` (JSON) |

**Requisição:**

```json
{
  "materia": "Matemática",
  "topico": "Equações Diferenciais",
  "dataHora": "25/12/2026 14:30:00",
  "status": "PENDENTE"
}
```

**Resposta — `201 Created`:**

```json
{
  "ID": 1,
  "materia": "Matemática",
  "topico": "Equações Diferenciais",
  "dataHora": "25/12/2026 14:30:00",
  "status": "PENDENTE"
}
```

> ⚠️ Este endpoint recebe a entidade `Tarefa` diretamente (não o `TarefaDTO`), portanto **não há validação** de campos obrigatórios ou de data futura nesta rota.

---

### 2. Listar Todas as Tarefas

Retorna todas as tarefas cadastradas.

| | |
|---|---|
| **Método** | `GET` |
| **Rota** | `/tarefas` |

**Resposta — `200 OK`:**

```json
[
  {
    "ID": 1,
    "materia": "Matemática",
    "topico": "Equações Diferenciais",
    "dataHora": "25/12/2026 14:30:00",
    "status": "PENDENTE"
  },
  {
    "ID": 2,
    "materia": "História",
    "topico": "Revolução Francesa",
    "dataHora": "01/01/2027 09:00:00",
    "status": "CONCLUIDO"
  }
]
```

---

### 3. Filtrar Tarefas por Data

Retorna as tarefas cuja `dataHora` está entre os valores `inicio` e `fim` informados.

| | |
|---|---|
| **Método** | `GET` |
| **Rota** | `/tarefas/dataHora` |

**Query Parameters:**

| Parâmetro | Tipo | Obrigatório | Formato |
|---|---|---|---|
| `inicio` | `String` | Sim | `dd/MM/yyyy HH:mm:ss` |
| `fim` | `String` | Sim | `dd/MM/yyyy HH:mm:ss` |

**Exemplo de requisição:**

```
GET /tarefas/dataHora?inicio=01/12/2026 00:00:00&fim=31/12/2026 23:59:59
```

**Resposta — `200 OK`:**

```json
[
  {
    "ID": 1,
    "materia": "Matemática",
    "topico": "Equações Diferenciais",
    "dataHora": "25/12/2026 14:30:00",
    "status": "PENDENTE"
  }
]
```

> ℹ️ Os parâmetros contêm espaços (`:` e barras); em uma chamada HTTP real eles devem ser **URL-encoded** (ex.: espaço vira `%20`).

---

### 4. Atualizar Tarefa Parcialmente

Atualiza um ou mais campos de uma tarefa existente, sem exigir o objeto completo.

| | |
|---|---|
| **Método** | `PATCH` |
| **Rota** | `/tarefas/{id}` |
| **Corpo** | objeto JSON com os campos a alterar |

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `Long` | Identificador da tarefa |

**Exemplo de requisição:**

```json
{
  "status": "CONCLUIDO"
}
```

**Resposta — `200 OK`:**

```json
{
  "ID": 1,
  "materia": "Matemática",
  "topico": "Equações Diferenciais",
  "dataHora": "25/12/2026 14:30:00",
  "status": "CONCLUIDO"
}
```

**Resposta — `404 Not Found`** (tarefa não encontrada):

```json
{
  "message": "Tarefa com id 99não encontrada!"
}
```

> ⚠️ Este endpoint aplica os campos recebidos via **reflexão** (`ReflectionUtils`), sobrescrevendo diretamente qualquer atributo de `Tarefa` cujo nome coincida com uma chave do JSON — incluindo `ID`. Não há checagem de campos permitidos.

---

### 5. Atualizar Tarefa Completamente

Substitui todos os campos de uma tarefa existente. Requer o objeto completo e validado (`TarefaDTO`).

| | |
|---|---|
| **Método** | `PUT` |
| **Rota** | `/tarefas/{id}` |
| **Corpo** | objeto `TarefaDTO` (JSON) |

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `Long` | Identificador da tarefa |

**Exemplo de requisição:**

```json
{
  "materia": "Física",
  "topico": "Termodinâmica",
  "dataHora": "10/03/2027 08:00:00",
  "status": "PENDENTE"
}
```

**Resposta — `200 OK`:**

```json
{
  "ID": 1,
  "materia": "Física",
  "topico": "Termodinâmica",
  "dataHora": "10/03/2027 08:00:00",
  "status": "PENDENTE"
}
```

**Resposta — `404 Not Found`** (tarefa não encontrada, corpo vazio)

**Resposta — `400 Bad Request`** (falha de validação, ex.: campo em branco ou data não futura)

---

### 6. Deletar Tarefa

Remove uma tarefa existente.

| | |
|---|---|
| **Método** | `DELETE` |
| **Rota** | `/tarefas/{id}` |

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `Long` | Identificador da tarefa |

**Resposta — `200 OK`** (retorna a tarefa deletada):

```json
{
  "ID": 1,
  "materia": "Matemática",
  "topico": "Equações Diferenciais",
  "dataHora": "25/12/2026 14:30:00",
  "status": "PENDENTE"
}
```

**Resposta — `404 Not Found`** (tarefa não encontrada, corpo vazio)

---

## Tratamento de Erros

| Código | Situação |
|---|---|
| `400 Bad Request` | Falha de validação (`TarefaDTO`) ou erro de parsing de data |
| `404 Not Found` | Recurso não encontrado (`ResourceNotFoundException`, ou `Optional` vazio nos endpoints `PUT`/`DELETE`) |
| `201 Created` | Tarefa criada com sucesso |
| `200 OK` | Operação concluída com sucesso |

A exceção `ResourceNotFoundException` é anotada com `@ResponseStatus(HttpStatus.NOT_FOUND)`, retornando automaticamente status `404` com a mensagem definida.

## Observações Técnicas

Pontos identificados no código-fonte que podem merecer atenção em uma revisão futura:

- **Segurança desabilitada**: todas as rotas estão liberadas (`permitAll()`) e o CSRF está desativado — adequado apenas para ambiente de desenvolvimento.
- **Cadastro sem validação**: o endpoint `POST /tarefas/cadastro` recebe a entidade `Tarefa` diretamente, não o `TarefaDTO`, então as regras de validação (`@NotBlank`, `@Future`, etc.) não se aplicam nesse fluxo.
- **PATCH via reflexão**: `alterarParcialmente` usa `ReflectionUtils` para setar qualquer campo presente no JSON recebido, sem lista de permissões (*allowlist*), o que permite, por exemplo, sobrescrever o campo `ID`.
- **Formato de data inconsistente**: o parâmetro `fim` em `filtrarPorData` usa o padrão `YYYY` (ano da semana ISO) em vez de `yyyy` (ano do calendário) na anotação `@DateTimeFormat`, o que pode gerar comportamento inesperado em datas de virada de ano.
