/** Placa no padrão Mercosul. tamanho: "sm", "md" ou "lg". */
export default function Placa({ numero, tamanho = 'md' }) {
  return (
    <div className={`placa ${tamanho}`}>
      <div className="placa-topo">BRASIL</div>
      <div className="placa-numero">{numero}</div>
    </div>
  )
}
