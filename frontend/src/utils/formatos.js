// Máscaras e validações dos documentos brasileiros usados nos formulários.

export const somenteDigitos = (valor) => (valor ?? '').replace(/\D/g, '')

function aplicarMascara(valor, padrao) {
  const digitos = somenteDigitos(valor)
  let i = 0
  let saida = ''
  for (const c of padrao) {
    if (i >= digitos.length) break
    saida += c === '9' ? digitos[i++] : c
  }
  return saida
}

export const mascaraCpf = (v) => aplicarMascara(v, '999.999.999-99')
export const mascaraCnpj = (v) => aplicarMascara(v, '99.999.999/9999-99')
export const mascaraCep = (v) => aplicarMascara(v, '99999-999')

function digitoVerificador(numeros, pesos) {
  const soma = pesos.reduce((total, peso, i) => total + Number(numeros[i]) * peso, 0)
  const resto = soma % 11
  return resto < 2 ? 0 : 11 - resto
}

export function cpfValido(valor) {
  const cpf = somenteDigitos(valor)
  if (cpf.length !== 11 || /^(\d)\1+$/.test(cpf)) return false
  const d1 = digitoVerificador(cpf, [10, 9, 8, 7, 6, 5, 4, 3, 2])
  const d2 = digitoVerificador(cpf, [11, 10, 9, 8, 7, 6, 5, 4, 3, 2])
  return d1 === Number(cpf[9]) && d2 === Number(cpf[10])
}

export function cnpjValido(valor) {
  const cnpj = somenteDigitos(valor)
  if (cnpj.length !== 14 || /^(\d)\1+$/.test(cnpj)) return false
  const d1 = digitoVerificador(cnpj, [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2])
  const d2 = digitoVerificador(cnpj, [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2])
  return d1 === Number(cnpj[12]) && d2 === Number(cnpj[13])
}

/** "6.200,50" → 6200.5 (ou null se não for um valor). */
export function lerMoeda(texto) {
  const limpo = (texto ?? '').replace(/[^\d,]/g, '').replace(',', '.')
  const numero = Number.parseFloat(limpo)
  return Number.isFinite(numero) ? numero : null
}

export const formatarMoeda = (numero) =>
  numero.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

/** Enquanto digita: mantém só dígitos e uma vírgula com até 2 casas. */
export function mascaraMoeda(texto) {
  const [inteiro = '', centavos] = (texto ?? '').replace(/[^\d,]/g, '').split(',')
  const milhar = inteiro.replace(/^0+(?=\d)/, '').replace(/\B(?=(\d{3})+(?!\d))/g, '.')
  return centavos === undefined ? milhar : `${milhar},${centavos.slice(0, 2)}`
}

/** 6200.5 → "6.200,50" (para preencher um campo com máscara de moeda). */
export const moedaParaCampo = (numero) => mascaraMoeda(Number(numero).toFixed(2).replace('.', ','))

/** "Mariana Costa" → "MC" */
export const iniciais = (nome) =>
  (nome ?? '')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('')

export const UFS = [
  'AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA',
  'PB', 'PE', 'PI', 'PR', 'RJ', 'RN', 'RO', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO',
]
