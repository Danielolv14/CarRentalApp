import { api } from './api.js'

export const listarBancos = (token) => api('/bancos', { token })

export const criarPedido = (token, dados) => api('/pedidos', { metodo: 'POST', corpo: dados, token })

export const meusPedidos = (token) => api('/pedidos', { token })

export const detalharPedido = (token, id) => api(`/pedidos/${id}`, { token })

export const modificarPedido = (token, id, dados) => api(`/pedidos/${id}`, { metodo: 'PUT', corpo: dados, token })

export const cancelarPedido = (token, id) => api(`/pedidos/${id}/cancelar`, { metodo: 'POST', token })
