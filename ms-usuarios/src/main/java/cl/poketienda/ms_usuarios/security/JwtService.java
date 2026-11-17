package cl.poketienda.ms_usuarios.security;

import cl.poketienda.ms_usuarios.model.Rol;
import cl.poketienda.ms_usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_TIPO = "tipo";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey clave;
    private final Duration duracionAccess;
    private final Duration duracionRefresh;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.access-minutos}") long accessMinutos,
                      @Value("${jwt.refresh-dias}") long refreshDias) {
        // HS256 exige una clave de al menos 32 caracteres.
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionAccess = Duration.ofMinutes(accessMinutos);
        this.duracionRefresh = Duration.ofDays(refreshDias);
    }

    public String generarAccessToken(Usuario usuario) {
        return generar(usuario, ACCESS, duracionAccess);
    }

    public String generarRefreshToken(Usuario usuario) {
        return generar(usuario, REFRESH, duracionRefresh);
    }

    public long segundosDeVidaAccess() {
        return duracionAccess.toSeconds();
    }

    /** Valida la firma y la fecha de vencimiento. Lanza JwtException si algo falla. */
    public Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esAccess(Claims claims) {
        return ACCESS.equals(claims.get(CLAIM_TIPO, String.class));
    }

    public boolean esRefresh(Claims claims) {
        return REFRESH.equals(claims.get(CLAIM_TIPO, String.class));
    }

    public UsuarioToken aUsuarioToken(Claims claims) {
        Long id = ((Number) claims.get(CLAIM_ID)).longValue();
        Rol rol = Rol.valueOf(claims.get(CLAIM_ROL, String.class));
        return new UsuarioToken(id, claims.getSubject(), rol);
    }

    private String generar(Usuario usuario, String tipo, Duration duracion) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim(CLAIM_ID, usuario.getId())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_TIPO, tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion.toMillis()))
                .signWith(clave)
                .compact();
    }
}
