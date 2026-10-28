import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import Campo from '../../components/Campo.jsx'
import EditorRendimentos from '../../components/EditorRendimentos.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import Modal from '../../components/Modal.jsx'
import Toast from '../../components/Toast.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import * as clienteService from '../../services/clienteService.js'
import { iniciais, mascaraCep, mascaraCpf, moedaParaCampo, UFS } from '../../utils/formatos.js'
import { enderecoParaApi, rendimentosParaApi, validarEndereco, validarRendimentos } from '../../utils/validacoes.js'

const SECOES = [
  { ancora: 'dados', icone: 'user', rotulo: 'Dados pessoais' },
  { ancora: 'endereco', icone: 'home', rotulo: 'Endereço' },
  { ancora: 'renda', icone: 'briefcase', rotulo: 'Profissão e renda' },
  { ancora: 'excluir', icone: 'trash', rotulo: 'Zona de perigo', perigo: true },
]

function paraFormulario(c) {
  const e = c.endereco ?? {}
  return {
    nome: c.nome ?? '',
    rg: c.rg ?? '',
    profissao: c.profissao ?? '',
    cep: mascaraCep(e.cep ?? ''),
    logradouro: e.logradouro ?? '',
    numero: e.numero ?? '',
    bairro: e.bairro ?? '',
    cidade: e.cidade ?? '',
    uf: e.uf ?? 'MG',
    rendimentos: c.rendimentos.map((r) => ({ entidade: r.entidadeEmpregadora, valor: moedaParaCampo(r.valor) })),
  }
}

function validar(f) {
  const e = { ...validarEndereco(f), ...validarRendimentos(f.rendimentos) }
  if (!f.nome.trim()) e.nome = 'Informe o nome.'
  if (!f.rg.trim()) e.rg = 'Informe o RG.'
  return e
}

const camposAlterados = (atual, original) =>
  Object.keys(atual).filter((k) => JSON.stringify(atual[k]) !== JSON.stringify(original[k])).length

