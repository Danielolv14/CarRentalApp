import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import CabecalhoPublico from '../../components/CabecalhoPublico.jsx'
import Campo from '../../components/Campo.jsx'
import EditorRendimentos from '../../components/EditorRendimentos.jsx'
import Icone from '../../components/Icone.jsx'
import Stepper from '../../components/Stepper.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import { cpfValido, mascaraCep, mascaraCpf, somenteDigitos, UFS } from '../../utils/formatos.js'
import { enderecoParaApi, rendimentosParaApi, validarEndereco, validarRendimentos } from '../../utils/validacoes.js'

const PASSOS = ['Dados pessoais', 'Endereço', 'Profissão e renda', 'Acesso']

const VAZIO = {
  nome: '', cpf: '', rg: '',
  cep: '', logradouro: '', numero: '', bairro: '', cidade: '', uf: 'MG',
  profissao: '', rendimentos: [],
  login: '', senha: '', confirmarSenha: '',
}

/** Em qual passo fica cada campo (para levar o usuário ao erro que voltou da API). */
const PASSO_DO_CAMPO = {
  nome: 0, cpf: 0, rg: 0,
  'endereco.cep': 1, 'endereco.logradouro': 1, 'endereco.numero': 1, 'endereco.bairro': 1, 'endereco.cidade': 1, 'endereco.uf': 1,
  profissao: 2, rendimentos: 2,
  login: 3, senha: 3,
}

function validarPasso(passo, d) {
  const e = {}
  if (passo === 0) {
    if (!d.nome.trim()) e.nome = 'Informe o nome.'
    if (!d.cpf) e.cpf = 'Informe o CPF.'
    else if (!cpfValido(d.cpf)) e.cpf = 'CPF inválido. Confira os 11 dígitos.'
    if (!d.rg.trim()) e.rg = 'Informe o RG.'
  }
  if (passo === 1) Object.assign(e, validarEndereco(d))
  if (passo === 2) Object.assign(e, validarRendimentos(d.rendimentos))
  if (passo === 3) {
    if (d.login.trim().length < 3) e.login = 'O usuário precisa ter pelo menos 3 caracteres.'
    if (d.senha.length < 6) e.senha = 'A senha precisa ter pelo menos 6 caracteres.'
    if (d.confirmarSenha !== d.senha) e.confirmarSenha = 'As senhas não conferem.'
  }
  return e
}

function paraApi(d) {
  return {
    nome: d.nome.trim(),
    cpf: somenteDigitos(d.cpf),
    rg: d.rg.trim(),
    profissao: d.profissao.trim() || null,
    endereco: enderecoParaApi(d),
    rendimentos: rendimentosParaApi(d.rendimentos),
    login: d.login.trim(),
    senha: d.senha,
  }
}

