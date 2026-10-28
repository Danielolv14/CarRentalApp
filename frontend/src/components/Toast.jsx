import { useEffect } from 'react'
import Icone from './Icone.jsx'

/** Aviso rápido no canto da tela, que some sozinho. tipo: "ok" ou "erro". */
export default function Toast({ titulo, texto, tipo = 'ok', aoFechar, duracao = 4000 }) {
  useEffect(() => {
    const timer = setTimeout(aoFechar, duracao)
    return () => clearTimeout(timer)
  }, [aoFechar, duracao])

  return (
    <div className="toast" role="status">
      <Icone nome={tipo === 'ok' ? 'checkcircle' : 'alert'} tamanho={20} cor={tipo === 'ok' ? '#4ADE80' : '#F87171'} />
      <div className="toast-texto">
        <div className="toast-titulo">{titulo}</div>
        {texto && <div className="toast-sub">{texto}</div>}
      </div>
      <button type="button" className="toast-fechar" onClick={aoFechar} aria-label="Fechar aviso">
        ×
      </button>
    </div>
  )
}
