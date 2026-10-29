import { Link, useNavigate } from 'react-router-dom'
import { formatarMoeda, iniciais } from '../utils/formatos.js'
import { nomeDoCarro, tempoDesde } from '../utils/pedidos.js'
import Icone from './Icone.jsx'
import MiniCarro from './MiniCarro.jsx'
import StatusBadge from './StatusBadge.jsx'

/** Ação de cada linha: avaliar (se o parecer é do agente), executar o contrato (aprovado) ou só ver. */
function acao(item) {
  const { pedido } = item
  if (item.podeAvaliar) return { rotulo: 'Avaliar', variante: 'btn-primary', para: `/agente/pedidos/${pedido.id}` }
  if (item.podeExecutarContrato) return { rotulo: 'Contrato', variante: 'btn-secondary', para: `/agente/pedidos/${pedido.id}/contrato` }
  return { rotulo: 'Ver', variante: 'btn-secondary', para: `/agente/pedidos/${pedido.id}` }
}

/** Tabela de pedidos do agente (fila do painel e tela de pedidos). itens vem de GET /agente/pedidos. */
export default function TabelaPedidosAgente({ itens, vazio = 'Nenhum pedido com esse filtro.' }) {
  const navegar = useNavigate()

  return (
    <table>
      <thead>
        <tr>
          <th>Pedido</th>
          <th>Cliente</th>
          <th>Automóvel</th>
          <th>Enviado</th>
          <th>Valor</th>
          <th>Crédito</th>
          <th>Status</th>
          <th className="td-acao">
            <span className="sr-only">Ações</span>
          </th>
        </tr>
      </thead>
      <tbody>
        {itens.map((item) => {
          const { pedido, cliente } = item
          const a = acao(item)
          return (
            <tr key={pedido.id} className="clicavel" onClick={() => navegar(a.para)}>
              <td className="mono forte nao-quebra">{pedido.numero}</td>
              <td>
                <div className="celula-cliente">
                  <span className="avatar pequeno">{iniciais(cliente.nome)}</span>
                  <span>{cliente.nome}</span>
                </div>
              </td>
              <td>
                <div className="celula-carro">
                  <MiniCarro automovel={pedido.automovel} largura={56} altura={38} />
                  <div>
                    <div className="forte">{nomeDoCarro(pedido.automovel)}</div>
                    <div className="passo-sub">
                      {pedido.automovel.ano} · {pedido.automovel.placa}
                    </div>
                  </div>
                </div>
              </td>
              <td className="suave">{tempoDesde(pedido.dataCriacao)}</td>
              <td className="mono">{formatarMoeda(pedido.valorEstimado)}</td>
              <td>
                {pedido.credito ? (
                  <span className="credito-sim" title={`Crédito pedido ao ${pedido.credito.banco}`}>
                    <Icone nome="landmark" tamanho={14} cor="#1E40AF" /> Sim
                  </span>
                ) : (
                  <span className="suave">Não</span>
                )}
              </td>
              <td>
                <StatusBadge status={pedido.status} />
              </td>
              <td className="td-acao">
                <Link className={`btn pequeno ${a.variante}`} to={a.para} onClick={(e) => e.stopPropagation()}>
                  {a.rotulo}
                </Link>
              </td>
            </tr>
          )
        })}
        {itens.length === 0 && (
          <tr>
            <td colSpan={8} className="sem-resultado">
              {vazio}
            </td>
          </tr>
        )}
      </tbody>
    </table>
  )
}
