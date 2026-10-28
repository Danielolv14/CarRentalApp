import { api } from './api.js'

export const entrar = (login, senha) => api('/auth/login', { metodo: 'POST', corpo: { login, senha } })

export const cadastrarCliente = (dados) => api('/auth/cadastro/cliente', { metodo: 'POST', corpo: dados })

export const cadastrarAgente = (dados) => api('/auth/cadastro/agente', { metodo: 'POST', corpo: dados })

export const usuarioAtual = (token) => api('/auth/me', { token })
