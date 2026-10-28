import Icone from './Icone.jsx'

/** variante: primary | secondary | ghost | danger | amber */
export default function Botao({ children, variante = 'primary', icone, carregando = false, bloco = false, type = 'button', ...resto }) {
  const classes = ['btn', `btn-${variante}`, bloco && 'btn-bloco', carregando && 'carregando'].filter(Boolean).join(' ')
  return (
    <button type={type} className={classes} disabled={carregando || resto.disabled} {...resto}>
      {carregando ? <span className="spinner" aria-hidden="true" /> : icone && <Icone nome={icone} tamanho={16} traco={2.2} />}
      <span>{children}</span>
    </button>
  )
}
