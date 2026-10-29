import { Navigate, Route, Routes } from 'react-router-dom'
import RotaProtegida from './components/RotaProtegida.jsx'
import { areaDoUsuario, useAuth } from './context/AuthContext.jsx'
import Login from './pages/publico/Login.jsx'
import EscolhaPerfil from './pages/publico/EscolhaPerfil.jsx'
import CadastroCliente from './pages/publico/CadastroCliente.jsx'
import CadastroAgente from './pages/publico/CadastroAgente.jsx'
import InicioCliente from './pages/cliente/InicioCliente.jsx'
import MeuPerfil from './pages/cliente/MeuPerfil.jsx'
import NovoPedido from './pages/cliente/NovoPedido.jsx'
import MeusPedidos from './pages/cliente/MeusPedidos.jsx'
import DetalhePedido from './pages/cliente/DetalhePedido.jsx'
import PainelAgente from './pages/agente/PainelAgente.jsx'
import PedidosAgente from './pages/agente/PedidosAgente.jsx'
import AvaliarPedido from './pages/agente/AvaliarPedido.jsx'
import ExecutarContrato from './pages/agente/ExecutarContrato.jsx'
import Contratos from './pages/agente/Contratos.jsx'
import Automoveis from './pages/agente/Automoveis.jsx'

export default function App() {
  const { usuario } = useAuth()
  const inicio = usuario ? areaDoUsuario(usuario) : '/login'

  return (
    <Routes>
      <Route path="/" element={<Navigate to={inicio} replace />} />
      <Route path="/login" element={usuario ? <Navigate to={inicio} replace /> : <Login />} />
      <Route path="/cadastro" element={<EscolhaPerfil />} />
      <Route path="/cadastro/cliente" element={<CadastroCliente />} />
      <Route path="/cadastro/agente" element={<CadastroAgente />} />
      <Route
        path="/cliente"
        element={
          <RotaProtegida perfil="CLIENTE">
            <InicioCliente />
          </RotaProtegida>
        }
      />
      <Route
        path="/cliente/perfil"
        element={
          <RotaProtegida perfil="CLIENTE">
            <MeuPerfil />
          </RotaProtegida>
        }
      />
      <Route
        path="/cliente/pedidos"
        element={
          <RotaProtegida perfil="CLIENTE">
            <MeusPedidos />
          </RotaProtegida>
        }
      />
      <Route
        path="/cliente/pedidos/novo"
        element={
          <RotaProtegida perfil="CLIENTE">
            <NovoPedido />
          </RotaProtegida>
        }
      />
      <Route
        path="/cliente/pedidos/:id"
        element={
          <RotaProtegida perfil="CLIENTE">
            <DetalhePedido />
          </RotaProtegida>
        }
      />
      <Route
        path="/agente"
        element={
          <RotaProtegida perfil="AGENTE">
            <PainelAgente />
          </RotaProtegida>
        }
      />
      <Route
        path="/agente/pedidos"
        element={
          <RotaProtegida perfil="AGENTE">
            <PedidosAgente />
          </RotaProtegida>
        }
      />
      <Route
        path="/agente/pedidos/:id"
        element={
          <RotaProtegida perfil="AGENTE">
            <AvaliarPedido />
          </RotaProtegida>
        }
      />
      <Route
        path="/agente/pedidos/:id/contrato"
        element={
          <RotaProtegida perfil="AGENTE">
            <ExecutarContrato />
          </RotaProtegida>
        }
      />
      <Route
        path="/agente/contratos"
        element={
          <RotaProtegida perfil="AGENTE">
            <Contratos />
          </RotaProtegida>
        }
      />
      <Route
        path="/agente/automoveis"
        element={
          <RotaProtegida perfil="AGENTE" tipoAgente="EMPRESA">
            <Automoveis />
          </RotaProtegida>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
