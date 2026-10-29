// Apoio às telas de pedido: status, cores dos carros e datas.

export const STATUS = {
  ENVIADO: { rotulo: 'Enviado', classe: 'enviado' },
  EM_ANALISE: { rotulo: 'Em análise', classe: 'analise' },
  APROVADO: { rotulo: 'Aprovado', classe: 'aprovado' },
  REPROVADO: { rotulo: 'Reprovado', classe: 'reprovado' },
  CONTRATADO: { rotulo: 'Contratado', classe: 'contratado' },
  CANCELADO: { rotulo: 'Cancelado', classe: 'cancelado' },
}

/** Cor da lataria e do fundo de cada modelo (as mesmas dos wireframes); os outros caem numa paleta fixa. */
const CORES_MODELO = {
  argo: ['#C2410C', '#FBEDE5'],
  polo: ['#2B59C3', '#E8EEFC'],
  onix: ['#BDB8AD', '#F1EFEA'],
  hb20: ['#7D838D', '#EEF0F3'],
  renegade: ['#4D6B3C', '#EAF0E5'],
  corolla: ['#2A2F38', '#ECEDEF'],
}
const PALETA = [
  ['#2B59C3', '#E8EEFC'],
  ['#C2410C', '#FBEDE5'],
  ['#4D6B3C', '#EAF0E5'],
  ['#7D838D', '#EEF0F3'],
  ['#2A2F38', '#ECEDEF'],
]

export function coresDoCarro(automovel) {
  const chave = (automovel?.modelo ?? '').toLowerCase()
  if (CORES_MODELO[chave]) return CORES_MODELO[chave]
  const soma = [...chave].reduce((total, c) => total + c.charCodeAt(0), 0)
  return PALETA[soma % PALETA.length]
}

export const nomeDoCarro = (a) => `${a.marca} ${a.modelo}`

// ---------- datas (a API usa AAAA-MM-DD)

const doisDigitos = (n) => String(n).padStart(2, '0')

export function hojeIso(somarDias = 0) {
  const d = new Date()
  d.setDate(d.getDate() + somarDias)
  return `${d.getFullYear()}-${doisDigitos(d.getMonth() + 1)}-${doisDigitos(d.getDate())}`
}

/** "2026-11-05" → "05/11/2026" */
export function formatarData(iso) {
  if (!iso) return ''
  const [a, m, d] = iso.slice(0, 10).split('-')
  return `${d}/${m}/${a}`
}

/** "2026-10-28T14:32:10" → "28/10/2026 às 14:32" */
export function formatarDataHora(iso) {
  if (!iso) return ''
  return `${formatarData(iso)} às ${iso.slice(11, 16)}`
}

/** Período curto: "05/11 a 12/11/2026" */
export function formatarPeriodo(retirada, devolucao) {
  return `${formatarData(retirada).slice(0, 5)} a ${formatarData(devolucao)}`
}

export function diariasEntre(retirada, devolucao) {
  if (!retirada || !devolucao) return 0
  const dias = (new Date(devolucao) - new Date(retirada)) / 86_400_000
  return dias > 0 ? Math.round(dias) : 0
}

// ---------- lado do agente

/** Status em que o pedido ainda espera o parecer. */
export const PENDENTES = ['ENVIADO', 'EM_ANALISE']

/** Em nome de quem o automóvel fica registrado no contrato. */
export const PROPRIETARIOS = {
  CLIENTE: { rotulo: 'Cliente', icone: 'user' },
  EMPRESA: { rotulo: 'Empresa', icone: 'building' },
  BANCO: { rotulo: 'Banco', icone: 'landmark' },
}

/** "2026-10-28T14:32:10" → "há 3 horas", "há 2 dias" */
export function tempoDesde(iso) {
  const minutos = Math.max(0, Math.floor((Date.now() - new Date(iso)) / 60_000))
  if (minutos < 60) return minutos < 2 ? 'agora há pouco' : `há ${minutos} min`
  const horas = Math.floor(minutos / 60)
  if (horas < 24) return `há ${horas} ${horas === 1 ? 'hora' : 'horas'}`
  const dias = Math.floor(horas / 24)
  return `há ${dias} ${dias === 1 ? 'dia' : 'dias'}`
}

/** Parcela fixa pela tabela Price, a mesma conta do back-end: PMT = PV · i / (1 − (1 + i)^−n). */
export function calcularParcela(valor, taxaMensal, parcelas) {
  if (!(valor > 0) || !(parcelas >= 1)) return 0
  const i = taxaMensal / 100
  if (!(i > 0)) return valor / parcelas
  const fator = (1 + i) ** parcelas
  return (valor * i * fator) / (fator - 1)
}

/** 1.9 → "1,90%" */
export const formatarTaxa = (taxa) => `${Number(taxa).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 4 })}%`
