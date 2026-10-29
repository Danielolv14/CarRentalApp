import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import Campo from '../../components/Campo.jsx'
import CarCard from '../../components/CarCard.jsx'
import CarroSvg from '../../components/CarroSvg.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import Placa from '../../components/Placa.jsx'
import Toast from '../../components/Toast.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as automovelService from '../../services/automovelService.js'
import * as pedidoService from '../../services/pedidoService.js'
import { formatarMoeda, lerMoeda, mascaraMoeda, moedaParaCampo } from '../../utils/formatos.js'
import { coresDoCarro, diariasEntre, formatarData, hojeIso, nomeDoCarro } from '../../utils/pedidos.js'

function validar({ automovel, retirada, devolucao, comCredito, bancoId, valorCredito }) {
  const e = {}
  if (!automovel) e.automovelId = 'Escolha um automóvel.'
  if (!retirada) e.dataRetirada = 'Informe a data de retirada.'
  else if (retirada < hojeIso()) e.dataRetirada = 'A retirada não pode estar no passado.'
  if (!devolucao) e.dataDevolucao = 'Informe a data de devolução.'
  else if (retirada && devolucao <= retirada) e.dataDevolucao = 'A devolução precisa ser depois da retirada.'
  if (comCredito) {
    if (!bancoId) e['credito.bancoId'] = 'Escolha o banco.'
    if (!(lerMoeda(valorCredito) > 0)) e['credito.valor'] = 'Informe o valor do crédito.'
  }
  return e
}

