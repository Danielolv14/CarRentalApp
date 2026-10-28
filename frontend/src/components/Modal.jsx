import { useEffect, useId } from 'react'
import Icone from './Icone.jsx'

/** Caixa de confirmação por cima da tela. Fecha com Esc ou clicando fora. */
export default function Modal({ titulo, icone = 'alert', perigo = false, aoFechar, children, acoes }) {
  const idTitulo = useId()

  useEffect(() => {
    const tecla = (e) => e.key === 'Escape' && aoFechar()
    document.addEventListener('keydown', tecla)
    return () => document.removeEventListener('keydown', tecla)
  }, [aoFechar])

  return (
    <div className="modal-fundo" onMouseDown={(e) => e.target === e.currentTarget && aoFechar()}>
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby={idTitulo}>
        <div className={`modal-icone ${perigo ? 'perigo' : ''}`}>
          <Icone nome={icone} tamanho={24} cor={perigo ? '#B42318' : '#1F4FD8'} />
        </div>
        <h2 className="modal-titulo" id={idTitulo}>
          {titulo}
        </h2>
        <div className="modal-texto">{children}</div>
        <div className="modal-acoes">{acoes}</div>
      </div>
    </div>
  )
}
