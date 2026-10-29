import { useEffect, useState } from 'react'
import { formatarMoeda, lerMoeda, mascaraMoeda, moedaParaCampo, somenteDigitos } from '../utils/formatos.js'
import { coresDoCarro } from '../utils/pedidos.js'
import Botao from './Botao.jsx'
import CarroSvg from './CarroSvg.jsx'
import Campo from './Campo.jsx'
import DonoPill from './DonoPill.jsx'
import Icone from './Icone.jsx'
import Placa from './Placa.jsx'

const ANO_MAXIMO = new Date().getFullYear() + 1
const PLACA = /^[A-Z]{3}\d[A-Z0-9]\d{2}$/

function validar(f) {
  const e = {}
  if (somenteDigitos(f.matricula).length !== 11) e.matricula = 'A matrícula (RENAVAM) tem 11 dígitos.'
  if (!f.marca.trim()) e.marca = 'Informe a marca.'
  if (!f.modelo.trim()) e.modelo = 'Informe o modelo.'
  const ano = Number(f.ano)
  if (!Number.isInteger(ano) || ano < 1950 || ano > ANO_MAXIMO) e.ano = `Informe um ano entre 1950 e ${ANO_MAXIMO}.`
  if (!PLACA.test(f.placa)) e.placa = 'Placa inválida. Use o padrão ABC1D23.'
  if (!(lerMoeda(f.diaria) > 0)) e.valorDiaria = 'Informe o valor da diária.'
  return e
}

/**
 * Gaveta lateral para cadastrar ou editar um automóvel da frota (UC13).
 * automovel null = cadastro. aoSalvar recebe os dados no formato da API; errosApi traz os erros por campo.
 */
export default function GavetaAutomovel({ automovel, aoSalvar, aoFechar, salvando, errosApi }) {
  const [form, setForm] = useState(() => ({
    matricula: automovel?.matricula ?? '',
    marca: automovel?.marca ?? '',
    modelo: automovel?.modelo ?? '',
    ano: automovel ? String(automovel.ano) : String(new Date().getFullYear()),
    placa: automovel?.placa ?? '',
    diaria: automovel ? moedaParaCampo(automovel.valorDiaria) : '',
  }))
  const [erros, setErros] = useState({})
  const [cor, fundo] = coresDoCarro({ modelo: form.modelo })

  useEffect(() => setErros(errosApi ?? {}), [errosApi])

  useEffect(() => {
    const tecla = (e) => e.key === 'Escape' && !salvando && aoFechar()
    document.addEventListener('keydown', tecla)
    return () => document.removeEventListener('keydown', tecla)
  }, [aoFechar, salvando])

  const mudar = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }))

  function salvar(evento) {
    evento.preventDefault()
    const e = validar(form)
    setErros(e)
    if (Object.keys(e).length) return
    aoSalvar({
      matricula: somenteDigitos(form.matricula),
      marca: form.marca.trim(),
      modelo: form.modelo.trim(),
      ano: Number(form.ano),
      placa: form.placa,
      valorDiaria: lerMoeda(form.diaria),
    })
  }

  return (
    <>
      <div className="gaveta-fundo" onMouseDown={() => !salvando && aoFechar()} />
      <form className="gaveta" role="dialog" aria-modal="true" aria-labelledby="gaveta-titulo" onSubmit={salvar} noValidate>
        <div className="gaveta-topo">
          <div>
            <h2 className="card-titulo" id="gaveta-titulo">
              {automovel ? 'Editar automóvel' : 'Cadastrar automóvel'}
            </h2>
            <div className="passo-sub">{automovel ? `${automovel.marca} ${automovel.modelo} ${automovel.ano}` : 'Novo automóvel da frota'}</div>
          </div>
          <button type="button" className="botao-icone" onClick={aoFechar} aria-label="Fechar" disabled={salvando}>
            <Icone nome="x" tamanho={18} cor="#3F4450" />
          </button>
        </div>

        <div className="gaveta-corpo">
          <div className="gaveta-previa" style={{ background: fundo }} aria-hidden="true">
            <CarroSvg cor={cor} largura={220} />
            {form.placa && (
              <div className="gaveta-placa">
                <Placa numero={form.placa} tamanho="lg" />
              </div>
            )}
          </div>
          <Campo rotulo="Matrícula (RENAVAM)" mono inputMode="numeric" maxLength={11} value={form.matricula} onChange={(e) => setForm((f) => ({ ...f, matricula: somenteDigitos(e.target.value) }))} erro={erros.matricula} />
          <div className="linha2">
            <Campo rotulo="Marca" value={form.marca} onChange={mudar('marca')} erro={erros.marca} maxLength={60} placeholder="Ex.: Volkswagen" />
            <Campo rotulo="Modelo" value={form.modelo} onChange={mudar('modelo')} erro={erros.modelo} maxLength={60} placeholder="Ex.: Polo" />
          </div>
          <div className="linha2">
            <Campo rotulo="Ano" mono inputMode="numeric" maxLength={4} value={form.ano} onChange={(e) => setForm((f) => ({ ...f, ano: somenteDigitos(e.target.value) }))} erro={erros.ano} />
            <Campo
              rotulo="Placa"
              mono
              maxLength={7}
              value={form.placa}
              onChange={(e) => setForm((f) => ({ ...f, placa: e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '') }))}
              erro={erros.placa}
              dica="Padrão Mercosul: ABC1D23"
            />
          </div>
          <Campo
            rotulo="Valor da diária (R$)"
            mono
            inputMode="decimal"
            value={form.diaria}
            onChange={(e) => setForm((f) => ({ ...f, diaria: mascaraMoeda(e.target.value) }))}
            erro={erros.valorDiaria}
            dica={automovel ? `Hoje: ${formatarMoeda(automovel.valorDiaria)}. A nova diária vale para os próximos pedidos.` : undefined}
          />
          <div className="campo">
            <span className="rotulo">Proprietário</span>
            <div>
              <DonoPill tipo={automovel?.proprietario} />
            </div>
            <div className="ajuda">O proprietário é definido quando um contrato é executado.</div>
          </div>
        </div>

        <div className="gaveta-rodape">
          <Botao variante="secondary" onClick={aoFechar} disabled={salvando}>
            Cancelar
          </Botao>
          <Botao type="submit" icone="check" carregando={salvando}>
            Salvar automóvel
          </Botao>
        </div>
      </form>
    </>
  )
}
