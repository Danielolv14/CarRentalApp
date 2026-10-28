import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import * as authService from '../services/authService.js'

const CHAVE = 'rota.sessao'
const AuthContext = createContext(null)

function lerSessao() {
  try {
    const sessao = JSON.parse(localStorage.getItem(CHAVE))
    if (sessao && new Date(sessao.expiraEm) > new Date()) return sessao
  } catch {
    // sessão corrompida: começa deslogado
  }
  localStorage.removeItem(CHAVE)
  return null
}

/** Guarda o token e o usuário logado. A API confere o token em toda requisição. */
export function AuthProvider({ children }) {
  const [sessao, setSessao] = useState(lerSessao)

  const guardar = useCallback((resposta) => {
    const nova = { token: resposta.token, expiraEm: resposta.expiraEm, usuario: resposta.usuario }
    localStorage.setItem(CHAVE, JSON.stringify(nova))
    setSessao(nova)
    return nova.usuario
  }, [])

  const sair = useCallback(() => {
    localStorage.removeItem(CHAVE)
    setSessao(null)
  }, [])

  // Ao abrir o app, confirma com a API que o token guardado ainda vale.
  useEffect(() => {
    if (!sessao?.token) return
    authService.usuarioAtual(sessao.token).catch((erro) => {
      if (erro.status === 401) sair()
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const valor = useMemo(
    () => ({
      usuario: sessao?.usuario ?? null,
      token: sessao?.token ?? null,
      entrar: async (login, senha) => guardar(await authService.entrar(login, senha)),
      cadastrarCliente: async (dados) => guardar(await authService.cadastrarCliente(dados)),
      cadastrarAgente: async (dados) => guardar(await authService.cadastrarAgente(dados)),
      sair,
    }),
    [sessao, guardar, sair],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}

/** Página inicial de cada perfil depois do login. */
export const areaDoUsuario = (usuario) => (usuario?.perfil === 'AGENTE' ? '/agente' : '/cliente')
