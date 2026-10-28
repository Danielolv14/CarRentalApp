import { api } from './api.js'

export const buscarPerfil = (token) => api('/clientes/me', { token })

export const atualizarPerfil = (token, dados) => api('/clientes/me', { metodo: 'PUT', corpo: dados, token })

export const excluirConta = (token) => api('/clientes/me', { metodo: 'DELETE', token })
