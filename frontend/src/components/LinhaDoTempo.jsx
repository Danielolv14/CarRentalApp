import Icone from './Icone.jsx'
import { formatarData, formatarDataHora } from '../utils/pedidos.js'

/** Monta as 4 etapas do pedido a partir do status (Enviado → Em análise → Parecer → Contrato). */
function etapas(pedido) {
  const { status } = pedido
  const parecer = pedido.pareceres.at(-1)
  const enviado = { titulo: 'Enviado', sub: formatarDataHora(pedido.dataCriacao), estado: 'feito' }

  if (status === 'CANCELADO') {
    return [
      enviado,
      { titulo: 'Cancelado', sub: 'Pedido cancelado pelo cliente', estado: 'erro' },
      { titulo: 'Parecer', sub: 'Não haverá', estado: '' },
      { titulo: 'Contrato', sub: 'Não haverá', estado: '' },
    ]
  }

  const analisado = ['APROVADO', 'REPROVADO', 'CONTRATADO'].includes(status)
  return [
    enviado,
    {
      titulo: 'Em análise',
      sub: status === 'ENVIADO' ? 'Aguardando um agente' : analisado ? 'Análise concluída' : 'Agente analisando',
      estado: status === 'ENVIADO' ? '' : status === 'EM_ANALISE' ? 'atual' : 'feito',
    },
    {
      titulo: 'Parecer',
      sub: parecer ? `${parecer.resultado === 'POSITIVO' ? 'Positivo' : 'Negativo'} · ${parecer.agente}` : 'Positivo ou negativo',
      estado: status === 'REPROVADO' ? 'erro' : analisado ? 'feito' : '',
    },
    {
      titulo: 'Contrato',
      sub:
        status === 'CONTRATADO'
          ? pedido.contrato ? `${pedido.contrato.numero} · ${formatarData(pedido.contrato.dataExecucao)}` : 'Contrato executado'
          : status === 'APROVADO' ? 'Aguardando execução' : 'Execução pelo agente',
      estado: status === 'CONTRATADO' ? 'feito' : status === 'APROVADO' ? 'atual' : '',
    },
  ]
}

export default function LinhaDoTempo({ pedido }) {
  const lista = etapas(pedido)
  return (
    <ol className="linha-tempo">
      {lista.map((e, i) => (
        <li key={e.titulo} className={`lt-etapa ${e.estado}`}>
          <div className="lt-trilho">
            <span className="lt-ponto">
              {e.estado === 'feito' && <Icone nome="check" tamanho={14} cor="#FFFFFF" traco={3} />}
              {e.estado === 'erro' && <span className="lt-x">×</span>}
              {e.estado === 'atual' && <span className="lt-miolo" />}
            </span>
            {i < lista.length - 1 && <span className={`lt-linha ${e.estado === 'feito' ? 'feito' : ''}`} />}
          </div>
          <div className="lt-titulo">{e.titulo}</div>
          <div className="lt-sub">{e.sub}</div>
        </li>
      ))}
    </ol>
  )
}
