import Icone from './Icone.jsx'

/** Logo do Rota: quadrado âmbar com o carro + nome. */
export default function Marca({ escuro = false }) {
  return (
    <div className="marca">
      <div className="marca-icone">
        <Icone nome="car" tamanho={20} cor="#15181E" traco={2.2} />
      </div>
      <span className="marca-nome" style={{ color: escuro ? '#FFFFFF' : 'var(--cor-grafite)' }}>
        Rota
      </span>
    </div>
  )
}
