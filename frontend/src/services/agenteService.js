import { api } from './api.js'

// Lado do agente: fila de pedidos, análise, parecer, modificação e contratos.

/** Pedidos que o agente acompanha (a empresa vê todos; o banco, os que pediram crédito a ele). */
export const listarPedidos = (token) => api('/agente/pedidos', { token })

export const detalharPedido = (token, id) => api(`/agente/pedidos/${id}`, { token })

/** Ao abrir o pedido para avaliar: Enviado passa para Em análise. */
export const iniciarAnalise = (token, id) => api(`/agente/pedidos/${id}/analise`, { metodo: 'POST', token })

/** dados: { resultado, justificativa, credito?: { valor, parcelas, taxaMensal } } */
export const darParecer = (token, id, dados) => api(`/agente/pedidos/${id}/parecer`, { metodo: 'POST', corpo: dados, token })

/** dados: { automovelId, dataRetirada, dataDevolucao } */
export const modificarPedido = (token, id, dados) => api(`/agente/pedidos/${id}`, { metodo: 'PUT', corpo: dados, token })

export const executarContrato = (token, id, tipoProprietario) =>
  api(`/agente/pedidos/${id}/contrato`, { metodo: 'POST', corpo: { tipoProprietario }, token })

export const listarContratos = (token) => api('/agente/contratos', { token })
