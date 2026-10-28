import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import Campo from '../../components/Campo.jsx'
import CarroSvg from '../../components/CarroSvg.jsx'
import Icone from '../../components/Icone.jsx'
import Marca from '../../components/Marca.jsx'
import Placa from '../../components/Placa.jsx'
import { areaDoUsuario, useAuth } from '../../context/AuthContext.jsx'

const VANTAGENS = ['Análise financeira por agentes', 'Crédito com bancos parceiros', 'Status do pedido em tempo real']

export default function Login() {
  const { entrar } = useAuth()
  const navegar = useNavigate()
  const local = useLocation()
  const [form, setForm] = useState({ login: '', senha: '' })
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState('')
  const [enviando, setEnviando] = useState(false)

  const mudar = (campo) => (e) => {
    setForm((f) => ({ ...f, [campo]: e.target.value }))
    setErros((er) => ({ ...er, [campo]: undefined }))
  }

  async function enviar(e) {
    e.preventDefault()
    const faltando = {}
    if (!form.login.trim()) faltando.login = 'Informe o usuário.'
    if (!form.senha) faltando.senha = 'Informe a senha.'
    setErros(faltando)
    setErroGeral('')
    if (Object.keys(faltando).length) return

    setEnviando(true)
    try {
      const usuario = await entrar(form.login, form.senha)
      navegar(local.state?.de ?? areaDoUsuario(usuario), { replace: true })
    } catch (erro) {
      setErroGeral(erro.status === 401 ? 'Usuário ou senha inválidos' : erro.message)
      setErros(erro.campos ?? {})
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="login">
      <section className="login-esquerda" aria-hidden="true">
        <Marca escuro />
        <div className="login-texto">
          <div className="sobretitulo">ALUGUEL DE CARROS</div>
          <h1 className="hero-titulo">Seu próximo carro começa com um pedido.</h1>
          <p className="hero-texto">Peça, acompanhe a análise e feche o contrato com locadoras e bancos parceiros, tudo num só lugar.</p>
        </div>
        <div className="hero-arte">
          <div className="estrada">
            <div className="estrada-faixa" />
          </div>
          <div className="hero-carro">
            <CarroSvg cor="#F5F4F0" largura={330} />
          </div>
          <div className="cartao-flutuante">
            <div className="cf-topo">
              <span className="mono cf-id">PED-1045</span>
              <span className="badge aprovado">
                <span className="badge-ponto" />
                Aprovado
              </span>
            </div>
            <div className="cf-carro">Volkswagen Polo 2024</div>
            <div className="cf-sub">Parecer positivo · Locadora Serra Verde</div>
          </div>
          <div className="placa-flutuante">
            <Placa numero="RTA2B45" tamanho="lg" />
          </div>
        </div>
        <ul className="vantagens">
          {VANTAGENS.map((v) => (
            <li key={v}>
              <Icone nome="checkcircle" tamanho={18} cor="#F5B700" />
              <span>{v}</span>
            </li>
          ))}
        </ul>
      </section>

      <section className="login-direita">
        <form className="login-form" onSubmit={enviar} noValidate>
          <div className="login-marca-movel">
            <Marca />
          </div>
          <h2 className="form-titulo">Entrar</h2>
          <p className="sub">Acesse com o usuário e a senha cadastrados.</p>

          {erroGeral && (
            <div className="alerta-erro" role="alert">
              <Icone nome="alert" tamanho={18} cor="#B42318" />
              <div>
                <div className="alerta-titulo">{erroGeral}</div>
                {erroGeral === 'Usuário ou senha inválidos' && <div className="alerta-sub">Confira os dados e tente de novo.</div>}
              </div>
            </div>
          )}

          <Campo rotulo="Usuário" icone="user" autoComplete="username" value={form.login} onChange={mudar('login')} erro={erros.login} autoFocus />
          <Campo rotulo="Senha" icone="lock" tipo="senha" autoComplete="current-password" value={form.senha} onChange={mudar('senha')} erro={erros.senha} />
          <Botao type="submit" bloco carregando={enviando}>
            Entrar
          </Botao>
          <div className="divisor" />
          <p className="centro">
            <span className="suave">Ainda não tem conta?</span>{' '}
            <Link className="link" to="/cadastro">
              Criar conta
            </Link>
          </p>
        </form>
        <div className="login-rodape">Rota · Sistema de aluguel de carros</div>
      </section>
    </div>
  )
}
