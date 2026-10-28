import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import { useAuth } from '../../context/AuthContext.jsx'

const RESUMO = [
  { rotulo: 'Em análise', icone: 'clock', cor: '#92400E', fundo: '#FEF3C7' },
  { rotulo: 'Aprovados', icone: 'checkcircle', cor: '#166534', fundo: '#DCFCE7' },
  { rotulo: 'Contratados', icone: 'file', cor: '#1E40AF', fundo: '#DBEAFE' },
  { rotulo: 'Cancelados', icone: 'alert', cor: '#6B7280', fundo: '#F3F4F6' },
]

/** Página inicial do cliente. Os pedidos entram na etapa do protótipo. */
export default function InicioCliente() {
  const { usuario } = useAuth()
  const primeiroNome = usuario.nome.split(' ')[0]

  return (
    <LayoutApp>
      <div className="pagina-topo">
        <div>
          <h1>Olá, {primeiroNome}</h1>
          <p className="sub">Acompanhe seus pedidos de aluguel e o andamento de cada análise.</p>
        </div>
      </div>

      <div className="resumo">
        {RESUMO.map((r) => (
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
          <Icone nome="inbox" tamanho={28} cor="#3F4450" />
        </div>
        <div className="vazio-titulo">Você ainda não fez nenhum pedido</div>
        <p className="vazio-texto">Quando você enviar um pedido de aluguel, ele aparece aqui com o status da análise.</p>
      </div>
    </LayoutApp>
  )
}
