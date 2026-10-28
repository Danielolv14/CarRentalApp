import { coresDoCarro } from '../utils/pedidos.js'
import CarroSvg from './CarroSvg.jsx'

/** Miniatura do carro para listas e tabelas. */
export default function MiniCarro({ automovel, largura = 64, altura = 42 }) {
  const [cor, fundo] = coresDoCarro(automovel)
  return (
    <span className="mini-carro" style={{ width: largura, height: altura, background: fundo }}>
      <CarroSvg cor={cor} largura={largura - 10} />
    </span>
  )
}
