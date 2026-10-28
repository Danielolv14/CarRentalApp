import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import CabecalhoPublico from '../../components/CabecalhoPublico.jsx'
import Campo from '../../components/Campo.jsx'
import Icone from '../../components/Icone.jsx'
import Segmentado from '../../components/Segmentado.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import { cnpjValido, mascaraCnpj, somenteDigitos } from '../../utils/formatos.js'

const TIPOS = [
  { valor: 'EMPRESA', rotulo: 'Empresa', icone: 'building' },
  { valor: 'BANCO', rotulo: 'Banco', icone: 'landmark' },
]

function validar(d) {
  const e = {}
  if (!d.razaoSocial.trim()) e.razaoSocial = 'Informe a razão social.'
  if (!d.cnpj) e.cnpj = 'Informe o CNPJ.'
  else if (!cnpjValido(d.cnpj)) e.cnpj = 'CNPJ inválido. Confira os 14 dígitos.'
  if (d.login.trim().length < 3) e.login = 'O usuário precisa ter pelo menos 3 caracteres.'
  if (d.senha.length < 6) e.senha = 'A senha precisa ter pelo menos 6 caracteres.'
  if (d.confirmarSenha !== d.senha) e.confirmarSenha = 'As senhas não conferem.'
  return e
}

export default function CadastroAgente() {
  const { cadastrarAgente } = useAuth()
  const navegar = useNavigate()
  const [dados, setDados] = useState({ tipo: 'EMPRESA', razaoSocial: '', cnpj: '', login: '', senha: '', confirmarSenha: '' })
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState('')
  const [enviando, setEnviando] = useState(false)

  const mudar = (campo, mascara) => (e) => {
    const valor = mascara ? mascara(e.target.value) : e.target.value
    setDados((d) => ({ ...d, [campo]: valor }))
    setErros((er) => ({ ...er, [campo]: undefined }))
  }

  async function enviar(e) {
    e.preventDefault()
    const encontrados = validar(dados)
    setErros(encontrados)
    setErroGeral('')
    if (Object.keys(encontrados).length) return

    setEnviando(true)
    try {
      await cadastrarAgente({
        tipo: dados.tipo,
        razaoSocial: dados.razaoSocial.trim(),
        cnpj: somenteDigitos(dados.cnpj),
        login: dados.login.trim(),
        senha: dados.senha,
      })
      navegar('/agente', { replace: true })
    } catch (erro) {
      const campos = { ...erro.campos }
      if (erro.status === 409) {
        if (erro.message.includes('CNPJ')) campos.cnpj = erro.message
        else if (erro.message.includes('usuário')) campos.login = erro.message
      }
      setErros(campos)
      setErroGeral(erro.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="pub">
      <CabecalhoPublico />
      <main className="cadastro">
        <div className="cadastro-topo">
          <div className="sobretitulo azul">CADASTRO DE AGENTE</div>
          <h1 className="titulo-grande esquerda">Cadastre sua empresa ou banco</h1>
        </div>
        <div className="cadastro-corpo">
          <form className="card form-card estreito" onSubmit={enviar} noValidate>
            <div className="form-card-topo">
              <h2 className="card-h">Cadastro de agente</h2>
              <p className="sub">Para locadoras e bancos que avaliam pedidos de aluguel.</p>
            </div>
            <div className="form-grade">
              {erroGeral && (
                <div className="alerta-erro" role="alert">
                  <Icone nome="alert" tamanho={18} cor="#B42318" />
                  <div className="alerta-titulo">{erroGeral}</div>
                </div>
              )}
              <div className="campo">
                <span className="rotulo">Tipo de agente</span>
                <Segmentado rotulo="Tipo de agente" opcoes={TIPOS} valor={dados.tipo} aoMudar={(tipo) => setDados((d) => ({ ...d, tipo }))} />
              </div>
              <Campo rotulo="Razão social" value={dados.razaoSocial} onChange={mudar('razaoSocial')} erro={erros.razaoSocial} autoComplete="organization" autoFocus />
              <Campo rotulo="CNPJ" mono inputMode="numeric" placeholder="00.000.000/0000-00" value={dados.cnpj} onChange={mudar('cnpj', mascaraCnpj)} erro={erros.cnpj} />
              <div className="separador" />
              <div className="secao-rotulo">
                <span>Acesso</span>
              </div>
              <Campo rotulo="Usuário" icone="user" value={dados.login} onChange={mudar('login')} erro={erros.login} autoComplete="username" />
              <div className="linha2">
                <Campo rotulo="Senha" icone="lock" tipo="senha" value={dados.senha} onChange={mudar('senha')} erro={erros.senha} autoComplete="new-password" dica="Mínimo de 6 caracteres." />
                <Campo rotulo="Confirmar senha" icone="lock" tipo="senha" value={dados.confirmarSenha} onChange={mudar('confirmarSenha')} erro={erros.confirmarSenha} autoComplete="new-password" />
              </div>
            </div>
            <div className="form-rodape">
              <Botao variante="secondary" icone="chevleft" onClick={() => navegar('/cadastro')}>
                Voltar
              </Botao>
              <Botao type="submit" carregando={enviando}>
                Criar conta
              </Botao>
            </div>
          </form>
        </div>
      </main>
    </div>
  )
}
