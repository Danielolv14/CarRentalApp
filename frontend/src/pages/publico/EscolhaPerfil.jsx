import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Botao from '../../components/Botao.jsx'
import CabecalhoPublico from '../../components/CabecalhoPublico.jsx'
import Icone from '../../components/Icone.jsx'

const PERFIS = [
  {
    valor: 'cliente',
    icone: 'user',
    titulo: 'Sou cliente',
    texto: 'Para quem quer alugar um carro: faça pedidos, acompanhe a análise e o contrato.',
    itens: ['Criar, editar e cancelar pedidos', 'Solicitar crédito bancário', 'Acompanhar o status'],
  },
  {
    valor: 'agente',
    icone: 'building',
    titulo: 'Sou agente',
    texto: 'Para locadoras e bancos: avalie pedidos, emita pareceres e execute contratos.',
    itens: ['Avaliar pedidos (parecer)', 'Conceder crédito (bancos)', 'Gerenciar contratos e frota'],
  },
]

export default function EscolhaPerfil() {
  const [escolha, setEscolha] = useState('cliente')
  const navegar = useNavigate()

  return (
    <div className="pub">
      <CabecalhoPublico />
      <main className="coluna-central">
        <div className="sobretitulo azul">CRIAR CONTA</div>
        <h1 className="titulo-grande">Como você vai usar o Rota?</h1>
        <p className="sub centro">Escolha o seu perfil. Ele define o que você pode fazer no sistema.</p>

        <div className="perfis" role="radiogroup" aria-label="Perfil">
          {PERFIS.map((p) => {
            const ativo = escolha === p.valor
            return (
              <button
                key={p.valor}
                type="button"
                role="radio"
                aria-checked={ativo}
                className={`perfil ${ativo ? 'selecionado' : ''}`}
                onClick={() => setEscolha(p.valor)}
                onDoubleClick={() => navegar(`/cadastro/${p.valor}`)}
              >
                <span className={ativo ? 'perfil-check' : 'perfil-radio'}>{ativo && <Icone nome="check" tamanho={14} cor="#FFFFFF" traco={3} />}</span>
                <span className="perfil-icone">
                  <Icone nome={p.icone} tamanho={26} cor={ativo ? '#1F4FD8' : '#3F4450'} />
                </span>
                <span className="perfil-titulo">{p.titulo}</span>
                <span className="perfil-texto">{p.texto}</span>
                <span className="perfil-lista">
                  {p.itens.map((item) => (
                    <span key={item} className="item-check">
                      <Icone nome="check" tamanho={16} cor={ativo ? '#1F4FD8' : '#6B7080'} traco={2.5} />
                      {item}
                    </span>
                  ))}
                </span>
              </button>
            )
          })}
        </div>

        <div className="acoes-linha">
          <Botao variante="secondary" icone="chevleft" onClick={() => navegar('/login')}>
            Voltar para o login
          </Botao>
          <Botao onClick={() => navegar(`/cadastro/${escolha}`)}>Continuar</Botao>
        </div>
      </main>
    </div>
  )
}
