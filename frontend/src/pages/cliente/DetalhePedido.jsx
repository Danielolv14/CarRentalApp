import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import CarroSvg from '../../components/CarroSvg.jsx'
import DonoPill from '../../components/DonoPill.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import LinhaDoTempo from '../../components/LinhaDoTempo.jsx'
import Modal from '../../components/Modal.jsx'
import ModalModificarPedido from '../../components/ModalModificarPedido.jsx'
import Placa from '../../components/Placa.jsx'
import StatusBadge from '../../components/StatusBadge.jsx'
import Toast from '../../components/Toast.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as automovelService from '../../services/automovelService.js'
import * as pedidoService from '../../services/pedidoService.js'
import { formatarMoeda } from '../../utils/formatos.js'
import { coresDoCarro, formatarData, formatarDataHora, formatarTaxa, nomeDoCarro } from '../../utils/pedidos.js'

/** Detalhe do pedido: linha do tempo do status, automóvel, período, crédito e parecer (UC05, UC06 e UC07). */
export default function DetalhePedido() {
  const { id } = useParams()
  const { token } = useAuth()
  const navegar = useNavigate()
  const local = useLocation()
  const sessaoExpirada = useSessaoExpirada()
  const [pedido, setPedido] = useState(null)
  const [erro, setErro] = useState('')
  const [confirmar, setConfirmar] = useState(false)
  const [cancelando, setCancelando] = useState(false)
  const [modificando, setModificando] = useState(false)
  const [automoveis, setAutomoveis] = useState([])
  const [salvandoAlteracao, setSalvandoAlteracao] = useState(false)
  const [erroAlteracao, setErroAlteracao] = useState('')
  const [aviso, setAviso] = useState(local.state?.aviso ? { tipo: 'ok', titulo: local.state.aviso, texto: 'Agora é só acompanhar a análise.' } : null)

  useEffect(() => {
    pedidoService
      .detalharPedido(token, id)
      .then(setPedido)
      .catch((e) => (e.status === 401 ? sessaoExpirada() : setErro(e.message)))
  }, [token, id, sessaoExpirada])

  async function abrirModificacao() {
    setErroAlteracao('')
    setModificando(true)
    try {
      setAutomoveis(await automovelService.listarAutomoveis(token))
    } catch (e) {
      if (e.status === 401) sessaoExpirada()
    }
  }

  async function salvarModificacao(dados) {
    setSalvandoAlteracao(true)
    setErroAlteracao('')
    try {
      setPedido(await pedidoService.modificarPedido(token, id, dados))
      setModificando(false)
      setAviso({ tipo: 'ok', titulo: 'Pedido modificado', texto: 'O valor estimado foi recalculado.' })
    } catch (e) {
      if (e.status === 401) return sessaoExpirada()
      setErroAlteracao(e.message)
    } finally {
      setSalvandoAlteracao(false)
    }
  }

  async function cancelar() {
    setCancelando(true)
    try {
      setPedido(await pedidoService.cancelarPedido(token, id))
      setAviso({ tipo: 'ok', titulo: 'Pedido cancelado' })
    } catch (e) {
      if (e.status === 401) return sessaoExpirada()
      setAviso({ tipo: 'erro', titulo: 'Não foi possível cancelar', texto: e.message })
    } finally {
      setCancelando(false)
      setConfirmar(false)
    }
  }

  if (erro) {
    return (
      <LayoutApp>
        <div className="card vazio">
          <div className="vazio-icone">
            <Icone nome="alert" tamanho={28} cor="#3F4450" />
          </div>
          <div className="vazio-titulo">{erro}</div>
          <Botao variante="secondary" icone="chevleft" onClick={() => navegar('/cliente/pedidos')}>
            Voltar para Meus pedidos
          </Botao>
        </div>
      </LayoutApp>
    )
  }

  if (!pedido) {
    return (
      <LayoutApp>
        <div className="carregando-pagina">
          <span className="spinner escuro" aria-hidden="true" /> Carregando pedido...
        </div>
      </LayoutApp>
    )
  }

  const a = pedido.automovel
  const [cor, fundo] = coresDoCarro(a)
  const parecer = pedido.pareceres.at(-1)

  return (
    <LayoutApp>
      <div className="pagina-topo com-acao">
        <div>
          <nav className="trilha" aria-label="Você está em">
            <Link to="/cliente/pedidos">Meus pedidos</Link>
            <Icone nome="chevright" tamanho={14} cor="#9A9DA6" />
            <span>{pedido.numero}</span>
          </nav>
          <div className="titulo-com-selo">
            <h1>Pedido {pedido.numero}</h1>
            <StatusBadge status={pedido.status} />
          </div>
          <p className="sub">
            Enviado em {formatarDataHora(pedido.dataCriacao)}.
            {pedido.podeSerAlterado && ' Você ainda pode modificar ou cancelar enquanto ele estiver em análise.'}
          </p>
        </div>
        {pedido.podeSerAlterado && (
          <div className="topo-acoes">
            <Botao variante="secondary" icone="pencil" onClick={abrirModificacao}>
              Modificar pedido
            </Botao>
            <Botao variante="danger-outline" icone="x" onClick={() => setConfirmar(true)}>
              Cancelar pedido
            </Botao>
          </div>
        )}
      </div>

      <div className="card card-corpo">
        <LinhaDoTempo pedido={pedido} />
      </div>

      <div className="detalhe-colunas">
        <div className="detalhe-principal">
          <section className="card">
            <h2 className="card-cabeca">Automóvel</h2>
            <div className="auto-caixa">
              <div className="auto-img" style={{ background: fundo }}>
                <CarroSvg cor={cor} largura={230} />
              </div>
              <div className="auto-info">
                <div className="resumo-modelo">{nomeDoCarro(a)}</div>
                <div className="passo-sub">Ano {a.ano}</div>
                <Placa numero={a.placa} />
                <dl className="linhas-info">
                  <div><dt>Matrícula (RENAVAM)</dt><dd className="mono">{a.matricula}</dd></div>
                  <div><dt>Diária</dt><dd className="mono">{formatarMoeda(pedido.valorDiaria)}</dd></div>
                </dl>
              </div>
            </div>
          </section>

          <section className="card">
            <h2 className="card-cabeca">Período e valor</h2>
            <div className="card-corpo">
              <dl className="linhas-info">
                <div><dt>Retirada</dt><dd className="mono">{formatarData(pedido.dataRetirada)}</dd></div>
                <div><dt>Devolução</dt><dd className="mono">{formatarData(pedido.dataDevolucao)}</dd></div>
                <div><dt>Diárias</dt><dd className="mono">{pedido.diarias} × {formatarMoeda(pedido.valorDiaria)}</dd></div>
              </dl>
              <div className="total-pedido">
                <span>Valor estimado</span>
                <span className="mono">{formatarMoeda(pedido.valorEstimado)}</span>
              </div>
            </div>
          </section>
        </div>

        <div className="detalhe-lateral">
          <section className="card">
            <h2 className="card-cabeca">Crédito bancário</h2>
            <div className="card-corpo">
              {pedido.credito ? (
                <dl className="linhas-info">
                  <div><dt>Banco</dt><dd>{pedido.credito.banco}</dd></div>
                  <div><dt>Valor pedido</dt><dd className="mono">{formatarMoeda(pedido.credito.valor)}</dd></div>
                  {pedido.credito.concedido ? (
                    <>
                      <div><dt>Concedido</dt><dd className="mono">{formatarMoeda(pedido.credito.concedido.valor)}</dd></div>
                      <div><dt>Parcelas</dt><dd className="mono">{pedido.credito.concedido.parcelas}x de {formatarMoeda(pedido.credito.concedido.valorParcela)}</dd></div>
                      <div><dt>Taxa</dt><dd className="mono">{formatarTaxa(pedido.credito.concedido.taxaMensal)} ao mês</dd></div>
                    </>
                  ) : (
                    <div><dt>Situação</dt><dd>{pedido.status === 'REPROVADO' ? 'Não concedido' : 'Em análise pelo banco'}</dd></div>
                  )}
                </dl>
              ) : (
                <p className="suave">Sem crédito bancário neste pedido.</p>
              )}
            </div>
          </section>

          <section className="card">
            <h2 className="card-cabeca">Parecer do agente</h2>
            <div className="card-corpo">
              {parecer ? (
                <div className={`parecer ${parecer.resultado === 'POSITIVO' ? 'positivo' : 'negativo'}`}>
                  <div className="parecer-titulo">
                    Parecer {parecer.resultado === 'POSITIVO' ? 'positivo' : 'negativo'} · {parecer.agente}
                  </div>
                  <p className="parecer-texto">“{parecer.justificativa}” · {formatarData(parecer.data)}</p>
                </div>
              ) : pedido.status === 'CANCELADO' ? (
                <p className="suave">O pedido foi cancelado antes do parecer.</p>
              ) : (
                <div className="aguardando">
                  <Icone nome="clock" tamanho={20} cor="#92400E" />
                  <div>
                    <div className="aguardando-titulo">Aguardando parecer</div>
                    <p className="aguardando-texto">Um agente vai fazer a análise financeira do seu pedido. O parecer aparece aqui, com a justificativa.</p>
                  </div>
                </div>
              )}
            </div>
          </section>

          {pedido.contrato && (
            <section className="card">
              <h2 className="card-cabeca">Contrato</h2>
              <div className="card-corpo">
                <dl className="linhas-info">
                  <div><dt>Nº do contrato</dt><dd className="mono">{pedido.contrato.numero}</dd></div>
                  <div><dt>Executado em</dt><dd className="mono">{formatarData(pedido.contrato.dataExecucao)}</dd></div>
                  <div><dt>Proprietário do automóvel</dt><dd><DonoPill tipo={pedido.contrato.tipoProprietario} /></dd></div>
                </dl>
              </div>
            </section>
          )}
        </div>
      </div>

      {confirmar && (
        <Modal
          titulo={`Cancelar o pedido ${pedido.numero}?`}
          perigo
          aoFechar={() => !cancelando && setConfirmar(false)}
          acoes={
            <>
              <Botao variante="secondary" onClick={() => setConfirmar(false)} disabled={cancelando}>
                Manter pedido
              </Botao>
              <Botao variante="danger" icone="x" onClick={cancelar} carregando={cancelando}>
                Cancelar pedido
              </Botao>
            </>
          }
        >
          O pedido sai da análise{pedido.credito ? ` e o crédito solicitado ao ${pedido.credito.banco} também é cancelado` : ''}. Essa ação não pode ser desfeita.
        </Modal>
      )}

      {modificando && (
        <ModalModificarPedido
          pedido={pedido}
          automoveis={automoveis}
          aoSalvar={salvarModificacao}
          aoFechar={() => setModificando(false)}
          salvando={salvandoAlteracao}
          erroApi={erroAlteracao}
        />
      )}
      {aviso && <Toast {...aviso} aoFechar={() => setAviso(null)} />}
    </LayoutApp>
  )
}
