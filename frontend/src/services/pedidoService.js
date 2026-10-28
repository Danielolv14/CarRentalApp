import { api } from './api.js'

export const listarAutomoveis = (token) => api('/automoveis', { token })

export const listarBancos = (token) => api('/bancos', { token })

export const criarPedido = (token, dados) => api('/pedidos', { metodo: 'POST', corpo: dados, token })

export const meusPedidos = (token) => api('/pedidos', { token })

export const detalharPedido = (token, id) => api(`/pedidos/${id}`, { token })

export const cancelarPedido = (token, id) => api(`/pedidos/${id}/cancelar`, { metodo: 'POST', token })
