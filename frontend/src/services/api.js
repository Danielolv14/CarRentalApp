// Cliente HTTP único para a API Spring Boot. Todas as chamadas do front passam por aqui.
const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export async function api(caminho, { metodo = 'GET', corpo, token } = {}) {
  const resposta = await fetch(`${BASE_URL}${caminho}`, {
    method: metodo,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: corpo ? JSON.stringify(corpo) : undefined,
  })

  if (!resposta.ok) {
    const erro = await resposta.json().catch(() => ({}))
    throw new Error(erro.mensagem ?? `Erro ${resposta.status} ao chamar ${caminho}`)
  }
  return resposta.status === 204 ? null : resposta.json()
}
