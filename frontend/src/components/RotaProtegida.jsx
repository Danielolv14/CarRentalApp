import { Navigate, useLocation } from 'react-router-dom'
import { areaDoUsuario, useAuth } from '../context/AuthContext.jsx'

/** Só deixa passar quem está logado e tem o perfil certo. */
export default function RotaProtegida({ perfil, children }) {
  const { usuario } = useAuth()
  const local = useLocation()

  if (!usuario) return <Navigate to="/login" replace state={{ de: local.pathname }} />
  if (perfil && usuario.perfil !== perfil) return <Navigate to={areaDoUsuario(usuario)} replace />
  return children
}
