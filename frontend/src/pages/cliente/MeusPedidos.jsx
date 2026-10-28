import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import MiniCarro from '../../components/MiniCarro.jsx'
import Modal from '../../components/Modal.jsx'
import StatusBadge from '../../components/StatusBadge.jsx'
import Toast from '../../components/Toast.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as pedidoService from '../../services/pedidoService.js'
import { formatarMoeda } from '../../utils/formatos.js'
import { formatarPeriodo, nomeDoCarro, STATUS } from '../../utils/pedidos.js'

/** UC05 Consultar pedidos (lado do cliente), com o cancelamento (UC07) direto da lista. */
export default function MeusPedidos() {
  const { token } = useAuth()
  const navegar = useNavigate()
  const sessaoExpirada = useSessaoExpirada()
  const [pedidos, setPedidos] = useState(null)
  const [erroCarga, setErroCarga] = useState('')
  const [aba, setAba] = useState('TODOS')
  const [busca, setBusca] = useState('')
  const [cancelando, setCancelando] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [aviso, setAviso] = useState(null)

  useEffect(() => {
    pedidoService
      .meusPedidos(token)
      .then(setPedidos)
      .catch((erro) => (erro.status === 401 ? sessaoExpirada() : setErroCarga(erro.message)))
  }, [token, sessaoExpirada])

  async function confirmarCancelamento() {
    setEnviando(true)
    try {
      const atualizado = await pedidoService.cancelarPedido(token, cancelando.id)
      setPedidos((lista) => lista.map((p) => (p.id === atualizado.id ? atualizado : p)))
      setAviso({ tipo: 'ok', titulo: `Pedido ${atualizado.numero} cancelado` })
    } catch (erro) {
      if (erro.status === 401) return sessaoExpirada()
      setAviso({ tipo: 'erro', titulo: 'Não foi possível cancelar', texto: erro.message })
    } finally {
      setEnviando(false)
      setCancelando(null)
    }
  }

  const contagem = (status) => (pedidos ?? []).filter((p) => status === 'TODOS' || p.status === status).length
  const termo = busca.trim().toLowerCase()
  const visiveis = (pedidos ?? []).filter(
    (p) =>
      (aba === 'TODOS' || p.status === aba) &&
      (!termo || p.numero.toLowerCase().includes(termo) || nomeDoCarro(p.automovel).toLowerCase().includes(termo)),
  )

  return (
    <LayoutApp>
      <div className="pagina-topo com-acao">
        <div>
          <h1>Meus pedidos</h1>
          <p className="sub">Todos os seus pedidos de aluguel e o status de cada um.</p>
        </div>
        <Botao icone="plus" onClick={() => navegar('/cliente/pedidos/novo')}>
          Novo pedido
        </Botao>
      </div>

      {erroCarga && (
        <div className="alerta-erro" role="alert">
          <Icone nome="alert" tamanho={18} cor="#B42318" />
          <div className="alerta-titulo">{erroCarga}</div>
        </div>
      )}
      {!pedidos && !erroCarga && (
        <div className="carregando-pagina">
          <span className="spinner escuro" aria-hidden="true" /> Carregando pedidos...
        </div>
      )}

      {pedidos && (
        <div className="card tabela">
          <div className="tabela-barra">
            <div className="abas" role="tablist" aria-label="Filtrar por status">
              {['TODOS', ...Object.keys(STATUS)].map((s) => (
                <button key={s} type="button" role="tab" aria-selected={aba === s} className={`aba ${aba === s ? 'ativa' : ''}`} onClick={() => setAba(s)}>
                  {s === 'TODOS' ? 'Todos' : STATUS[s].rotulo}
                  <span className="aba-contador">{contagem(s)}</span>
                </button>
              ))}
            </div>
            <div className="busca">
              <Icone nome="search" tamanho={16} cor="#6B7080" />
              <input aria-label="Buscar por número ou automóvel" placeholder="Buscar pedido ou carro" value={busca} onChange={(e) => setBusca(e.target.value)} />
            </div>
          </div>

          {pedidos.length === 0 ? (
            <div className="vazio">
              <div className="vazio-icone">
                <Icone nome="inbox" tamanho={28} cor="#3F4450" />
              </div>
              <div className="vazio-titulo">Você ainda não fez nenhum pedido</div>
              <p className="vazio-texto">Quando você enviar um pedido de aluguel, ele aparece aqui com o status da análise.</p>
              <Botao icone="plus" onClick={() => navegar('/cliente/pedidos/novo')}>
                Fazer meu primeiro pedido
              </Botao>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Pedido</th>
                  <th>Automóvel</th>
                  <th>Período</th>
                  <th>Valor</th>
                  <th>Status</th>
                  <th className="acoes-col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {visiveis.map((p) => (
                  <tr key={p.id}>
                    <td className="mono forte">
                      <Link to={`/cliente/pedidos/${p.id}`}>{p.numero}</Link>
                    </td>
                    <td>
                      <div className="celula-carro">
                        <MiniCarro automovel={p.automovel} />
                        <div>
                          <div className="forte">
                            {nomeDoCarro(p.automovel)} {p.automovel.ano}
                          </div>
                          <div className="passo-sub">Diária {formatarMoeda(p.automovel.valorDiaria)}</div>
                        </div>
                      </div>
                    </td>
                    <td className="mono">{formatarPeriodo(p.dataRetirada, p.dataDevolucao)}</td>
                    <td className="mono">{formatarMoeda(p.valorEstimado)}</td>
                    <td>
                      <StatusBadge status={p.status} />
                    </td>
                    <td className="acoes-col">
                      <div className="acoes">
                        <Link className="botao-icone pequeno" to={`/cliente/pedidos/${p.id}`} aria-label={`Ver ${p.numero}`} title="Ver detalhes">
                          <Icone nome="eye" tamanho={16} cor="#3F4450" />
                        </Link>
                        {p.podeSerAlterado && (
                          <button type="button" className="botao-icone pequeno" onClick={() => setCancelando(p)} aria-label={`Cancelar ${p.numero}`} title="Cancelar pedido">
                            <Icone nome="x" tamanho={16} cor="#B42318" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
                {visiveis.length === 0 && (
                  <tr>
                    <td colSpan={6} className="sem-resultado">
                      Nenhum pedido com esse filtro.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
          {pedidos.length > 0 && (
            <div className="tabela-rodape">
              <span className="suave">
                Mostrando {visiveis.length} de {pedidos.length} {pedidos.length === 1 ? 'pedido' : 'pedidos'}
              </span>
              <span className="suave">Cancelar só aparece em pedidos Enviado ou Em análise.</span>
            </div>
          )}
        </div>
      )}

      {cancelando && (
        <Modal
          titulo={`Cancelar o pedido ${cancelando.numero}?`}
          perigo
          aoFechar={() => !enviando && setCancelando(null)}
          acoes={
            <>
              <Botao variante="secondary" onClick={() => setCancelando(null)} disabled={enviando}>
                Manter pedido
              </Botao>
              <Botao variante="danger" icone="x" onClick={confirmarCancelamento} carregando={enviando}>
                Cancelar pedido
              </Botao>
            </>
          }
        >
          O pedido do {nomeDoCarro(cancelando.automovel)} sai da análise{cancelando.credito ? ' e o crédito solicitado também é cancelado' : ''}. Essa ação não pode ser desfeita.
        </Modal>
      )}

      {aviso && <Toast {...aviso} aoFechar={() => setAviso(null)} />}
    </LayoutApp>
  )
}