/** UC04 Introduzir pedido (com UC08 Solicitar crédito bancário opcional). */
export default function NovoPedido() {
  const { token } = useAuth()
  const navegar = useNavigate()
  const sessaoExpirada = useSessaoExpirada()
  const [automoveis, setAutomoveis] = useState(null)
  const [bancos, setBancos] = useState([])
  const [erroCarga, setErroCarga] = useState('')
  const [marca, setMarca] = useState('Todas')
  const [busca, setBusca] = useState('')
  const [automovelId, setAutomovelId] = useState(null)
  const [retirada, setRetirada] = useState(hojeIso(1))
  const [devolucao, setDevolucao] = useState(hojeIso(8))
  const [comCredito, setComCredito] = useState(false)
  const [bancoId, setBancoId] = useState('')
  const [valorCredito, setValorCredito] = useState('')
  const [erros, setErros] = useState({})
  const [aviso, setAviso] = useState(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    Promise.all([automovelService.listarAutomoveis(token), pedidoService.listarBancos(token)])
      .then(([listaAutomoveis, listaBancos]) => {
        setAutomoveis(listaAutomoveis)
        setBancos(listaBancos)
      })
      .catch((erro) => (erro.status === 401 ? sessaoExpirada() : setErroCarga(erro.message)))
  }, [token, sessaoExpirada])

  const automovel = automoveis?.find((a) => a.id === automovelId) ?? null
  const diarias = diariasEntre(retirada, devolucao)
  const valorEstimado = automovel ? automovel.valorDiaria * diarias : 0
  const banco = bancos.find((b) => String(b.id) === String(bancoId))

  const marcas = useMemo(() => ['Todas', ...new Set((automoveis ?? []).map((a) => a.marca))], [automoveis])
  const visiveis = (automoveis ?? []).filter(
    (a) => (marca === 'Todas' || a.marca === marca) && `${a.marca} ${a.modelo}`.toLowerCase().includes(busca.trim().toLowerCase()),
  )

  function ligarCredito() {
    const ligado = !comCredito
    setComCredito(ligado)
    if (ligado && !valorCredito && valorEstimado > 0) setValorCredito(moedaParaCampo(valorEstimado))
    if (ligado && !bancoId && bancos.length === 1) setBancoId(String(bancos[0].id))
  }

  async function enviar() {
    const encontrados = validar({ automovel, retirada, devolucao, comCredito, bancoId, valorCredito })
    setErros(encontrados)
    if (Object.keys(encontrados).length) {
      setAviso({ tipo: 'erro', titulo: 'Confira o pedido', texto: Object.values(encontrados)[0] })
      return
    }
    setEnviando(true)
    try {
      const pedido = await pedidoService.criarPedido(token, {
        automovelId,
        dataRetirada: retirada,
        dataDevolucao: devolucao,
        credito: comCredito ? { bancoId: Number(bancoId), valor: lerMoeda(valorCredito) } : null,
      })
      navegar(`/cliente/pedidos/${pedido.id}`, { state: { aviso: `Pedido ${pedido.numero} enviado` } })
    } catch (erro) {
      if (erro.status === 401) return sessaoExpirada()
      setErros(erro.campos)
      setAviso({ tipo: 'erro', titulo: 'Não foi possível enviar', texto: erro.message })
    } finally {
      setEnviando(false)
    }
  }

  const [cor, fundo] = automovel ? coresDoCarro(automovel) : ['#C9C6BD', '#F2F0EA']

  return (
    <LayoutApp>
      <div className="pagina-topo">
        <nav className="trilha" aria-label="Você está em">
          <Link to="/cliente/pedidos">Meus pedidos</Link>
          <Icone nome="chevright" tamanho={14} cor="#9A9DA6" />
          <span>Novo pedido</span>
        </nav>
        <h1>Novo pedido</h1>
        <p className="sub">Monte o pedido em 3 passos. Depois de enviado, ele passa pela análise financeira de um agente.</p>
      </div>

      {erroCarga && (
        <div className="alerta-erro" role="alert">
          <Icone nome="alert" tamanho={18} cor="#B42318" />
          <div className="alerta-titulo">{erroCarga}</div>
        </div>
      )}
      {!automoveis && !erroCarga && (
        <div className="carregando-pagina">
          <span className="spinner escuro" aria-hidden="true" /> Carregando automóveis...
        </div>
      )}

      {automoveis && (
        <div className="pedido-colunas">
          <div className="pedido-passos">
            <section className="card passo-card" aria-labelledby="passo1">
              <div className="passo-topo">
                <span className="passo-bola">1</span>
                <div>
                  <h2 className="card-titulo" id="passo1">Escolha o automóvel</h2>
                  <p className="passo-sub">{automoveis.length} automóveis na frota</p>
                </div>
                <div className="busca">
                  <Icone nome="search" tamanho={16} cor="#6B7080" />
                  <input aria-label="Buscar modelo" placeholder="Buscar modelo" value={busca} onChange={(e) => setBusca(e.target.value)} />
                </div>
              </div>
              <div className="chips" role="tablist" aria-label="Filtrar por marca">
                {marcas.map((m) => (
                  <button key={m} type="button" role="tab" aria-selected={marca === m} className={`chip ${marca === m ? 'ativo' : ''}`} onClick={() => setMarca(m)}>
                    {m}
                  </button>
                ))}
              </div>
              {erros.automovelId && <div className="ajuda erro">{erros.automovelId}</div>}
              <div className="grade-carros" role="radiogroup" aria-label="Automóveis">
                {visiveis.map((a) => (
                  <CarCard
                    key={a.id}
                    automovel={a}
                    selecionado={a.id === automovelId}
                    aoEscolher={() => {
                      setAutomovelId(a.id)
                      setErros((er) => ({ ...er, automovelId: undefined }))
                    }}
                  />
                ))}
                {visiveis.length === 0 && <p className="suave">Nenhum automóvel com esse filtro.</p>}
              </div>
            </section>

            <section className="card passo-card" aria-labelledby="passo2">
              <div className="passo-topo">
                <span className="passo-bola">2</span>
                <div>
                  <h2 className="card-titulo" id="passo2">Período</h2>
                  <p className="passo-sub">Datas de retirada e devolução do automóvel</p>
                </div>
              </div>
              <div className="linha3">
                <Campo rotulo="Retirada" tipo="date" mono min={hojeIso()} value={retirada} onChange={(e) => setRetirada(e.target.value)} erro={erros.dataRetirada} />
                <Campo rotulo="Devolução" tipo="date" mono min={retirada || hojeIso()} value={devolucao} onChange={(e) => setDevolucao(e.target.value)} erro={erros.dataDevolucao} />
                <div className="diarias">
                  <span className="mono diarias-n">{diarias}</span>
                  <span>{diarias === 1 ? 'diária' : 'diárias'}</span>
                </div>
              </div>
            </section>

            <section className="card passo-card" aria-labelledby="passo3">
              <div className="passo-topo">
                <span className="passo-bola">3</span>
                <div>
                  <h2 className="card-titulo" id="passo3">Crédito bancário</h2>
                  <p className="passo-sub">Opcional. Um banco agente analisa e pode conceder o crédito.</p>
                </div>
              </div>
              <label className={`linha-toggle ${bancos.length === 0 ? 'desligada' : ''}`}>
                <button
                  type="button"
                  role="switch"
                  aria-checked={comCredito}
                  aria-label="Solicitar crédito bancário"
                  className={`toggle ${comCredito ? 'ligado' : ''}`}
                  onClick={ligarCredito}
                  disabled={bancos.length === 0}
                >
                  <span className="toggle-bola" />
                </button>
                <span>
                  <span className="toggle-titulo">Solicitar crédito bancário</span>
                  <span className="toggle-sub">
                    {bancos.length === 0 ? 'Nenhum banco cadastrado ainda.' : 'O contrato de crédito fica associado ao aluguel.'}
                  </span>
                </span>
              </label>
              {comCredito && (
                <div className="linha2">
                  <Campo
                    rotulo="Banco"
                    icone="landmark"
                    value={bancoId}
                    onChange={(e) => setBancoId(e.target.value)}
                    erro={erros['credito.bancoId']}
                    opcoes={[{ valor: '', rotulo: 'Escolha o banco' }, ...bancos.map((b) => ({ valor: String(b.id), rotulo: b.nome }))]}
                  />
                  <Campo rotulo="Valor do crédito (R$)" mono inputMode="decimal" value={valorCredito} onChange={(e) => setValorCredito(mascaraMoeda(e.target.value))} erro={erros['credito.valor']} />
                </div>
              )}
            </section>
          </div>

          <aside className="card resumo-pedido" aria-label="Resumo do pedido">
            <h2 className="card-cabeca">Resumo do pedido</h2>
            <div className="resumo-carro" style={{ background: fundo }}>
              <CarroSvg cor={cor} largura={220} />
            </div>
            <div className="resumo-nome">
              <div>
                <div className="resumo-modelo">{automovel ? nomeDoCarro(automovel) : 'Nenhum automóvel'}</div>
                <div className="passo-sub">{automovel ? `${automovel.ano} · ${formatarMoeda(automovel.valorDiaria)}/dia` : 'Escolha um carro ao lado'}</div>
              </div>
              {automovel && <Placa numero={automovel.placa} />}
            </div>
            <dl className="linhas-info">
              <div><dt>Retirada</dt><dd className="mono">{formatarData(retirada) || '—'}</dd></div>
              <div><dt>Devolução</dt><dd className="mono">{formatarData(devolucao) || '—'}</dd></div>
              <div><dt>Diárias</dt><dd className="mono">{automovel ? `${diarias} × ${formatarMoeda(automovel.valorDiaria)}` : diarias}</dd></div>
              <div><dt>Crédito</dt><dd>{comCredito ? banco?.nome ?? 'Escolha o banco' : 'Não'}</dd></div>
            </dl>
            <div className="total-pedido">
              <span>Valor estimado</span>
              <span className="mono">{formatarMoeda(valorEstimado)}</span>
            </div>
            <p className="nota">
              <Icone nome="info" tamanho={16} cor="#3F4450" />
              Depois de enviado, o pedido passa pela análise financeira de um agente.
            </p>
            <div className="resumo-acoes">
              <Botao bloco icone="arrowright" onClick={enviar} carregando={enviando}>
                Enviar pedido
              </Botao>
              <Botao bloco variante="ghost" onClick={() => navegar('/cliente/pedidos')}>
                Cancelar
              </Botao>
            </div>
          </aside>
        </div>
      )}

      {aviso && <Toast {...aviso} aoFechar={() => setAviso(null)} />}
    </LayoutApp>
  )
}
