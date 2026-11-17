package cl.poketienda.ms_productos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Aquí solo se VALIDAN tokens. Los tokens de usuario los emite ms-usuarios.
@Service
public class JwtService {

    private static final Set<String> ROLES_DE_USUARIO = Set.of("ADMIN", "VENDEDOR", "CLIENTE");

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

    /**
     * Devuelve quién es el dueño del token, o null si el token no sirve para entrar
     * (por ejemplo, un refresh token).
     */
    public UsuarioToken aUsuarioToken(Claims claims) {
        String tipo = claims.get("tipo", String.class);

        // Token de otro microservicio (ms-ventas): el rol se fija aquí, no se lee del token.
        if ("servicio".equals(tipo)) {
            return new UsuarioToken(null, claims.getSubject(), "SERVICIO");
        }

        // Token de un usuario: solo roles de usuario, así un access token nunca actúa como SERVICIO.
        if ("access".equals(tipo)) {
            String rol = claims.get("rol", String.class);
            if (rol == null || !ROLES_DE_USUARIO.contains(rol)) {
                return null;
            }
            Long id = claims.get("id") instanceof Number numero ? numero.longValue() : null;
            return new UsuarioToken(id, claims.getSubject(), rol);
        }
        return null;
    }
}
