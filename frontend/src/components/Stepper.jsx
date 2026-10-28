import { Fragment } from 'react'
import Icone from './Icone.jsx'

/** Etapas de um formulário longo. atual é o índice (0, 1, 2...). */
export default function Stepper({ passos, atual }) {
  return (
    <ol className="stepper">
      {passos.map((passo, i) => {
        const estado = i < atual ? 'feito' : i === atual ? 'ativo' : ''
        return (
          <Fragment key={passo}>
            <li className={`passo ${estado}`} aria-current={i === atual ? 'step' : undefined}>
              <span className="passo-numero">{i < atual ? <Icone nome="check" tamanho={14} cor="#FFFFFF" traco={3} /> : i + 1}</span>
              <span>{passo}</span>
            </li>
            {i < passos.length - 1 && <li className={`passo-linha ${i < atual ? 'feito' : ''}`} aria-hidden="true" />}
          </Fragment>
        )
      })}
    </ol>
  )
}
