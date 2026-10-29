import { useCallback, useEffect, useState } from 'react'
import Botao from '../../components/Botao.jsx'
import DonoPill from '../../components/DonoPill.jsx'
import GavetaAutomovel from '../../components/GavetaAutomovel.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import MiniCarro from '../../components/MiniCarro.jsx'
import Modal from '../../components/Modal.jsx'
import Placa from '../../components/Placa.jsx'
import Toast from '../../components/Toast.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as automovelService from '../../services/automovelService.js'
import { formatarMoeda } from '../../utils/formatos.js'
import { nomeDoCarro } from '../../utils/pedidos.js'

/** UC13 Manter automóveis: a empresa cadastra, edita e remove os automóveis da frota. */
export default function Automoveis() {
  const { usuario, token } = useAuth()
  const sessaoExpirada = useSessaoExpirada()
  const [automoveis, setAutomoveis] = useState(null)
  const [erro, setErro] = useState('')
  const [editando, setEditando] = useState(undefined) // undefined = gaveta fechada, null = cadastro, objeto = edição
  const [salvando, setSalvando] = useState(false)
  const [errosApi, setErrosApi] = useState(null)
  const [removendo, setRemovendo] = useState(null)
  const [aviso, setAviso] = useState(null)

  useEffect(() => {
    automovelService
      .listarAutomoveis(token)
      .then(setAutomoveis)
      .catch((e) => (e.status === 401 ? sessaoExpirada() : setErro(e.message)))
  }, [token, sessaoExpirada])

  const fecharGaveta = useCallback(() => {
    setEditando(undefined)
    setErrosApi(null)
  }, [])

  async function salvar(dados) {
    setSalvando(true)
    try {
      const salvo = editando
        ? await automovelService.atualizarAutomovel(token, editando.id, dados)
        : await automovelService.cadastrarAutomovel(token, dados)
      setAutomoveis((lista) =>
        (editando ? lista.map((a) => (a.id === salvo.id ? salvo : a)) : [...lista, salvo]).sort((x, y) =>
          nomeDoCarro(x).localeCompare(nomeDoCarro(y), 'pt-BR'),
        ),
      )
      setAviso({ tipo: 'ok', titulo: editando ? 'Automóvel atualizado' : 'Automóvel cadastrado', texto: `${nomeDoCarro(salvo)} · ${salvo.placa}` })
      fecharGaveta()
    } catch (falha) {
      if (falha.status === 401) return sessaoExpirada()
      if (Object.keys(falha.campos).length) setErrosApi(falha.campos)
      else if (falha.status === 409) setErrosApi({ [falha.message.includes('placa') ? 'placa' : 'matricula']: falha.message })
      else setAviso({ tipo: 'erro', titulo: 'Não foi possível salvar', texto: falha.message })
    } finally {
      setSalvando(false)
    }
  }

  async function confirmarRemocao() {
    setSalvando(true)
    try {
      await automovelService.removerAutomovel(token, removendo.id)
      setAutomoveis((lista) => lista.filter((a) => a.id !== removendo.id))
      setAviso({ tipo: 'ok', titulo: 'Automóvel removido', texto: `${nomeDoCarro(removendo)} · ${removendo.placa}` })
    } catch (falha) {
      if (falha.status === 401) return sessaoExpirada()
      setAviso({ tipo: 'erro', titulo: 'Não foi possível remover', texto: falha.message })
    } finally {
      setSalvando(false)
      setRemovendo(null)
    }
  }

  return (
    <LayoutApp>
      <div className="pagina-topo com-acao">
        <div>
          <h1>Automóveis</h1>
          <p className="sub">Frota da {usuario.nome} disponível para pedidos.</p>
        </div>
        <Botao icone="plus" onClick={() => setEditando(null)}>
          Cadastrar automóvel
        </Botao>
      </div>

      {erro && (
        <div className="alerta-erro" role="alert">
          <Icone nome="alert" tamanho={18} cor="#B42318" />
          <div className="alerta-titulo">{erro}</div>
        </div>
      )}
      {!automoveis && !erro && (
        <div className="carregando-pagina">
          <span className="spinner escuro" aria-hidden="true" /> Carregando a frota...
        </div>
      )}

      {automoveis && (
        <div className="card tabela">
          {automoveis.length === 0 ? (
            <div className="vazio">
              <div className="vazio-icone">
                <Icone nome="car" tamanho={28} cor="#3F4450" />
              </div>
              <div className="vazio-titulo">Nenhum automóvel na frota</div>
              <p className="vazio-texto">Cadastre os automóveis para que os clientes possam pedir o aluguel.</p>
              <Botao icone="plus" onClick={() => setEditando(null)}>
                Cadastrar o primeiro automóvel
              </Botao>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Automóvel</th>
                  <th>Ano</th>
                  <th>Placa</th>
                  <th>Matrícula (RENAVAM)</th>
                  <th>Proprietário</th>
                  <th className="acoes-col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {automoveis.map((a) => (
                  <tr key={a.id} className={editando?.id === a.id ? 'selecionada' : ''}>
                    <td>
                      <div className="celula-carro">
                        <MiniCarro automovel={a} />
                        <div>
                          <div className="forte">{nomeDoCarro(a)}</div>
                          <div className="passo-sub">Diária {formatarMoeda(a.valorDiaria)}</div>
                        </div>
                      </div>
                    </td>
                    <td className="mono">{a.ano}</td>
                    <td>
                      <Placa numero={a.placa} tamanho="sm" />
                    </td>
                    <td className="mono">{a.matricula}</td>
                    <td>
                      <DonoPill tipo={a.proprietario} />
                    </td>
                    <td className="acoes-col">
                      <div className="acoes">
                        <button type="button" className="botao-icone pequeno" onClick={() => setEditando(a)} aria-label={`Editar ${nomeDoCarro(a)} ${a.placa}`} title="Editar">
                          <Icone nome="pencil" tamanho={16} cor="#3F4450" />
                        </button>
                        <button type="button" className="botao-icone pequeno" onClick={() => setRemovendo(a)} aria-label={`Remover ${nomeDoCarro(a)} ${a.placa}`} title="Remover">
                          <Icone nome="trash" tamanho={16} cor="#B42318" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {automoveis.length > 0 && (
            <div className="tabela-rodape">
              <span className="suave">
                {automoveis.length} {automoveis.length === 1 ? 'automóvel' : 'automóveis'} na frota
              </span>
              <span className="suave">Automóvel que já aparece em pedidos não pode ser removido.</span>
            </div>
          )}
        </div>
      )}

      {editando !== undefined && (
        <GavetaAutomovel key={editando?.id ?? 'novo'} automovel={editando} aoSalvar={salvar} aoFechar={fecharGaveta} salvando={salvando} errosApi={errosApi} />
      )}

      {removendo && (
        <Modal
          titulo={`Remover o ${nomeDoCarro(removendo)} (${removendo.placa})?`}
          perigo
          aoFechar={() => !salvando && setRemovendo(null)}
          acoes={
            <>
              <Botao variante="secondary" onClick={() => setRemovendo(null)} disabled={salvando}>
                Manter automóvel
              </Botao>
              <Botao variante="danger" icone="trash" onClick={confirmarRemocao} carregando={salvando}>
                Remover
              </Botao>
            </>
          }
        >
          O automóvel sai da frota e não aparece mais para os clientes. Se ele já estiver em algum pedido, ele continua no histórico e não pode ser removido.
        </Modal>
      )}

      {aviso && <Toast {...aviso} aoFechar={() => setAviso(null)} />}
    </LayoutApp>
  )
}
