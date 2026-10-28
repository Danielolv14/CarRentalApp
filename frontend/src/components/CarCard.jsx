import { formatarMoeda } from '../utils/formatos.js'
import { coresDoCarro, nomeDoCarro } from '../utils/pedidos.js'
import CarroSvg from './CarroSvg.jsx'
import Icone from './Icone.jsx'
import Placa from './Placa.jsx'

/** Card de automóvel para escolher no novo pedido. */
export default function CarCard({ automovel, selecionado, aoEscolher }) {
  const [cor, fundo] = coresDoCarro(automovel)
  return (
    <button
      type="button"
      role="radio"
      aria-checked={selecionado}
      aria-label={`${nomeDoCarro(automovel)} ${automovel.ano}, placa ${automovel.placa}`}
      className={`car-card ${selecionado ? 'selecionado' : ''}`}
      onClick={aoEscolher}
    >
      <span className="car-img" style={{ background: fundo }}>
        <span className="car-placa">
          <Placa numero={automovel.placa} tamanho="sm" />
        </span>
        {selecionado && (
          <span className="car-check">
            <Icone nome="check" tamanho={14} cor="#FFFFFF" traco={3} />
          </span>
        )}
        <CarroSvg cor={cor} largura={164} />
      </span>
      <span className="car-corpo">
        <span className="car-nome">{nomeDoCarro(automovel)}</span>
        <span className="car-meta">
          {automovel.ano} · <b>{formatarMoeda(automovel.valorDiaria)}</b>/dia
        </span>
      </span>
    </button>
  )
}
