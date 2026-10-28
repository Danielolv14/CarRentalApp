package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.CadastroAgenteRequest;
import br.pucminas.aluguelcarros.dto.CadastroClienteRequest;
import br.pucminas.aluguelcarros.dto.TokenResponse;
import br.pucminas.aluguelcarros.exception.ConflitoException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Empresa;
import br.pucminas.aluguelcarros.model.Rendimento;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import br.pucminas.aluguelcarros.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** UC02 Cadastrar-se: cria a conta, guarda a senha com BCrypt e já devolve o token (entra direto). */
@Service
public class CadastroService {

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final AgenteRepository agentes;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public CadastroService(UsuarioRepository usuarios, ClienteRepository clientes, AgenteRepository agentes,
                           PasswordEncoder encoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.agentes = agentes;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @Transactional
    public TokenResponse cadastrarCliente(CadastroClienteRequest dados) {
        String login = normalizarLogin(dados.login());
        String cpf = somenteDigitos(dados.cpf());
        exigirLoginLivre(login);
        if (clientes.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe um cliente com este CPF.");
        }

        Cliente cliente = new Cliente(login, encoder.encode(dados.senha()), dados.nome().trim(), dados.rg().trim(), cpf,
                textoOuNulo(dados.profissao()), dados.endereco().paraModelo());
        if (dados.rendimentos() != null) {
            dados.rendimentos().forEach(r ->
                    cliente.adicionarRendimento(new Rendimento(r.entidadeEmpregadora().trim(), r.valor())));
        }
        return entrar(clientes.save(cliente));
    }

    @Transactional
    public TokenResponse cadastrarAgente(CadastroAgenteRequest dados) {
        String login = normalizarLogin(dados.login());
        String cnpj = somenteDigitos(dados.cnpj());
        exigirLoginLivre(login);
        if (agentes.existsByCnpj(cnpj)) {
            throw new ConflitoException("Já existe um agente com este CNPJ.");
        }

        String senha = encoder.encode(dados.senha());
        String razaoSocial = dados.razaoSocial().trim();
        Agente agente = switch (dados.tipo()) {
            case EMPRESA -> new Empresa(login, senha, razaoSocial, cnpj, razaoSocial);
            case BANCO -> new Banco(login, senha, razaoSocial, cnpj, razaoSocial);
        };
        return entrar(agentes.save(agente));
    }

    /** Depois de criar a conta, o usuário já sai logado. */
    private TokenResponse entrar(Usuario usuario) {
        TokenService.TokenGerado token = tokens.emitir(usuario);
        return TokenResponse.bearer(token.token(), token.expiraEm(), usuario);
    }

    private void exigirLoginLivre(String login) {
        if (usuarios.existsByLogin(login)) {
            throw new ConflitoException("Este usuário já está em uso.");
        }
    }

    static String normalizarLogin(String login) {
        return login == null ? null : login.trim().toLowerCase(Locale.ROOT);
    }

    private static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }

    static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
