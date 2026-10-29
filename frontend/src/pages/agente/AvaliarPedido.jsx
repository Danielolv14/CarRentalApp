import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import Campo from '../../components/Campo.jsx'
import CarroSvg from '../../components/CarroSvg.jsx'
import DonoPill from '../../components/DonoPill.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import ModalModificarPedido from '../../components/ModalModificarPedido.jsx'
import Placa from '../../components/Placa.jsx'
import StatusBadge from '../../components/StatusBadge.jsx'
import Toast from '../../components/Toast.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as agenteService from '../../services/agenteService.js'
import * as automovelService from '../../services/automovelService.js'
import { formatarMoeda, iniciais, lerMoeda, mascaraCpf, mascaraMoeda, moedaParaCampo } from '../../utils/formatos.js'
import {
  calcularParcela, coresDoCarro, formatarData, formatarDataHora, formatarPeriodo, formatarTaxa, nomeDoCarro, PENDENTES,
} from '../../utils/pedidos.js'

const OPCOES_PARCELAS = [1, 2, 3, 6, 10, 12, 18, 24, 36, 48, 60].map((n) => ({ valor: String(n), rotulo: `${n}x` }))
// Referência usada na análise: o aluguel não deveria passar de 30% da renda mensal
const LIMITE_COMPROMETIMENTO = 30

