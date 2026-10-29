import { api } from './api.js'

// Frota de automóveis: todos consultam, só a empresa cadastra, edita e remove.

export const listarAutomoveis = (token) => api('/automoveis', { token })

export const cadastrarAutomovel = (token, dados) => api('/automoveis', { metodo: 'POST', corpo: dados, token })

export const atualizarAutomovel = (token, id, dados) => api(`/automoveis/${id}`, { metodo: 'PUT', corpo: dados, token })

export const removerAutomovel = (token, id) => api(`/automoveis/${id}`, { metodo: 'DELETE', token })
