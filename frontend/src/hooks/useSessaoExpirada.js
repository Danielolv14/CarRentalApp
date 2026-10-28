import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

/** Devolve uma função que encerra a sessão e leva ao login quando a API responde 401. */
export default function useSessaoExpirada() {
  const { sair } = useAuth()
  const navegar = useNavigate()
  return useCallback(() => {
    sair()
    navegar('/login', { replace: true, state: { aviso: 'Sua sessão expirou. Entre de novo.' } })
  }, [sair, navegar])
}
