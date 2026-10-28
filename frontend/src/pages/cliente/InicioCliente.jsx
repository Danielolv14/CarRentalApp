import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import CarroSvg from '../../components/CarroSvg.jsx'
import Icone from '../../components/Icone.jsx'
import LayoutApp from '../../components/LayoutApp.jsx'
import MiniCarro from '../../components/MiniCarro.jsx'
import StatusBadge from '../../components/StatusBadge.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import useSessaoExpirada from '../../hooks/useSessaoExpirada.js'
import * as pedidoService from '../../services/pedidoService.js'
import { formatarMoeda } from '../../utils/formatos.js'
import { formatarPeriodo, nomeDoCarro } from '../../utils/pedidos.js'

const RESUMO = [
  { rotulo: 'Em análise', status: ['ENVIADO', 'EM_ANALISE'], nota: 'Enviado ou em análise', icone: 'clock', cor: '#92400E', fundo: '#FEF3C7' },
  { rotulo: 'Aprovados', status: ['APROVADO'], nota: 'Aguardando contrato', icone: 'checkcircle', cor: '#166534', fundo: '#DCFCE7' },
  { rotulo: 'Contratados', status: ['CONTRATADO'], nota: 'Contrato executado', icone: 'file', cor: '#1E40AF', fundo: '#DBEAFE' },
  { rotulo: 'Cancelados', status: ['CANCELADO'], nota: 'Cancelados por você', icone: 'alert', cor: '#6B7280', fundo: '#F3F4F6' },
]

/** Página inicial do cliente: resumo dos pedidos e atalhos. */
export default function InicioCliente() {
  const { usuario, token } = useAuth()
  const navegar = useNavigate()
  const sessaoExpirada = useSessaoExpirada()
  const [pedidos, setPedidos] = useState(null)
  const primeiroNome = usuario.nome.split(' ')[0]

  useEffect(() => {
    pedidoService
      .meusPedidos(token)
      .then(setPedidos)
      .catch((erro) => (erro.status === 401 ? sessaoExpirada() : setPedidos([])))
  }, [token, sessaoExpirada])

  const lista = pedidos ?? []
  const aprovado = lista.find((p) => p.status === 'APROVADO')

  return (
    <LayoutApp>
      <div className="pagina-topo com-acao">
        <div>
          <h1>Olá, {primeiroNome}</h1>
          <p className="sub">Acompanhe seus pedidos de aluguel e o andamento de cada análise.</p>
        </div>
        <Botao icone="plus" onClick={() => navegar('/cliente/pedidos/novo')}>
          Novo pedido
        </Botao>
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
            <div className="stat-numero">{pedidos ? lista.filter((p) => r.status.includes(p.status)).length : '–'}</div>
            <div className="stat-nota">{r.nota}</div>
          </div>
        ))}
      </div>

      <div className="inicio-colunas">
        <section className="card inicio-recentes">
          <div className="card-cabeca com-link">
            <h2>Pedidos recentes</h2>
            {lista.length > 0 && (
              <Link className="link" to="/cliente/pedidos">
                Ver todos
              </Link>
            )}
          </div>
          {pedidos && lista.length === 0 && (
            <div className="vazio">
              <div className="vazio-icone">
                <Icone nome="inbox" tamanho={28} cor="#3F4450" />
              </div>
              <div className="vazio-titulo">Você ainda não fez nenhum pedido</div>
              <p className="vazio-texto">Quando você enviar um pedido de aluguel, ele aparece aqui com o status da análise.</p>
            </div>
          )}
          <ul className="lista-pedidos">
            {lista.slice(0, 4).map((p) => (
              <li key={p.id}>
                <Link to={`/cliente/pedidos/${p.id}`} className="linha-pedido">
                  <MiniCarro automovel={p.automovel} largura={72} altura={48} />
                  <span className="lp-texto">
                    <span className="forte">
                      {nomeDoCarro(p.automovel)} {p.automovel.ano}
                    </span>
                    <span className="passo-sub">
                      <span className="mono">{p.numero}</span> · {formatarPeriodo(p.dataRetirada, p.dataDevolucao)}
                    </span>
                  </span>
                  <span className="mono lp-valor">{formatarMoeda(p.valorEstimado)}</span>
                  <StatusBadge status={p.status} />
                  <Icone nome="chevright" tamanho={18} cor="#9A9DA6" />
                </Link>
              </li>
            ))}
          </ul>
        </section>

        <div className="inicio-lateral">
          {aprovado && (
            <section className="card proximo-passo">
              <span className="pp-tag">
                <Icone nome="thumbup" tamanho={16} cor="#166534" /> Boa notícia
              </span>
              <h2 className="pp-titulo">Seu pedido {aprovado.numero} foi aprovado</h2>
              <p className="pp-texto">
                {aprovado.pareceres.at(-1)?.agente ?? 'O agente'} deu parecer positivo. Agora é só aguardar a execução do contrato.
              </p>
              <Botao variante="secondary" onClick={() => navegar(`/cliente/pedidos/${aprovado.id}`)}>
                Ver pedido
              </Botao>
            </section>
          )}
          <section className="promo">
            <div className="promo-texto">
              <h2 className="promo-titulo">Precisa de um carro?</h2>
              <p className="promo-p">Escolha o automóvel, o período e, se quiser, peça crédito ao banco. São 3 passos.</p>
              <Botao variante="amber" icone="plus" onClick={() => navegar('/cliente/pedidos/novo')}>
                Novo pedido
              </Botao>
            </div>
            <div className="promo-carro" aria-hidden="true">
              <CarroSvg cor="#F5F4F0" largura={210} />
            </div>
          </section>
        </div>
      </div>
    </LayoutApp>
  )
}
