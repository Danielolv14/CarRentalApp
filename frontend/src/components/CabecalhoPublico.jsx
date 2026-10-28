import { Link } from 'react-router-dom'
import Marca from './Marca.jsx'

/** Topo das telas de cadastro: logo à esquerda e atalho para o login. */
export default function CabecalhoPublico() {
  return (
    <header className="pub-topo">
      <Link to="/login" aria-label="Rota, ir para o login">
        <Marca />
      </Link>
      <div className="pub-direita">
        <span className="suave">Já tem conta?</span>
        <Link className="link" to="/login">
          Entrar
        </Link>
      </div>
    </header>
  )
}
