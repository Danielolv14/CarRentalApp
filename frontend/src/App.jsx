import { Navigate, Route, Routes } from 'react-router-dom'
import RotaProtegida from './components/RotaProtegida.jsx'
import { areaDoUsuario, useAuth } from './context/AuthContext.jsx'
import Login from './pages/publico/Login.jsx'
import EscolhaPerfil from './pages/publico/EscolhaPerfil.jsx'
import CadastroCliente from './pages/publico/CadastroCliente.jsx'
import CadastroAgente from './pages/publico/CadastroAgente.jsx'
import InicioCliente from './pages/cliente/InicioCliente.jsx'
import PainelAgente from './pages/agente/PainelAgente.jsx'

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
        path="/agente"
        element={
          <RotaProtegida perfil="AGENTE">
            <PainelAgente />
          </RotaProtegida>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