export default function CadastroCliente() {
  const { cadastrarCliente } = useAuth()
  const navegar = useNavigate()
  const [passo, setPasso] = useState(0)
  const [dados, setDados] = useState(VAZIO)
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState('')
  const [enviando, setEnviando] = useState(false)

  const mudar = (campo, mascara) => (e) => {
    const valor = mascara ? mascara(e.target.value) : e.target.value
    setDados((d) => ({ ...d, [campo]: valor }))
    setErros((er) => ({ ...er, [campo]: undefined, [`endereco.${campo}`]: undefined }))
  }

  const mudarRendimentos = (rendimentos) => {
    setDados((d) => ({ ...d, rendimentos }))
    setErros({})
  }

  async function avancar(e) {
    e.preventDefault()
    const errosPasso = validarPasso(passo, dados)
    setErros(errosPasso)
    setErroGeral('')
    if (Object.keys(errosPasso).length) return
    if (passo < PASSOS.length - 1) {
      setPasso(passo + 1)
      return
    }

    setEnviando(true)
    try {
      await cadastrarCliente(paraApi(dados))
      navegar('/cliente', { replace: true })
    } catch (erro) {
      // Leva o usuário ao passo do primeiro campo com erro.
      const campos = { ...erro.campos }
      if (erro.status === 409) {
        if (erro.message.includes('CPF')) campos.cpf = erro.message
        else if (erro.message.includes('usuário')) campos.login = erro.message
      }
      const passosComErro = Object.keys(campos).map((c) => PASSO_DO_CAMPO[c.replace(/\[\d+\].*/, '')] ?? 3)
      if (passosComErro.length) setPasso(Math.min(...passosComErro))
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
          <div className="sobretitulo azul">
            CADASTRO DE CLIENTE · PASSO {passo + 1} DE {PASSOS.length}
          </div>
          <h1 className="titulo-grande esquerda">Crie sua conta de cliente</h1>
          <Stepper passos={PASSOS} atual={passo} />
        </div>

        <div className="cadastro-corpo">
          <form className="card form-card" onSubmit={avancar} noValidate>
            <div className="form-card-topo">
              <h2 className="card-h">{PASSOS[passo]}</h2>
              <p className="sub">
                {passo === 0 && 'Use os mesmos dados do seu documento de identidade.'}
                {passo === 1 && 'Endereço onde você mora.'}
                {passo === 2 && 'Informe até 3 rendimentos. Eles entram na análise financeira.'}
                {passo === 3 && 'Você vai usar esse usuário e essa senha para entrar.'}
              </p>
            </div>

            <div className="form-grade">
              {erroGeral && (
                <div className="alerta-erro" role="alert">
                  <Icone nome="alert" tamanho={18} cor="#B42318" />
                  <div className="alerta-titulo">{erroGeral}</div>
                </div>
              )}

              {passo === 0 && (
                <>
                  <Campo rotulo="Nome completo" value={dados.nome} onChange={mudar('nome')} erro={erros.nome} autoComplete="name" autoFocus />
                  <div className="linha2">
                    <Campo rotulo="CPF" mono inputMode="numeric" placeholder="000.000.000-00" value={dados.cpf} onChange={mudar('cpf', mascaraCpf)} erro={erros.cpf} />
                    <Campo rotulo="RG" mono value={dados.rg} onChange={mudar('rg')} erro={erros.rg} />
                  </div>
                </>
              )}

              {passo === 1 && (
                <>
                  <div className="linha-endereco">
                    <Campo className="cep" rotulo="CEP" mono inputMode="numeric" placeholder="00000-000" value={dados.cep} onChange={mudar('cep', mascaraCep)} erro={erros['endereco.cep']} autoFocus />
                    <Campo rotulo="Rua" value={dados.logradouro} onChange={mudar('logradouro')} erro={erros['endereco.logradouro']} autoComplete="address-line1" />
                    <Campo className="numero" rotulo="Número" mono value={dados.numero} onChange={mudar('numero')} erro={erros['endereco.numero']} />
                  </div>
                  <div className="linha3">
                    <Campo rotulo="Bairro" value={dados.bairro} onChange={mudar('bairro')} erro={erros['endereco.bairro']} />
                    <Campo rotulo="Cidade" value={dados.cidade} onChange={mudar('cidade')} erro={erros['endereco.cidade']} autoComplete="address-level2" />
                    <Campo rotulo="UF" value={dados.uf} onChange={mudar('uf')} opcoes={UFS.map((uf) => ({ valor: uf, rotulo: uf }))} />
                  </div>
                </>
              )}

              {passo === 2 && (
                <>
                  <Campo rotulo="Profissão" icone="briefcase" value={dados.profissao} onChange={mudar('profissao')} autoFocus />
                  <EditorRendimentos rendimentos={dados.rendimentos} aoMudar={mudarRendimentos} erros={erros} />
                </>
              )}

              {passo === 3 && (
                <>
                  <Campo rotulo="Usuário" icone="user" value={dados.login} onChange={mudar('login')} erro={erros.login} autoComplete="username" autoFocus />
                  <div className="linha2">
                    <Campo rotulo="Senha" icone="lock" tipo="senha" value={dados.senha} onChange={mudar('senha')} erro={erros.senha} autoComplete="new-password" dica="Mínimo de 6 caracteres." />
                    <Campo rotulo="Confirmar senha" icone="lock" tipo="senha" value={dados.confirmarSenha} onChange={mudar('confirmarSenha')} erro={erros.confirmarSenha} autoComplete="new-password" />
                  </div>
                </>
              )}
            </div>

            <div className="form-rodape">
              <Botao variante="secondary" icone="chevleft" onClick={() => (passo === 0 ? navegar('/cadastro') : setPasso(passo - 1))}>
                Voltar
              </Botao>
              <Botao type="submit" carregando={enviando}>
                {passo === PASSOS.length - 1 ? 'Criar conta' : 'Próximo'}
              </Botao>
            </div>
          </form>

          <aside className="nota-lateral">
            {passo === 0 ? (
              <>
                <Icone nome="shield" tamanho={22} cor="#1F4FD8" />
                <div className="nota-titulo">Por que pedimos isso?</div>
                <p className="nota-texto">Os agentes (locadoras e bancos) usam seus dados e a sua renda para fazer a análise financeira dos seus pedidos de aluguel.</p>
                <div className="item-check">
                  <Icone nome="check" tamanho={16} cor="#1F4FD8" traco={2.5} />
                  Senha guardada com criptografia
                </div>
                <div className="item-check">
                  <Icone nome="check" tamanho={16} cor="#1F4FD8" traco={2.5} />
                  Você pode editar depois em Meu perfil
                </div>
              </>
            ) : (
              <>
                <div className="nota-titulo">Seu cadastro</div>
                <ul className="mini-passos">
                  {PASSOS.map((p, i) => (
                    <li key={p} className={i < passo ? 'feito' : i === passo ? 'atual' : ''}>
                      <Icone nome={i < passo ? 'checkcircle' : i === passo ? 'clock' : 'lock'} tamanho={16} />
                      {p}
                    </li>
                  ))}
                </ul>
              </>
            )}
          </aside>
        </div>
      </main>
    </div>
  )
}
