import { PROPRIETARIOS } from '../utils/pedidos.js'
import Icone from './Icone.jsx'

/** Selo do proprietário do automóvel (Cliente, Empresa ou Banco). Sem contrato ainda, mostra um traço. */
export default function DonoPill({ tipo }) {
  const dono = PROPRIETARIOS[tipo]
  if (!dono) return <span className="pill sem-dono">Sem contrato</span>
  return (
    <span className="pill">
      <Icone nome={dono.icone} tamanho={14} cor="#3F4450" />
      <span>{dono.rotulo}</span>
    </span>
  )
}
