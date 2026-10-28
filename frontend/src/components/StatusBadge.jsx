import { STATUS } from '../utils/pedidos.js'

/** Selo colorido com o status do pedido (Enviado, Em análise, Aprovado...). */
export default function StatusBadge({ status }) {
  const s = STATUS[status] ?? { rotulo: status, classe: 'enviado' }
  return (
    <span className={`badge ${s.classe}`}>
      <span className="badge-ponto" />
      <span className="badge-texto">{s.rotulo}</span>
    </span>
  )
}
