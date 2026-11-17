package cl.poketienda.ms_ventas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Valida los tokens de usuario (los emite ms-usuarios) y emite el token de servicio
// con el que ms-ventas se identifica ante los endpoints internos de ms-productos.
@Service
public class JwtService {

    private static final Set<String> ROLES_DE_USUARIO = Set.of("ADMIN", "VENDEDOR", "CLIENTE");
    private static final Duration DURACION_SERVICIO = Duration.ofMinutes(5);

    private final SecretKey clave;

    public JwtService(@Value("${jwt.secret}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    /** Valida la firma y la fecha de vencimiento. Lanza JwtException si algo falla. */
    public Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Devuelve el dueño del token, o null si no es un access token de usuario válido. */
    public UsuarioToken aUsuarioToken(Claims claims) {
        if (!"access".equals(claims.get("tipo", String.class))) {
            return null;
        }
        String rol = claims.get("rol", String.class);
        if (rol == null || !ROLES_DE_USUARIO.contains(rol)) {
            return null;
        }
        Long id = claims.get("id") instanceof Number numero ? numero.longValue() : null;
        return new UsuarioToken(id, claims.getSubject(), rol);
    }

    /** Token corto de ms-ventas para llamar a ms-productos. El rol SERVICIO lo fija ms-productos. */
    public String generarTokenServicio() {
        Date ahora = new Date();
        return Jwts.builder()
                .subject("ms-ventas")
                .claim("tipo", "servicio")
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + DURACION_SERVICIO.toMillis()))
                .signWith(clave)
                .compact();
    }
}
