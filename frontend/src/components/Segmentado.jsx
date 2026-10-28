import Icone from './Icone.jsx'

/** Escolha única em formato de abas. opcoes: [{ valor, rotulo, icone }]. */
export default function Segmentado({ opcoes, valor, aoMudar, rotulo }) {
  return (
    <div className="seg" role="radiogroup" aria-label={rotulo}>
      {opcoes.map((o) => {
        const ativo = o.valor === valor
        return (
          <button
            key={o.valor}
            type="button"
            role="radio"
            aria-checked={ativo}
            className={`seg-item ${ativo ? 'ativo' : ''}`}
            onClick={() => aoMudar(o.valor)}
          >
            {o.icone && <Icone nome={o.icone} tamanho={16} />}
            <span>{o.rotulo}</span>
          </button>
        )
      })}
    </div>
  )
}
