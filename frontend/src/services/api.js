// Cliente HTTP único para a API Spring Boot. Todas as chamadas do front passam por aqui.
const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

/** Erro vindo da API: status HTTP, mensagem e, quando houver, a mensagem de cada campo. */
export class ErroApi extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem)
    this.status = status
    this.campos = campos ?? {}
  }
}

export async function api(caminho, { metodo = 'GET', corpo, token } = {}) {
  let resposta
  try {
    resposta = await fetch(`${BASE_URL}${caminho}`, {
      method: metodo,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: corpo ? JSON.stringify(corpo) : undefined,
    })
  } catch {
    throw new ErroApi(0, 'Não foi possível falar com o servidor. Confira se a API está rodando.')
  }

  if (!resposta.ok) {
    const erro = await resposta.json().catch(() => ({}))
    throw new ErroApi(resposta.status, erro.mensagem ?? `Erro ${resposta.status}.`, erro.campos)
  }
  return resposta.status === 204 ? null : resposta.json()
}
