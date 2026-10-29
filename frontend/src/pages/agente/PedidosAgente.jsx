import { useEffect, useState } from 'react'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import TabelaPedidosAgente from '../../components/TabelaPedidosAgente.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as agenteService from '../../services/agenteService.js'
import { nomeDoCarro, PENDENTES } from '../../utils/pedidos.js'

const ABAS = [
  { chave: 'AVALIAR', rotulo: 'Para avaliar', filtro: (i) => PENDENTES.includes(i.pedido.status) },
  { chave: 'APROVADO', rotulo: 'Aprovados', filtro: (i) => i.pedido.status === 'APROVADO' },
  { chave: 'REPROVADO', rotulo: 'Reprovados', filtro: (i) => i.pedido.status === 'REPROVADO' },
  { chave: 'CONTRATADO', rotulo: 'Contratados', filtro: (i) => i.pedido.status === 'CONTRATADO' },
  { chave: 'CANCELADO', rotulo: 'Cancelados', filtro: (i) => i.pedido.status === 'CANCELADO' },
  { chave: 'TODOS', rotulo: 'Todos', filtro: () => true },
]

/** UC05 Consultar pedidos (lado do agente): a fila e os pedidos já avaliados, com abas por status e busca. */
export default function PedidosAgente() {
  const { usuario, token } = useAuth()
  const sessaoExpirada = useSessaoExpirada()
  const [pedidos, setPedidos] = useState(null)
  const [erro, setErro] = useState('')
  const [aba, setAba] = useState('AVALIAR')
  const [busca, setBusca] = useState('')

  useEffect(() => {
    agenteService
      .listarPedidos(token)
      .then(setPedidos)
      .catch((e) => (e.status === 401 ? sessaoExpirada() : setErro(e.message)))
  }, [token, sessaoExpirada])

  const lista = pedidos ?? []
  const filtroAba = ABAS.find((a) => a.chave === aba).filtro
  const termo = busca.trim().toLowerCase()
  const visiveis = lista.filter(
    (i) =>
      filtroAba(i) &&
      (!termo ||
        i.pedido.numero.toLowerCase().includes(termo) ||
        i.cliente.nome.toLowerCase().includes(termo) ||
        nomeDoCarro(i.pedido.automovel).toLowerCase().includes(termo)),
  )

  return (
    <LayoutApp>
      <div className="pagina-topo">
        <h1>Pedidos para avaliar</h1>
        <p className="sub">
          {usuario.tipoAgente === 'BANCO'
            ? 'Pedidos em que o cliente pediu crédito ao seu banco. O parecer positivo já concede o crédito.'
            : 'Todos os pedidos de aluguel. Os pedidos com crédito bancário são avaliados pelo banco escolhido.'}
        </p>
      </div>

      {erro && (
        <div className="alerta-erro" role="alert">
          <Icone nome="alert" tamanho={18} cor="#B42318" />
          <div className="alerta-titulo">{erro}</div>
        </div>
      )}
      {!pedidos && !erro && (
        <div className="carregando-pagina">
          <span className="spinner escuro" aria-hidden="true" /> Carregando pedidos...
        </div>
      )}

      {pedidos && (
        <div className="card tabela">
          <div className="tabela-barra">
            <div className="abas" role="tablist" aria-label="Filtrar por status">
              {ABAS.map((a) => (
                <button key={a.chave} type="button" role="tab" aria-selected={aba === a.chave} className={`aba ${aba === a.chave ? 'ativa' : ''}`} onClick={() => setAba(a.chave)}>
                  {a.rotulo}
                  <span className="aba-contador">{lista.filter(a.filtro).length}</span>
                </button>
              ))}
            </div>
            <div className="busca">
              <Icone nome="search" tamanho={16} cor="#6B7080" />
              <input aria-label="Buscar por número, cliente ou automóvel" placeholder="Buscar pedido, cliente ou carro" value={busca} onChange={(e) => setBusca(e.target.value)} />
            </div>
          </div>

          <TabelaPedidosAgente itens={visiveis} vazio={lista.length ? 'Nenhum pedido com esse filtro.' : 'Nenhum pedido por aqui ainda.'} />

          <div className="tabela-rodape">
            <span className="suave">
              Mostrando {visiveis.length} de {lista.length} {lista.length === 1 ? 'pedido' : 'pedidos'}
            </span>
            <span className="suave">Do mais antigo para o mais novo.</span>
          </div>
        </div>
      )}
    </LayoutApp>
  )
}
