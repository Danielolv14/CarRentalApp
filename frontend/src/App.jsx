// Tela provisória. As telas de verdade seguem os wireframes em docs/wireframes
// e entram em pages/publico, pages/cliente e pages/agente.
export default function App() {
  return (
    <main className="provisoria">
      <div className="marca">
        <span className="marca-icone" aria-hidden="true">R</span>
        <span className="marca-nome">Rota</span>
      </div>
      <h1>Sistema de aluguel de carros</h1>
      <p>Front-end em construção. API em <code>{import.meta.env.VITE_API_URL ?? 'http://localhost:8080'}</code>.</p>
    </main>
  )
}