/** UC03 Manter dados cadastrais: o cliente vê, altera e exclui o próprio cadastro. */
export default function MeuPerfil() {
  const { token, sair, atualizarUsuario } = useAuth()
  const navegar = useNavigate()
  const [cliente, setCliente] = useState(null)
  const [original, setOriginal] = useState(null)
  const [form, setForm] = useState(null)
  const [erros, setErros] = useState({})
  const [erroCarga, setErroCarga] = useState('')
  const [salvando, setSalvando] = useState(false)
  const [confirmarExclusao, setConfirmarExclusao] = useState(false)
  const [excluindo, setExcluindo] = useState(false)
  const [aviso, setAviso] = useState(null)

  const sessaoExpirada = useCallback(() => {
    sair()
    navegar('/login', { replace: true, state: { aviso: 'Sua sessão expirou. Entre de novo.' } })
  }, [sair, navegar])

  const carregar = useCallback((dados) => {
    const f = paraFormulario(dados)
    setCliente(dados)
    setOriginal(f)
    setForm(f)
  }, [])

  useEffect(() => {
    clienteService
      .buscarPerfil(token)
      .then(carregar)
      .catch((erro) => (erro.status === 401 ? sessaoExpirada() : setErroCarga(erro.message)))
  }, [token, carregar, sessaoExpirada])

  const mudar = (campo, mascara) => (e) => {
    const valor = mascara ? mascara(e.target.value) : e.target.value
    setForm((f) => ({ ...f, [campo]: valor }))
    setErros((er) => ({ ...er, [campo]: undefined, [`endereco.${campo}`]: undefined }))
  }

  async function salvar(e) {
    e.preventDefault()
    const encontrados = validar(form)
    setErros(encontrados)
    if (Object.keys(encontrados).length) {
      setAviso({ tipo: 'erro', titulo: 'Confira os campos destacados.' })
      return
    }
    setSalvando(true)
    try {
      const atualizado = await clienteService.atualizarPerfil(token, {
        nome: form.nome.trim(),
        rg: form.rg.trim(),
        profissao: form.profissao.trim() || null,
        endereco: enderecoParaApi(form),
        rendimentos: rendimentosParaApi(form.rendimentos),
      })
      carregar(atualizado)
      atualizarUsuario({ nome: atualizado.nome })
      setAviso({ tipo: 'ok', titulo: 'Dados salvos', texto: 'Seu cadastro foi atualizado.' })
    } catch (erro) {
      if (erro.status === 401) return sessaoExpirada()
      setErros(erro.campos)
      setAviso({ tipo: 'erro', titulo: 'Não foi possível salvar', texto: erro.message })
    } finally {
      setSalvando(false)
    }
  }

  async function excluir() {
    setExcluindo(true)
    try {
      await clienteService.excluirConta(token)
      sair()
      navegar('/login', { replace: true, state: { aviso: 'Sua conta foi excluída.' } })
    } catch (erro) {
      if (erro.status === 401) return sessaoExpirada()
      setConfirmarExclusao(false)
      setAviso({ tipo: 'erro', titulo: 'Não foi possível excluir', texto: erro.message })
    } finally {
      setExcluindo(false)
    }
  }

  const alterados = form && original ? camposAlterados(form, original) : 0

  return (
    <LayoutApp>
      <div className="pagina-topo">
        <h1>Meu perfil</h1>
        <p className="sub">Mantenha seus dados atualizados. Eles são usados na análise dos seus pedidos.</p>
      </div>

      {!form && !erroCarga && (
        <div className="carregando-pagina">
          <span className="spinner escuro" aria-hidden="true" /> Carregando seus dados...
        </div>
      )}
      {erroCarga && (
        <div className="alerta-erro" role="alert">
          <Icone nome="alert" tamanho={18} cor="#B42318" />
          <div className="alerta-titulo">{erroCarga}</div>
        </div>
      )}

      {form && (
        <div className="perfil-colunas">
          <aside className="card perfil-cartao">
            <div className="perfil-topo">
              <div className="avatar grande">{iniciais(cliente.nome)}</div>
              <div className="perfil-nome">{cliente.nome}</div>
              <div className="perfil-cpf mono">{mascaraCpf(cliente.cpf)}</div>
              <span className="pill">
                <Icone nome="user" tamanho={14} cor="#3F4450" />@{cliente.login}
              </span>
            </div>
            <nav className="perfil-nav" aria-label="Seções do perfil">
              {SECOES.map((s) => (
                <a key={s.ancora} href={`#${s.ancora}`} className={s.perigo ? 'perigo' : ''}>
                  <Icone nome={s.icone} tamanho={16} />
                  {s.rotulo}
                </a>
              ))}
            </nav>
          </aside>

          <form className="perfil-secoes" onSubmit={salvar} noValidate>
            <section id="dados" className="card">
              <h2 className="card-cabeca">Dados pessoais</h2>
              <div className="card-corpo form-grade">
                <Campo rotulo="Nome completo" value={form.nome} onChange={mudar('nome')} erro={erros.nome} autoComplete="name" />
                <div className="linha2">
                  <Campo rotulo="CPF" mono value={mascaraCpf(cliente.cpf)} disabled dica="O CPF não pode ser alterado." />
                  <Campo rotulo="RG" mono value={form.rg} onChange={mudar('rg')} erro={erros.rg} />
                </div>
              </div>
            </section>

            <section id="endereco" className="card">
              <h2 className="card-cabeca">Endereço</h2>
              <div className="card-corpo form-grade">
                <div className="linha-endereco">
                  <Campo className="cep" rotulo="CEP" mono inputMode="numeric" value={form.cep} onChange={mudar('cep', mascaraCep)} erro={erros['endereco.cep']} />
                  <Campo rotulo="Rua" value={form.logradouro} onChange={mudar('logradouro')} erro={erros['endereco.logradouro']} />
                  <Campo className="numero" rotulo="Número" mono value={form.numero} onChange={mudar('numero')} erro={erros['endereco.numero']} />
                </div>
                <div className="linha3">
                  <Campo rotulo="Bairro" value={form.bairro} onChange={mudar('bairro')} erro={erros['endereco.bairro']} />
                  <Campo rotulo="Cidade" value={form.cidade} onChange={mudar('cidade')} erro={erros['endereco.cidade']} />
                  <Campo rotulo="UF" value={form.uf} onChange={mudar('uf')} opcoes={UFS.map((uf) => ({ valor: uf, rotulo: uf }))} />
                </div>
              </div>
            </section>

            <section id="renda" className="card">
              <h2 className="card-cabeca">Profissão e renda</h2>
              <div className="card-corpo form-grade">
                <Campo rotulo="Profissão" icone="briefcase" value={form.profissao} onChange={mudar('profissao')} />
                <EditorRendimentos
                  rendimentos={form.rendimentos}
                  aoMudar={(rendimentos) => {
                    setForm((f) => ({ ...f, rendimentos }))
                    setErros({})
                  }}
                  erros={erros}
                />
              </div>
            </section>

            {alterados > 0 && (
              <div className="barra-salvar">
                <span className="suave">
                  Você alterou {alterados} {alterados === 1 ? 'campo' : 'campos'}.
                </span>
                <div className="barra-acoes">
                  <Botao
                    variante="secondary"
                    onClick={() => {
                      setForm(original)
                      setErros({})
                    }}
                  >
                    Descartar
                  </Botao>
                  <Botao type="submit" icone="check" carregando={salvando}>
                    Salvar alterações
                  </Botao>
                </div>
              </div>
            )}

            <section id="excluir" className="card zona-perigo">
              <div>
                <h2 className="zp-titulo">Excluir conta</h2>
                <p className="zp-texto">
                  Apaga seu cadastro e cancela os pedidos que ainda estão em análise. Contratos já executados continuam registrados.
                </p>
              </div>
              <Botao variante="danger-outline" icone="trash" onClick={() => setConfirmarExclusao(true)}>
                Excluir conta
              </Botao>
            </section>
          </form>
        </div>
      )}

      {confirmarExclusao && (
        <Modal
          titulo="Excluir sua conta?"
          icone="trash"
          perigo
          aoFechar={() => !excluindo && setConfirmarExclusao(false)}
          acoes={
            <>
              <Botao variante="secondary" onClick={() => setConfirmarExclusao(false)} disabled={excluindo}>
                Manter conta
              </Botao>
              <Botao variante="danger" icone="trash" onClick={excluir} carregando={excluindo}>
                Excluir conta
              </Botao>
            </>
          }
        >
          Seu cadastro será apagado e os pedidos em análise serão cancelados. Contratos já executados continuam registrados. Essa ação não pode ser desfeita.
        </Modal>
      )}

      {aviso && <Toast {...aviso} aoFechar={() => setAviso(null)} />}
    </LayoutApp>
  )
}
