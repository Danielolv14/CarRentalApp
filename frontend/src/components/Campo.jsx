import { useId, useState } from 'react'
import Icone from './Icone.jsx'

/**
 * Campo de formulário com rótulo, ícone, erro e dica.
 * tipo "senha" ganha o botão de mostrar/esconder. opcoes transforma o campo em select.
 */
export default function Campo({ rotulo, erro, dica, icone, tipo = 'text', mono = false, opcoes, className, ...input }) {
  const id = useId()
  const [verSenha, setVerSenha] = useState(false)
  const ehSenha = tipo === 'senha'
  const classes = ['input', erro && 'erro', input.disabled && 'desabilitado'].filter(Boolean).join(' ')

  return (
    <div className={`campo ${className ?? ''}`}>
      {rotulo && (
        <label className="rotulo" htmlFor={id}>
          {rotulo}
        </label>
      )}
      <div className={classes}>
        {icone && <Icone nome={icone} tamanho={18} cor="#6B7080" />}
        {opcoes ? (
          <select id={id} className={mono ? 'mono' : ''} aria-invalid={!!erro} {...input}>
            {opcoes.map((o) => (
              <option key={o.valor} value={o.valor}>
                {o.rotulo}
              </option>
            ))}
          </select>
        ) : (
          <input
            id={id}
            type={ehSenha && !verSenha ? 'password' : ehSenha ? 'text' : tipo}
            className={mono ? 'mono' : ''}
            aria-invalid={!!erro}
            {...input}
          />
        )}
        {ehSenha && (
          <button type="button" className="ver-senha" onClick={() => setVerSenha((v) => !v)} aria-label={verSenha ? 'Esconder senha' : 'Mostrar senha'}>
            <Icone nome={verSenha ? 'eyeoff' : 'eye'} tamanho={18} cor="#6B7080" />
          </button>
        )}
      </div>
      {erro ? (
        <div className="ajuda erro" role="alert">
          <Icone nome="alert" tamanho={14} cor="#B42318" />
          <span>{erro}</span>
        </div>
      ) : (
        dica && <div className="ajuda">{dica}</div>
      )}
    </div>
  )
}
