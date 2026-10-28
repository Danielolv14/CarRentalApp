// Validações compartilhadas pelo cadastro e pelo perfil do cliente.
// As chaves dos erros seguem os nomes de campo que a API devolve (ex.: "endereco.cep").
import { lerMoeda, somenteDigitos } from './formatos.js'

export const MAXIMO_RENDIMENTOS = 3

export function validarEndereco(d) {
  const e = {}
  if (somenteDigitos(d.cep).length !== 8) e['endereco.cep'] = 'CEP inválido.'
  if (!d.logradouro.trim()) e['endereco.logradouro'] = 'Informe a rua.'
  if (!d.numero.trim()) e['endereco.numero'] = 'Informe o número.'
  if (!d.bairro.trim()) e['endereco.bairro'] = 'Informe o bairro.'
  if (!d.cidade.trim()) e['endereco.cidade'] = 'Informe a cidade.'
  return e
}

export function validarRendimentos(rendimentos) {
  const e = {}
  rendimentos.forEach((r, i) => {
    if (!r.entidade.trim()) e[`rendimentos[${i}].entidadeEmpregadora`] = 'Informe a entidade empregadora.'
    if (!(lerMoeda(r.valor) > 0)) e[`rendimentos[${i}].valor`] = 'Informe o rendimento.'
  })
  if (rendimentos.length > MAXIMO_RENDIMENTOS) e.rendimentos = 'Informe no máximo 3 rendimentos.'
  return e
}

/** Formato que a API espera para o endereço e os rendimentos. */
export const enderecoParaApi = (d) => ({
  cep: somenteDigitos(d.cep),
  logradouro: d.logradouro,
  numero: d.numero,
  bairro: d.bairro,
  cidade: d.cidade,
  uf: d.uf,
})

export const rendimentosParaApi = (rendimentos) =>
  rendimentos.map((r) => ({ entidadeEmpregadora: r.entidade.trim(), valor: lerMoeda(r.valor) }))
