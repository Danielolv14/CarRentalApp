import { formatarMoeda, lerMoeda, mascaraMoeda } from '../utils/formatos.js'
import { MAXIMO_RENDIMENTOS } from '../utils/validacoes.js'
import Botao from './Botao.jsx'
import Campo from './Campo.jsx'
import Icone from './Icone.jsx'

/** Lista de até 3 rendimentos (entidade empregadora + valor mensal) com o total. */
export default function EditorRendimentos({ rendimentos, aoMudar, erros = {} }) {
  const limite = rendimentos.length >= MAXIMO_RENDIMENTOS
  const total = rendimentos.reduce((soma, r) => soma + (lerMoeda(r.valor) ?? 0), 0)

  const mudar = (i, campo, mascara) => (e) => {
    const valor = mascara ? mascara(e.target.value) : e.target.value
    aoMudar(rendimentos.map((r, j) => (j === i ? { ...r, [campo]: valor } : r)))
  }

  return (
    <>
      <div className="secao-rotulo">
        <span>Rendimentos</span>
        <span className="contador">
          {rendimentos.length} de {MAXIMO_RENDIMENTOS}
        </span>
      </div>
      {rendimentos.length === 0 && <p className="suave">Nenhum rendimento informado. Você pode adicionar até 3.</p>}
      <div className="rendimentos">
        {rendimentos.map((r, i) => (
          <div className="rendimento" key={i}>
            <div className="rendimento-n">{i + 1}</div>
            <Campo rotulo={i === 0 ? 'Entidade empregadora' : undefined} aria-label={`Entidade empregadora ${i + 1}`} value={r.entidade} onChange={mudar(i, 'entidade')} erro={erros[`rendimentos[${i}].entidadeEmpregadora`]} />
            <Campo className="valor" rotulo={i === 0 ? 'Rendimento mensal (R$)' : undefined} aria-label={`Rendimento mensal ${i + 1}`} mono inputMode="decimal" placeholder="0,00" value={r.valor} onChange={mudar(i, 'valor', mascaraMoeda)} erro={erros[`rendimentos[${i}].valor`]} />
            <button type="button" className="botao-icone" onClick={() => aoMudar(rendimentos.filter((_, j) => j !== i))} aria-label={`Remover rendimento ${i + 1}`}>
              <Icone nome="trash" tamanho={18} cor="#6B7080" />
            </button>
          </div>
        ))}
      </div>
      <div className="linha-adicionar">
        <Botao variante="secondary" icone="plus" onClick={() => aoMudar([...rendimentos, { entidade: '', valor: '' }])} disabled={limite}>
          Adicionar empregador
        </Botao>
        {limite && (
          <span className="ajuda aviso">
            <Icone nome="info" tamanho={14} cor="#92400E" />
            Máximo de 3 rendimentos.
          </span>
        )}
      </div>
      {erros.rendimentos && <div className="ajuda erro">{erros.rendimentos}</div>}
      {rendimentos.length > 0 && (
        <div className="total">
          <span>Renda mensal total</span>
          <span className="mono total-valor">{formatarMoeda(total)}</span>
        </div>
      )}
    </>
  )
}
