import { NavLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { iniciais } from '../utils/formatos.js'
import Icone from './Icone.jsx'
import Marca from './Marca.jsx'

const MENU = {
  CLIENTE: [
    { para: '/cliente', icone: 'home', rotulo: 'Início' },
    { para: '/cliente/pedidos/novo', icone: 'plus', rotulo: 'Novo pedido' },
    // "Meus pedidos" fica marcado também no detalhe de um pedido (/cliente/pedidos/12)
    { para: '/cliente/pedidos', icone: 'clipboard', rotulo: 'Meus pedidos', ativo: (caminho) => /^\/cliente\/pedidos(\/\d+)?$/.test(caminho) },
    { para: '/cliente/perfil', icone: 'user', rotulo: 'Meu perfil' },
  ],
  AGENTE: [
    { para: '/agente', icone: 'dashboard', rotulo: 'Painel' },
    { icone: 'clipcheck', rotulo: 'Pedidos para avaliar' },
    { icone: 'file', rotulo: 'Contratos' },
    { icone: 'car', rotulo: 'Automóveis', so: 'EMPRESA' },
  ],
}

const hoje = new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }).format(new Date())

/** Estrutura da área logada: menu lateral por perfil + barra do topo. */
export default function LayoutApp({ children }) {
  const { usuario, sair } = useAuth()
  const navegar = useNavigate()
  const { pathname } = useLocation()
  const agente = usuario.perfil === 'AGENTE'
  const itens = MENU[usuario.perfil].filter((i) => !i.so || i.so === usuario.tipoAgente)

  function encerrar() {
    sair()
    navegar('/login', { replace: true })
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="sidebar-marca">
          <Marca escuro />
        </div>
        {agente && (
          <div className="inst">
            <div className="inst-icone">
              <Icone nome={usuario.tipoAgente === 'BANCO' ? 'landmark' : 'building'} tamanho={18} cor="#F5B700" />
            </div>
            <div>
              <div className="inst-nome">{usuario.nome}</div>
              <span className="inst-tag">{usuario.tipoAgente === 'BANCO' ? 'Banco' : 'Empresa'}</span>
            </div>
          </div>
        )}
        <nav className="nav" aria-label="Menu principal">
          <div className="nav-rotulo">{agente ? 'AGENTE' : 'MENU'}</div>
          {itens.map((item) =>
            item.para ? (
              <NavLink
                key={item.rotulo}
                to={item.para}
                end
                className={({ isActive }) => `nav-item ${(item.ativo ? item.ativo(pathname) : isActive) ? 'ativo' : ''}`}
              >
                <Icone nome={item.icone} tamanho={18} />
                <span>{item.rotulo}</span>
              </NavLink>
            ) : (
              <span key={item.rotulo} className="nav-item em-breve" aria-disabled="true" title="Disponível nas próximas etapas">
                <Icone nome={item.icone} tamanho={18} />
                <span>{item.rotulo}</span>
              </span>
            ),
          )}
        </nav>
        <div className="sidebar-pe">
          <div className="usuario-cartao">
            <div className="avatar">{iniciais(usuario.nome)}</div>
            <div className="usuario-texto">
              <div className="usuario-nome">{usuario.nome}</div>
              <div className="usuario-papel">{agente ? 'Agente' : 'Cliente'}</div>
            </div>
            <button type="button" className="sair" onClick={encerrar} aria-label="Sair" title="Sair">
              <Icone nome="logout" tamanho={18} />
            </button>
          </div>
        </div>
      </aside>
      <main className="principal">
        <div className="topbar">
          <span className="topbar-data">{hoje}</span>
        </div>
        <div className="conteudo">{children}</div>
      </main>
    </div>
  )
}
