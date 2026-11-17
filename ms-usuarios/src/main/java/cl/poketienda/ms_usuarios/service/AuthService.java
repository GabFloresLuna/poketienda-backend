package cl.poketienda.ms_usuarios.service;

import cl.poketienda.ms_usuarios.dto.LoginRequest;
import cl.poketienda.ms_usuarios.dto.RefreshRequest;
import cl.poketienda.ms_usuarios.dto.RegistroRequest;
import cl.poketienda.ms_usuarios.dto.TokenResponse;
import cl.poketienda.ms_usuarios.dto.UsuarioCrearRequest;
import cl.poketienda.ms_usuarios.dto.UsuarioResponse;
import cl.poketienda.ms_usuarios.exception.CredencialesInvalidasException;
import cl.poketienda.ms_usuarios.exception.RecursoNoEncontradoException;
import cl.poketienda.ms_usuarios.model.Rol;
import cl.poketienda.ms_usuarios.model.Usuario;
import cl.poketienda.ms_usuarios.repository.UsuarioRepository;
import cl.poketienda.ms_usuarios.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    // El mismo mensaje para correo inexistente y contraseña incorrecta:
    // así no se revela qué correos tienen cuenta.
    private static final String CREDENCIALES_INCORRECTAS = "Correo o contraseña incorrectos";
    private static final String REFRESH_INVALIDO = "El refresh token no es válido o está vencido";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.correo().trim())
                .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INCORRECTAS));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INCORRECTAS);
        }
        return emitirTokens(usuario);
    }

    @Transactional
    public UsuarioResponse registro(RegistroRequest request) {
        // El rol se fija aquí, sin mirar la petición: el registro público siempre crea un CLIENTE.
        UsuarioCrearRequest crear = new UsuarioCrearRequest(
                request.run(), request.nombre(), request.apellidos(), request.correo(),
                request.password(), Rol.CLIENTE, request.region(), request.comuna(),
                request.direccion(), request.fechaNacimiento());
        return usuarioService.crear(crear);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshRequest request) {
        Claims claims;
        try {
            claims = jwtService.leer(request.refreshToken());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new CredencialesInvalidasException(REFRESH_INVALIDO);
        }
        // Un access token no sirve para renovar.
        if (!jwtService.esRefresh(claims)) {
            throw new CredencialesInvalidasException(REFRESH_INVALIDO);
        }
        // Se vuelve a leer el usuario: si lo eliminaron o le cambiaron el rol, el token nuevo lo refleja.
        Usuario usuario = usuarioRepository.findById(jwtService.aUsuarioToken(claims).id())
                .orElseThrow(() -> new CredencialesInvalidasException(REFRESH_INVALIDO));
        return emitirTokens(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + idUsuario));
    }

    private TokenResponse emitirTokens(Usuario usuario) {
        return TokenResponse.de(
                jwtService.generarAccessToken(usuario),
                jwtService.generarRefreshToken(usuario),
                jwtService.segundosDeVidaAccess(),
                UsuarioResponse.desde(usuario));
    }
}
