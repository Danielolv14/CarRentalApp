import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import StatusBadge from '../../components/StatusBadge.jsx'
import TabelaPedidosAgente from '../../components/TabelaPedidosAgente.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as agenteService from '../../services/agenteService.js'
import { hojeIso, PENDENTES, tempoDesde } from '../../utils/pedidos.js'

const mesAtual = new Intl.DateTimeFormat('pt-BR', { month: 'long' }).format(new Date())
const prefixoDoMes = hojeIso().slice(0, 7)

/** Pareceres que este agente deu no mês atual. */
function pareceresDoMes(pedidos, agente) {
  return pedidos
    .flatMap((i) => i.pedido.pareceres)
    .filter((p) => p.agente === agente && p.data.startsWith(prefixoDoMes))
}

/** Painel do agente (empresa ou banco): indicadores e fila de análise, do pedido mais antigo para o mais novo. */
export default function PainelAgente() {
  const { usuario, token } = useAuth()
  const navegar = useNavigate()
  const sessaoExpirada = useSessaoExpirada()
  const [pedidos, setPedidos] = useState(null)
  const [contratos, setContratos] = useState([])
  const [erro, setErro] = useState('')

  useEffect(() => {
    Promise.all([agenteService.listarPedidos(token), agenteService.listarContratos(token)])
      .then(([listaPedidos, listaContratos]) => {
        setPedidos(listaPedidos)
        setContratos(listaContratos)
      })
      .catch((e) => (e.status === 401 ? sessaoExpirada() : setErro(e.message)))
  }, [token, sessaoExpirada])

  const lista = pedidos ?? []
  const fila = lista.filter((i) => PENDENTES.includes(i.pedido.status))
  const paraAvaliar = fila.filter((i) => i.podeAvaliar)
  const aprovados = lista.filter((i) => i.podeExecutarContrato)
  const pareceres = pareceresDoMes(lista, usuario.nome)
  const positivos = pareceres.filter((p) => p.resultado === 'POSITIVO').length
  const negativos = pareceres.length - positivos
  const ativos = contratos.filter((c) => c.dataDevolucao >= hojeIso()).length

  const indicadores = [
    {
      rotulo: 'Aguardando análise', numero: paraAvaliar.length, icone: 'clock', cor: '#92400E', fundo: '#FEF3C7',
      nota: paraAvaliar.length ? `Mais antigo: enviado ${tempoDesde(paraAvaliar[0].pedido.dataCriacao)}` : 'Fila em dia',
    },
    {
      rotulo: 'Aprovados no mês', numero: positivos, icone: 'checkcircle', cor: '#166534', fundo: '#DCFCE7',
      nota: `Seus pareceres positivos em ${mesAtual}`,
    },
    {
      rotulo: 'Reprovados no mês', numero: negativos, icone: 'xcircle', cor: '#991B1B', fundo: '#FEE2E2',
      nota: pareceres.length ? `${Math.round((negativos / pareceres.length) * 100)}% dos seus pareceres` : 'Nenhum parecer no mês',
    },
    {
      rotulo: 'Contratos ativos', numero: ativos, icone: 'file', cor: '#1E40AF', fundo: '#DBEAFE',
      nota: `${contratos.length} ${contratos.length === 1 ? 'contrato executado' : 'contratos executados'}`,
    },
  ]

  return (
    <LayoutApp>
      <div className="pagina-topo com-acao">
        <div>
          <h1>Painel</h1>
          <p className="sub">
            {usuario.nome} · {usuario.tipoAgente === 'BANCO' ? 'pedidos com crédito para o seu banco.' : 'pedidos aguardando o seu parecer.'}
          </p>
        </div>
        <Botao icone="clipcheck" onClick={() => navegar('/agente/pedidos')}>
          Ver pedidos para avaliar
        </Botao>
      </div>

      {erro && (
        <div className="alerta-erro" role="alert">
          <Icone nome="alert" tamanho={18} cor="#B42318" />
          <div className="alerta-titulo">{erro}</div>
        </div>
      )}

      <div className="resumo">
        {indicadores.map((r) => (
          <div className="card stat" key={r.rotulo}>
            <div className="stat-topo">
              <span className="stat-rotulo">{r.rotulo}</span>
              <span className="stat-icone" style={{ background: r.fundo }}>
                <Icone nome={r.icone} tamanho={18} cor={r.cor} />
              </span>
            </div>
            <div className="stat-numero">{pedidos ? r.numero : '–'}</div>
            <div className="stat-nota">{pedidos ? r.nota : ' '}</div>
          </div>
        ))}
      </div>

      {!pedidos && !erro && (
        <div className="carregando-pagina">
          <span className="spinner escuro" aria-hidden="true" /> Carregando a fila...
        </div>
      )}

      {pedidos && (
        <section className="card tabela">
          <div className="card-cabeca com-legenda">
            <div>
              <h2 className="card-titulo">Fila de análise</h2>
              <div className="passo-sub">Do pedido mais antigo para o mais novo</div>
            </div>
            <div className="legenda">
              <span className="suave">Seus pareceres em {mesAtual}</span>
              <span>
                <StatusBadge status="APROVADO" /> {positivos}
              </span>
              <span>
                <StatusBadge status="REPROVADO" /> {negativos}
              </span>
            </div>
          </div>
          {fila.length === 0 ? (
            <div className="vazio">
              <div className="vazio-icone">
                <Icone nome="clipcheck" tamanho={28} cor="#3F4450" />
              </div>
              <div className="vazio-titulo">Nenhum pedido aguardando análise</div>
              <p className="vazio-texto">Quando um cliente enviar um pedido, ele entra na sua fila, do mais antigo para o mais novo.</p>
            </div>
          ) : (
            <TabelaPedidosAgente itens={fila} />
          )}
        </section>
      )}

      {aprovados.length > 0 && (
        <section className="card tabela">
          <div className="card-cabeca com-legenda">
            <div>
              <h2 className="card-titulo">Aprovados aguardando contrato</h2>
              <div className="passo-sub">Pedidos com parecer positivo, prontos para executar o contrato</div>
            </div>
            <Link className="link" to="/agente/contratos">
              Ver contratos
            </Link>
          </div>
          <TabelaPedidosAgente itens={aprovados} />
        </section>
      )}
    </LayoutApp>
  )
}