/** UC09 Avaliar pedido (com UC10 Conceder crédito, para bancos) e UC06 Modificar pedido, pelo agente. */
export default function AvaliarPedido() {
  const { id } = useParams()
  const { usuario, token } = useAuth()
  const navegar = useNavigate()
  const sessaoExpirada = useSessaoExpirada()
  const [item, setItem] = useState(null)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState(null)
  const [resultado, setResultado] = useState('POSITIVO')
  const [justificativa, setJustificativa] = useState('')
  const [valorCredito, setValorCredito] = useState('')
  const [parcelas, setParcelas] = useState('12')
  const [taxa, setTaxa] = useState('1,90')
  const [erros, setErros] = useState({})
  const [salvando, setSalvando] = useState(false)
  const [modificando, setModificando] = useState(false)
  const [automoveis, setAutomoveis] = useState([])
  const [salvandoAlteracao, setSalvandoAlteracao] = useState(false)
  const [erroAlteracao, setErroAlteracao] = useState('')

  // Ao abrir um pedido Enviado que cabe a este agente, ele passa para Em análise (US11).
  useEffect(() => {
    let ativo = true
    agenteService
      .detalharPedido(token, id)
      .then((dados) => (dados.podeAvaliar && dados.pedido.status === 'ENVIADO' ? agenteService.iniciarAnalise(token, id) : dados))
      .then((dados) => {
        if (!ativo) return
        setItem(dados)
        if (dados.pedido.credito) setValorCredito(moedaParaCampo(dados.pedido.credito.valor))
      })
      .catch((e) => (e.status === 401 ? sessaoExpirada() : setErro(e.message)))
    return () => {
      ativo = false
    }
  }, [token, id, sessaoExpirada])

  if (erro || !item) {
    return (
      <LayoutApp>
        {erro ? (
          <div className="card vazio">
            <div className="vazio-icone">
              <Icone nome="alert" tamanho={28} cor="#3F4450" />
            </div>
            <div className="vazio-titulo">{erro}</div>
            <Botao variante="secondary" icone="chevleft" onClick={() => navegar('/agente/pedidos')}>
              Voltar para os pedidos
            </Botao>
          </div>
        ) : (
          <div className="carregando-pagina">
            <span className="spinner escuro" aria-hidden="true" /> Carregando pedido...
          </div>
        )}
      </LayoutApp>
    )
  }

  const { pedido: p, cliente } = item
  const comCredito = usuario.tipoAgente === 'BANCO' && p.credito && resultado === 'POSITIVO'
  const valorParcela = calcularParcela(lerMoeda(valorCredito) ?? 0, lerMoeda(taxa) ?? 0, Number(parcelas))

  function validar() {
    const e = {}
    if (!justificativa.trim()) e.justificativa = 'Escreva a justificativa.'
    if (comCredito) {
      if (!(lerMoeda(valorCredito) > 0)) e['credito.valor'] = 'Informe o valor do crédito.'
      const t = lerMoeda(taxa)
      if (t === null || t < 0 || t > 20) e['credito.taxaMensal'] = 'Informe uma taxa entre 0 e 20% ao mês.'
    }
    return e
  }

  async function salvarParecer() {
    const e = validar()
    setErros(e)
    if (Object.keys(e).length) return
    setSalvando(true)
    try {
      const dados = { resultado, justificativa: justificativa.trim() }
      if (comCredito) dados.credito = { valor: lerMoeda(valorCredito), parcelas: Number(parcelas), taxaMensal: lerMoeda(taxa) }
      const atualizado = await agenteService.darParecer(token, id, dados)
      setItem(atualizado)
      setAviso({
        tipo: 'ok',
        titulo: 'Parecer registrado',
        texto: `O pedido ${p.numero} foi ${resultado === 'POSITIVO' ? 'aprovado' : 'reprovado'}.`,
      })
    } catch (falha) {
      if (falha.status === 401) return sessaoExpirada()
      if (Object.keys(falha.campos).length) setErros(falha.campos)
      setAviso({ tipo: 'erro', titulo: 'Não foi possível salvar o parecer', texto: falha.message })
    } finally {
      setSalvando(false)
    }
  }

  async function abrirModificacao() {
    setErroAlteracao('')
    setModificando(true)
    try {
      setAutomoveis(await automovelService.listarAutomoveis(token))
    } catch (falha) {
      if (falha.status === 401) sessaoExpirada()
    }
  }

  async function salvarModificacao(dados) {
    setSalvandoAlteracao(true)
    setErroAlteracao('')
    try {
      setItem(await agenteService.modificarPedido(token, id, dados))
      setModificando(false)
      setAviso({ tipo: 'ok', titulo: 'Pedido modificado', texto: 'O valor estimado foi recalculado.' })
    } catch (falha) {
      if (falha.status === 401) return sessaoExpirada()
      setErroAlteracao(falha.message)
    } finally {
      setSalvandoAlteracao(false)
    }
  }

  return (
    <LayoutApp>
      <div className="pagina-topo com-acao">
        <div>
          <nav className="trilha" aria-label="Você está em">
            <Link to="/agente/pedidos">Pedidos para avaliar</Link>
            <Icone nome="chevright" tamanho={14} cor="#9A9DA6" />
            <span>{p.numero}</span>
          </nav>
          <div className="titulo-com-selo">
            <h1>
              {item.podeAvaliar ? 'Avaliar pedido' : 'Pedido'} {p.numero}
            </h1>
            <StatusBadge status={p.status} />
          </div>
          <p className="sub">
            Enviado por {cliente.nome} em {formatarDataHora(p.dataCriacao)}.
          </p>
        </div>
        {item.podeExecutarContrato && (
          <Botao icone="file" onClick={() => navegar(`/agente/pedidos/${p.id}/contrato`)}>
            Executar contrato
          </Botao>
        )}
      </div>

      <div className="detalhe-colunas">
        <div className="detalhe-principal">
          <CartaoPedido pedido={p} />
          <CartaoCliente cliente={cliente} valorEstimado={p.valorEstimado} />
        </div>

        <div className="detalhe-lateral">
          {item.podeAvaliar ? (
            <section className="card decisao">
              <div className="decisao-topo">
                <h2 className="card-titulo">Parecer financeiro</h2>
                <div className="passo-sub">Obrigatório para seguir com o pedido.</div>
              </div>
              <div className="decisao-corpo">
                <div className="veredito" role="radiogroup" aria-label="Resultado do parecer">
                  <button type="button" role="radio" aria-checked={resultado === 'POSITIVO'} className={`positivo ${resultado === 'POSITIVO' ? 'ativo' : ''}`} onClick={() => setResultado('POSITIVO')}>
                    <Icone nome="thumbup" tamanho={18} /> Positivo
                  </button>
                  <button type="button" role="radio" aria-checked={resultado === 'NEGATIVO'} className={`negativo ${resultado === 'NEGATIVO' ? 'ativo' : ''}`} onClick={() => setResultado('NEGATIVO')}>
                    <Icone nome="thumbdown" tamanho={18} /> Negativo
                  </button>
                </div>

                <div className="campo">
                  <label className="rotulo" htmlFor="justificativa">
                    Justificativa
                  </label>
                  <div className={`input area ${erros.justificativa ? 'erro' : ''}`}>
                    <textarea
                      id="justificativa"
                      maxLength={1000}
                      placeholder={resultado === 'POSITIVO' ? 'Ex.: renda compatível com o valor do aluguel.' : 'Ex.: renda insuficiente para o período pedido.'}
                      value={justificativa}
                      onChange={(e) => setJustificativa(e.target.value)}
                      aria-invalid={!!erros.justificativa}
                    />
                  </div>
                  {erros.justificativa && (
                    <div className="ajuda erro" role="alert">
                      <Icone nome="alert" tamanho={14} cor="#B42318" />
                      <span>{erros.justificativa}</span>
                    </div>
                  )}
                </div>

                {comCredito && (
                  <>
                    <div className="separador" />
                    <div className="credito-topo">
                      <h3 className="card-titulo">Contrato de crédito</h3>
                      <span className="chip-inst">
                        <Icone nome="landmark" tamanho={14} cor="#1E40AF" /> Você é banco · o cliente pediu crédito
                      </span>
                    </div>
                    <Campo rotulo="Valor do crédito (R$)" mono inputMode="decimal" value={valorCredito} onChange={(e) => setValorCredito(mascaraMoeda(e.target.value))} erro={erros['credito.valor']} />
                    <div className="linha2">
                      <Campo rotulo="Parcelas" opcoes={OPCOES_PARCELAS} value={parcelas} onChange={(e) => setParcelas(e.target.value)} erro={erros['credito.parcelas']} />
                      <Campo rotulo="Taxa ao mês (%)" mono inputMode="decimal" value={taxa} onChange={(e) => setTaxa(e.target.value.replace(/[^\d,]/g, ''))} erro={erros['credito.taxaMensal']} />
                    </div>
                    <div className="calculo">
                      <span>{parcelas} parcelas de</span>
                      <span className="mono">{formatarMoeda(valorParcela)}</span>
                    </div>
                  </>
                )}
              </div>
              <div className="decisao-rodape">
                <Botao icone="check" bloco onClick={salvarParecer} carregando={salvando}>
                  Salvar parecer
                </Botao>
                <Botao variante="secondary" icone="pencil" bloco onClick={abrirModificacao} disabled={salvando}>
                  Modificar pedido
                </Botao>
              </div>
            </section>
          ) : (
            <Situacao item={item} />
          )}
        </div>
      </div>

      {modificando && (
        <ModalModificarPedido
          pedido={p}
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

function CartaoPedido({ pedido: p }) {
  const a = p.automovel
  const [cor, fundo] = coresDoCarro(a)
  return (
    <section className="card">
      <div className="card-cabeca com-link">
        <h2>Pedido e automóvel</h2>
        <StatusBadge status={p.status} />
      </div>
      <div className="auto-caixa">
        <div className="auto-img" style={{ background: fundo }}>
          <CarroSvg cor={cor} largura={220} />
        </div>
        <div className="auto-info">
          <div className="resumo-modelo">
            {nomeDoCarro(a)} {a.ano}
          </div>
          <Placa numero={a.placa} />
          <dl className="linhas-info">
            <div><dt>Matrícula (RENAVAM)</dt><dd className="mono">{a.matricula}</dd></div>
            <div><dt>Período</dt><dd className="mono">{formatarPeriodo(p.dataRetirada, p.dataDevolucao)}</dd></div>
            <div><dt>Diárias</dt><dd className="mono">{p.diarias} × {formatarMoeda(p.valorDiaria)}</dd></div>
            <div><dt>Valor estimado</dt><dd className="mono">{formatarMoeda(p.valorEstimado)}</dd></div>
            <div><dt>Crédito solicitado</dt><dd>{p.credito ? `${p.credito.banco} · ${formatarMoeda(p.credito.valor)}` : 'Não'}</dd></div>
          </dl>
        </div>
      </div>
    </section>
  )
}

/** Dados do cliente para a análise financeira: rendimentos, renda total e quanto o aluguel pesa nela. */
function CartaoCliente({ cliente: c, valorEstimado }) {
  const local = c.endereco ? `${c.endereco.cidade}/${c.endereco.uf}` : null
  const percentual = c.rendaTotal > 0 ? (valorEstimado / c.rendaTotal) * 100 : null
  return (
    <section className="card">
      <h2 className="card-cabeca">Cliente</h2>
      <div className="card-corpo">
        <div className="cliente-topo">
          <span className="avatar medio">{iniciais(c.nome)}</span>
          <div>
            <div className="resumo-modelo">{c.nome}</div>
            <div className="passo-sub">{[c.profissao, local].filter(Boolean).join(' · ') || 'Sem profissão informada'}</div>
          </div>
        </div>
        <dl className="linhas-info grade2">
          <div><dt>CPF</dt><dd className="mono">{mascaraCpf(c.cpf)}</dd></div>
          <div><dt>RG</dt><dd className="mono">{c.rg}</dd></div>
        </dl>
        <div className="separador" />
        <div className="secao-rotulo">
          <span>Rendimentos</span>
          <span className="contador">{c.rendimentos.length}</span>
        </div>
        {c.rendimentos.length === 0 ? (
          <p className="suave lista-rendimentos">O cliente não informou rendimentos.</p>
        ) : (
          <ul className="lista-rendimentos">
            {c.rendimentos.map((r) => (
              <li key={r.id}>
                <Icone nome="briefcase" tamanho={16} cor="#6B7080" />
                <span className="empregador">{r.entidadeEmpregadora}</span>
                <span className="mono">{formatarMoeda(r.valor)}</span>
              </li>
            ))}
          </ul>
        )}
        <div className="total">
          <span>Renda mensal total</span>
          <span className="mono total-valor">{formatarMoeda(c.rendaTotal)}</span>
        </div>
        <div className="comprometimento">
          <div className="comprometimento-topo">
            <span>Valor do aluguel sobre a renda (referência: até {LIMITE_COMPROMETIMENTO}%)</span>
            <span className="mono forte">{percentual === null ? 'sem renda' : `${percentual.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`}</span>
          </div>
          <div className="barra" aria-hidden="true">
            <div className={percentual !== null && percentual <= LIMITE_COMPROMETIMENTO ? 'barra-ok' : 'barra-alta'} style={{ width: `${Math.min(percentual ?? 100, 100)}%` }} />
          </div>
        </div>
      </div>
    </section>
  )
}

/** Lateral quando o agente não dá o parecer: aguardando o banco, parecer já dado, crédito e contrato. */
function Situacao({ item }) {
  const { pedido: p } = item
  const parecer = p.pareceres.at(-1)
  const concedido = p.credito?.concedido

  return (
    <>
      <section className="card">
        <h2 className="card-cabeca">Parecer</h2>
        <div className="card-corpo">
          {parecer ? (
            <div className={`parecer ${parecer.resultado === 'POSITIVO' ? 'positivo' : 'negativo'}`}>
              <div className="parecer-titulo">
                Parecer {parecer.resultado === 'POSITIVO' ? 'positivo' : 'negativo'} · {parecer.agente}
              </div>
              <p className="parecer-texto">“{parecer.justificativa}” · {formatarData(parecer.data)}</p>
            </div>
          ) : p.status === 'CANCELADO' ? (
            <p className="suave">O cliente cancelou o pedido antes do parecer.</p>
          ) : PENDENTES.includes(p.status) && p.credito ? (
            <div className="aguardando">
              <Icone nome="clock" tamanho={20} cor="#92400E" />
              <div>
                <div className="aguardando-titulo">Aguardando o {p.credito.banco}</div>
                <p className="aguardando-texto">
                  O cliente pediu crédito a este banco, então o parecer é dele: o positivo já concede o crédito. Depois da aprovação, a locadora executa o contrato.
                </p>
              </div>
            </div>
          ) : (
            <p className="suave">Ainda sem parecer.</p>
          )}
        </div>
      </section>

      {p.credito && (
        <section className="card">
          <div className="card-cabeca com-link">
            <h2>Crédito bancário</h2>
            {concedido && <StatusBadge status="APROVADO" />}
          </div>
          <div className="card-corpo">
            <dl className="linhas-info">
              <div><dt>Banco</dt><dd>{p.credito.banco}</dd></div>
              <div><dt>Pedido pelo cliente</dt><dd className="mono">{formatarMoeda(p.credito.valor)}</dd></div>
              {concedido ? (
                <>
                  <div><dt>Concedido</dt><dd className="mono">{formatarMoeda(concedido.valor)}</dd></div>
                  <div><dt>Parcelas</dt><dd className="mono">{concedido.parcelas}x de {formatarMoeda(concedido.valorParcela)}</dd></div>
                  <div><dt>Taxa</dt><dd className="mono">{formatarTaxa(concedido.taxaMensal)} ao mês</dd></div>
                </>
              ) : (
                <div><dt>Concedido</dt><dd>Não</dd></div>
              )}
            </dl>
          </div>
        </section>
      )}

      {p.contrato && (
        <section className="card">
          <h2 className="card-cabeca">Contrato</h2>
          <div className="card-corpo">
            <dl className="linhas-info">
              <div><dt>Nº do contrato</dt><dd className="mono">{p.contrato.numero}</dd></div>
              <div><dt>Executado em</dt><dd className="mono">{formatarData(p.contrato.dataExecucao)}</dd></div>
              <div><dt>Proprietário</dt><dd><DonoPill tipo={p.contrato.tipoProprietario} /></dd></div>
            </dl>
          </div>
        </section>
      )}
    </>
  )
}
