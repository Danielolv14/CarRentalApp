import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import CarroSvg from '../../components/CarroSvg.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import Placa from '../../components/Placa.jsx'
import StatusBadge from '../../components/StatusBadge.jsx'
import Toast from '../../components/Toast.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as agenteService from '../../services/agenteService.js'
import { formatarMoeda } from '../../utils/formatos.js'
import { coresDoCarro, formatarData, formatarPeriodo, formatarTaxa, nomeDoCarro, PROPRIETARIOS } from '../../utils/pedidos.js'

/** Quem pode ser o proprietário e por quê não, quando não pode (as mesmas regras do Agente#executarContrato). */
function opcoesDeProprietario(pedido, cliente, usuario) {
  const empresa = usuario.tipoAgente === 'EMPRESA'
  return [
    { tipo: 'CLIENTE', nome: cliente.nome },
    { tipo: 'EMPRESA', nome: empresa ? usuario.nome : 'Só uma empresa registra em nome da empresa', bloqueado: !empresa },
    { tipo: 'BANCO', nome: pedido.credito ? pedido.credito.banco : 'O pedido não tem crédito bancário', bloqueado: !pedido.credito },
  ]
}

/** UC11 Executar contrato: o pedido aprovado vira contrato e o agente define o proprietário do automóvel. */
export default function ExecutarContrato() {
  const { id } = useParams()
  const { usuario, token } = useAuth()
  const navegar = useNavigate()
  const sessaoExpirada = useSessaoExpirada()
  const [item, setItem] = useState(null)
  const [erro, setErro] = useState('')
  const [dono, setDono] = useState(usuario.tipoAgente === 'EMPRESA' ? 'EMPRESA' : 'BANCO')
  const [executando, setExecutando] = useState(false)
  const [aviso, setAviso] = useState(null)

  useEffect(() => {
    agenteService
      .detalharPedido(token, id)
      .then(setItem)
      .catch((e) => (e.status === 401 ? sessaoExpirada() : setErro(e.message)))
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
  const a = p.automovel
  const [cor, fundo] = coresDoCarro(a)
  const parecer = p.pareceres.at(-1)
  const concedido = p.credito?.concedido
  const opcoes = opcoesDeProprietario(p, cliente, usuario)
  const escolhido = opcoes.find((o) => o.tipo === dono)

  async function executar() {
    setExecutando(true)
    try {
      const contrato = await agenteService.executarContrato(token, id, dono)
      navegar('/agente/contratos', {
        state: { aviso: { titulo: 'Contrato executado', texto: `${contrato.numero} criado. O pedido ${p.numero} agora está Contratado.` } },
      })
    } catch (falha) {
      if (falha.status === 401) return sessaoExpirada()
      setAviso({ tipo: 'erro', titulo: 'Não foi possível executar o contrato', texto: falha.message })
      setExecutando(false)
    }
  }

  return (
    <LayoutApp>
      <div className="pagina-topo">
        <nav className="trilha" aria-label="Você está em">
          <Link to="/agente/pedidos">Pedidos para avaliar</Link>
          <Icone nome="chevright" tamanho={14} cor="#9A9DA6" />
          <Link to={`/agente/pedidos/${p.id}`}>{p.numero}</Link>
          <Icone nome="chevright" tamanho={14} cor="#9A9DA6" />
          <span>Contrato</span>
        </nav>
        <h1>Executar contrato</h1>
        <p className="sub">
          {item.podeExecutarContrato
            ? 'Pedido aprovado. Confira os dados e defina o proprietário do automóvel.'
            : 'Só pedidos aprovados podem virar contrato.'}
        </p>
      </div>

      {!item.podeExecutarContrato && (
        <div className="alerta-info" role="status">
          <Icone nome="info" tamanho={18} cor="#1E40AF" />
          <div className="alerta-titulo azul">
            O pedido {p.numero} está {p.status === 'CONTRATADO' ? `contratado (${p.contrato?.numero ?? 'contrato já executado'})` : 'sem aprovação'}.{' '}
            <Link className="link" to={`/agente/pedidos/${p.id}`}>
              Ver o pedido
            </Link>
          </div>
        </div>
      )}

      <div className="detalhe-colunas">
        <div className="detalhe-principal">
          <section className="card">
            <div className="card-cabeca com-link">
              <h2>Resumo do pedido {p.numero}</h2>
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
                  <div><dt>Cliente</dt><dd>{cliente.nome}</dd></div>
                  <div><dt>Período</dt><dd className="mono">{formatarPeriodo(p.dataRetirada, p.dataDevolucao)}</dd></div>
                  <div><dt>Valor</dt><dd className="mono">{formatarMoeda(p.valorEstimado)}</dd></div>
                </dl>
              </div>
            </div>
            {parecer && (
              <div className="card-corpo borda-topo">
                <div className={`parecer ${parecer.resultado === 'POSITIVO' ? 'positivo' : 'negativo'}`}>
                  <div className="parecer-titulo">
                    Parecer {parecer.resultado === 'POSITIVO' ? 'positivo' : 'negativo'} · {parecer.agente}
                  </div>
                  <p className="parecer-texto">“{parecer.justificativa}” · {formatarData(parecer.data)}</p>
                </div>
              </div>
            )}
          </section>

          <section className="card">
            <div className="card-cabeca">
              <h2 className="card-titulo">Proprietário do automóvel</h2>
              <div className="passo-sub">Em nome de quem o automóvel fica registrado durante o contrato.</div>
            </div>
            <div className="card-corpo">
              <div className="donos" role="radiogroup" aria-label="Proprietário do automóvel">
                {opcoes.map((o) => {
                  const ativo = o.tipo === dono
                  return (
                    <button
                      key={o.tipo}
                      type="button"
                      role="radio"
                      aria-checked={ativo}
                      className={`dono-opcao ${ativo ? 'ativo' : ''}`}
                      disabled={o.bloqueado || !item.podeExecutarContrato}
                      onClick={() => setDono(o.tipo)}
                    >
                      <span className="dono-marca">{ativo && <Icone nome="check" tamanho={12} cor="#FFFFFF" traco={3} />}</span>
                      <span className="dono-icone">
                        <Icone nome={PROPRIETARIOS[o.tipo].icone} tamanho={20} cor={ativo ? '#1F4FD8' : '#3F4450'} />
                      </span>
                      <span className="dono-titulo">{PROPRIETARIOS[o.tipo].rotulo}</span>
                      <span className="dono-sub">{o.nome}</span>
                    </button>
                  )
                })}
              </div>
            </div>
          </section>
        </div>

        <div className="detalhe-lateral">
          <section className="card">
            <div className="card-cabeca com-link">
              <h2>Crédito associado</h2>
              {concedido && <StatusBadge status="APROVADO" />}
            </div>
            <div className="card-corpo">
              {concedido ? (
                <dl className="linhas-info">
                  <div><dt>Banco</dt><dd>{concedido.banco}</dd></div>
                  <div><dt>Valor</dt><dd className="mono">{formatarMoeda(concedido.valor)}</dd></div>
                  <div><dt>Parcelas</dt><dd className="mono">{concedido.parcelas}x de {formatarMoeda(concedido.valorParcela)}</dd></div>
                  <div><dt>Taxa</dt><dd className="mono">{formatarTaxa(concedido.taxaMensal)} ao mês</dd></div>
                </dl>
              ) : (
                <p className="suave">Sem crédito bancário neste pedido.</p>
              )}
            </div>
          </section>

          <section className="card card-corpo confirmar">
            <h2 className="card-titulo">Executar contrato</h2>
            <p className="sub">Ao executar, o pedido passa para Contratado e fica somente leitura para o cliente.</p>
            <dl className="linhas-info">
              <div><dt>Nº do contrato</dt><dd className="suave">Gerado ao executar</dd></div>
              <div><dt>Proprietário</dt><dd>{PROPRIETARIOS[dono].rotulo}{escolhido && !escolhido.bloqueado ? ` · ${escolhido.nome}` : ''}</dd></div>
              <div><dt>Crédito</dt><dd>{concedido ? 'Sim' : 'Não'}</dd></div>
            </dl>
            <Botao icone="file" bloco onClick={executar} carregando={executando} disabled={!item.podeExecutarContrato || escolhido?.bloqueado}>
              Executar contrato
            </Botao>
            <Botao variante="ghost" bloco onClick={() => navegar(`/agente/pedidos/${p.id}`)} disabled={executando}>
              Voltar
            </Botao>
          </section>
        </div>
      </div>

      {aviso && <Toast {...aviso} aoFechar={() => setAviso(null)} />}
    </LayoutApp>
  )
}
