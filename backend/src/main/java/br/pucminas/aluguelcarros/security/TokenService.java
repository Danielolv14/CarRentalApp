package br.pucminas.aluguelcarros.security;

import br.pucminas.aluguelcarros.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/** Emite o JWT da sessão. O "sub" é o login; as claims levam o id, o perfil e os papéis. */
@Service
public class TokenService {

    public static final String CLAIM_PAPEIS = "roles";

    private final JwtEncoder encoder;
    private final Duration validade;

    public TokenService(JwtEncoder encoder, @Value("${app.jwt.validade-horas:8}") long validadeHoras) {
        this.encoder = encoder;
        this.validade = Duration.ofHours(validadeHoras);
    }

    public TokenGerado emitir(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(validade);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("aluguel-carros")
                .subject(usuario.getLogin())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .claim("uid", usuario.getId())
                .claim("perfil", usuario.getPerfil().name())
                .claim(CLAIM_PAPEIS, Papeis.de(usuario))
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenGerado(token, expiraEm);
    }

    public record TokenGerado(String token, Instant expiraEm) {
    }
}
