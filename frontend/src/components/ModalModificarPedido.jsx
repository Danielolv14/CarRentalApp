import { useState } from 'react'
import { formatarMoeda } from '../utils/formatos.js'
import { diariasEntre, hojeIso, nomeDoCarro } from '../utils/pedidos.js'
import Botao from './Botao.jsx'
import Campo from './Campo.jsx'
import Icone from './Icone.jsx'
import Modal from './Modal.jsx'

/**
 * UC06 Modificar pedido, pelo cliente (detalhe do pedido) ou pelo agente que avalia: troca o automóvel e as datas.
 * A lista de automóveis vem da página.
 * aoSalvar recebe { automovelId, dataRetirada, dataDevolucao }.
 */
export default function ModalModificarPedido({ pedido, automoveis, aoSalvar, aoFechar, salvando, erroApi }) {
  const [automovelId, setAutomovelId] = useState(String(pedido.automovel.id))
  const [retirada, setRetirada] = useState(pedido.dataRetirada)
  const [devolucao, setDevolucao] = useState(pedido.dataDevolucao)
  const [erros, setErros] = useState({})

  const automovel = automoveis.find((a) => String(a.id) === automovelId) ?? pedido.automovel
  const diarias = diariasEntre(retirada, devolucao)
  const opcoes = (automoveis.length ? automoveis : [pedido.automovel]).map((a) => ({
    valor: String(a.id),
    rotulo: `${nomeDoCarro(a)} ${a.ano} · ${formatarMoeda(a.valorDiaria)}/dia`,
  }))

  function salvar() {
    const e = {}
    if (!retirada) e.dataRetirada = 'Informe a data de retirada.'
    else if (retirada !== pedido.dataRetirada && retirada < hojeIso()) e.dataRetirada = 'A retirada não pode estar no passado.'
    if (!devolucao) e.dataDevolucao = 'Informe a data de devolução.'
    else if (retirada && devolucao <= retirada) e.dataDevolucao = 'A devolução precisa ser depois da retirada.'
    setErros(e)
    if (Object.keys(e).length === 0) aoSalvar({ automovelId: Number(automovelId), dataRetirada: retirada, dataDevolucao: devolucao })
  }

  /** Mexer numa data apaga o erro dela, que só volta a ser conferido ao salvar. */
  function mudarData(campo, definir) {
    return (e) => {
      definir(e.target.value)
      setErros((atuais) => ({ ...atuais, [campo]: undefined }))
    }
  }

  return (
    <Modal
      titulo={`Modificar o pedido ${pedido.numero}`}
      icone="pencil"
      aoFechar={() => !salvando && aoFechar()}
      acoes={
        <>
          <Botao variante="secondary" onClick={aoFechar} disabled={salvando}>
            Cancelar
          </Botao>
          <Botao icone="check" onClick={salvar} carregando={salvando}>
            Salvar alterações
          </Botao>
        </>
      }
    >
      <p>
        O valor é recalculado com a diária atual do automóvel.
        {pedido.credito && ` O crédito pedido ao ${pedido.credito.banco} continua o mesmo.`}
      </p>
      <div className="form-modal">
        <Campo rotulo="Automóvel" icone="car" opcoes={opcoes} value={automovelId} onChange={(e) => setAutomovelId(e.target.value)} />
        <div className="linha2">
          <Campo rotulo="Retirada" tipo="date" mono value={retirada} onChange={mudarData('dataRetirada', setRetirada)} erro={erros.dataRetirada} />
          <Campo rotulo="Devolução" tipo="date" mono min={retirada} value={devolucao} onChange={mudarData('dataDevolucao', setDevolucao)} erro={erros.dataDevolucao} />
        </div>
        <div className="calculo">
          <span>
            {diarias} {diarias === 1 ? 'diária' : 'diárias'} × {formatarMoeda(automovel.valorDiaria)}
          </span>
          <span className="mono">{formatarMoeda(diarias * automovel.valorDiaria)}</span>
        </div>
        {erroApi && (
          <div className="ajuda erro" role="alert">
            <Icone nome="alert" tamanho={14} cor="#B42318" />
            <span>{erroApi}</span>
          </div>
        )}
      </div>
    </Modal>
  )
}
