import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import { useAuth } from '../../context/AuthContext.jsx'

const INDICADORES = [
  { rotulo: 'Aguardando análise', icone: 'clock', cor: '#92400E', fundo: '#FEF3C7' },
  { rotulo: 'Aprovados no mês', icone: 'checkcircle', cor: '#166534', fundo: '#DCFCE7' },
  { rotulo: 'Reprovados no mês', icone: 'alert', cor: '#991B1B', fundo: '#FEE2E2' },
  { rotulo: 'Contratos ativos', icone: 'file', cor: '#1E40AF', fundo: '#DBEAFE' },
]

/** Painel do agente (empresa ou banco). A fila de pedidos entra na etapa do protótipo. */
export default function PainelAgente() {
  const { usuario } = useAuth()

  return (
    <LayoutApp>
      <div className="pagina-topo">
        <div>
          <h1>Painel</h1>
          <p className="sub">{usuario.nome} · pedidos aguardando o seu parecer.</p>
        </div>
      </div>

      <div className="resumo">
        {INDICADORES.map((r) => (
          <div className="card stat" key={r.rotulo}>
            <div className="stat-topo">
              <span className="stat-rotulo">{r.rotulo}</span>
              <span className="stat-icone" style={{ background: r.fundo }}>
                <Icone nome={r.icone} tamanho={18} cor={r.cor} />
              </span>
            </div>
            <div className="stat-numero">0</div>
          </div>
        ))}
      </div>

      <div className="card vazio">
        <div className="vazio-icone">
          <Icone nome="clipcheck" tamanho={28} cor="#3F4450" />
        </div>
        <div className="vazio-titulo">Nenhum pedido aguardando análise</div>
        <p className="vazio-texto">Quando um cliente enviar um pedido, ele entra na sua fila, do mais antigo para o mais novo.</p>
      </div>
    </LayoutApp>
  )
}
