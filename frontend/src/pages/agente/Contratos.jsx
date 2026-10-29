import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import DonoPill from '../../components/DonoPill.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import MiniCarro from '../../components/MiniCarro.jsx'
import Placa from '../../components/Placa.jsx'
import Toast from '../../components/Toast.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as agenteService from '../../services/agenteService.js'
import { formatarMoeda } from '../../utils/formatos.js'
import { formatarData, nomeDoCarro } from '../../utils/pedidos.js'

const FILTROS = [
  { chave: 'TODOS', rotulo: 'Todos', filtro: () => true },
  { chave: 'COM', rotulo: 'Com crédito', filtro: (c) => !!c.credito },
  { chave: 'SEM', rotulo: 'Sem crédito', filtro: (c) => !c.credito },
]

/** UC12 Consultar contratos: contratos executados a partir de pedidos aprovados. */
export default function Contratos() {
  const { token } = useAuth()
  const navegar = useNavigate()
  const local = useLocation()
  const sessaoExpirada = useSessaoExpirada()
  const [contratos, setContratos] = useState(null)
  const [erro, setErro] = useState('')
  const [filtro, setFiltro] = useState('TODOS')
  const [busca, setBusca] = useState('')
  const [aviso, setAviso] = useState(local.state?.aviso ? { tipo: 'ok', ...local.state.aviso } : null)

  useEffect(() => {
    agenteService
      .listarContratos(token)
      .then(setContratos)
      .catch((e) => (e.status === 401 ? sessaoExpirada() : setErro(e.message)))
  }, [token, sessaoExpirada])

  const lista = contratos ?? []
  const termo = busca.trim().toLowerCase()
  const visiveis = lista.filter(
    (c) =>
      FILTROS.find((f) => f.chave === filtro).filtro(c) &&
      (!termo || c.numero.toLowerCase().includes(termo) || c.cliente.toLowerCase().includes(termo) || nomeDoCarro(c.automovel).toLowerCase().includes(termo)),
  )

  return (
    <LayoutApp>
      <div className="pagina-topo">
        <h1>Contratos</h1>
        <p className="sub">Contratos executados a partir de pedidos aprovados.</p>
      </div>

      {erro && (
        <div className="alerta-erro" role="alert">
          <Icone nome="alert" tamanho={18} cor="#B42318" />
          <div className="alerta-titulo">{erro}</div>
        </div>
      )}
      {!contratos && !erro && (
        <div className="carregando-pagina">
          <span className="spinner escuro" aria-hidden="true" /> Carregando contratos...
        </div>
      )}

      {contratos && (
        <div className="card tabela">
          <div className="tabela-barra com-chips">
            <div className="chips" role="group" aria-label="Filtrar por crédito">
              {FILTROS.map((f) => (
                <button key={f.chave} type="button" className={`chip ${filtro === f.chave ? 'ativo' : ''}`} aria-pressed={filtro === f.chave} onClick={() => setFiltro(f.chave)}>
                  {f.rotulo}
                </button>
              ))}
            </div>
            <div className="busca">
              <Icone nome="search" tamanho={16} cor="#6B7080" />
              <input aria-label="Buscar contrato, cliente ou automóvel" placeholder="Buscar contrato ou cliente" value={busca} onChange={(e) => setBusca(e.target.value)} />
            </div>
          </div>

          {lista.length === 0 ? (
            <div className="vazio">
              <div className="vazio-icone">
                <Icone nome="file" tamanho={28} cor="#3F4450" />
              </div>
              <div className="vazio-titulo">Nenhum contrato executado ainda</div>
              <p className="vazio-texto">Quando um pedido aprovado vira contrato, ele aparece aqui com o proprietário do automóvel e o crédito.</p>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Contrato</th>
                  <th>Cliente</th>
                  <th>Automóvel</th>
                  <th>Proprietário</th>
                  <th>Crédito</th>
                  <th>Valor</th>
                  <th>Data</th>
                  <th>
                    <span className="sr-only">Abrir</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {visiveis.map((c) => (
                  <tr key={c.id} className="clicavel" onClick={() => navegar(`/agente/pedidos/${c.pedidoId}`)} title={`Ver o pedido ${c.pedido}`}>
                    <td className="mono forte nao-quebra">{c.numero}</td>
                    <td>{c.cliente}</td>
                    <td>
                      <div className="celula-carro">
                        <MiniCarro automovel={c.automovel} largura={56} altura={38} />
                        <div className="pilha">
                          <div className="forte">
                            {nomeDoCarro(c.automovel)} {c.automovel.ano}
                          </div>
                          <Placa numero={c.automovel.placa} tamanho="sm" />
                        </div>
                      </div>
                    </td>
                    <td>
                      <DonoPill tipo={c.tipoProprietario} />
                    </td>
                    <td>
                      {c.credito ? (
                        <span className="credito-sim">
                          <Icone nome="landmark" tamanho={14} cor="#1E40AF" /> {c.credito.banco}
                        </span>
                      ) : (
                        <span className="suave">Não</span>
                      )}
                    </td>
                    <td className="mono">{formatarMoeda(c.valor)}</td>
                    <td className="mono nao-quebra">{formatarData(c.dataExecucao)}</td>
                    <td>
                      <Icone nome="chevright" tamanho={18} cor="#9A9DA6" />
                    </td>
                  </tr>
                ))}
                {visiveis.length === 0 && (
                  <tr>
                    <td colSpan={8} className="sem-resultado">
                      Nenhum contrato com esse filtro.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
          {lista.length > 0 && (
            <div className="tabela-rodape">
              <span className="suave">
                Mostrando {visiveis.length} de {lista.length} {lista.length === 1 ? 'contrato' : 'contratos'}
              </span>
              <span className="suave">Clique em um contrato para ver o pedido.</span>
            </div>
          )}
        </div>
      )}

      {aviso && <Toast {...aviso} aoFechar={() => setAviso(null)} />}
    </LayoutApp>
  )
}
