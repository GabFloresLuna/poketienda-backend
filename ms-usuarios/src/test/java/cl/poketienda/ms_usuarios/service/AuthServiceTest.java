package cl.poketienda.ms_usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.poketienda.ms_usuarios.dto.LoginRequest;
import cl.poketienda.ms_usuarios.dto.RefreshRequest;
import cl.poketienda.ms_usuarios.dto.RegistroRequest;
import cl.poketienda.ms_usuarios.dto.TokenResponse;
import cl.poketienda.ms_usuarios.dto.UsuarioCrearRequest;
import cl.poketienda.ms_usuarios.exception.CredencialesInvalidasException;
import cl.poketienda.ms_usuarios.model.Rol;
import cl.poketienda.ms_usuarios.model.Usuario;
import cl.poketienda.ms_usuarios.repository.UsuarioRepository;
import cl.poketienda.ms_usuarios.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PasswordEncoder passwordEncoder;

    // JwtService real: así se prueban de verdad los tokens (firma, tipo y vencimiento)
    private final JwtService jwtService =
            new JwtService("clave-de-prueba-con-mas-de-32-caracteres-ok", 15, 7);

    private AuthService authService;

    @BeforeEach
    void preparar() {
        authService = new AuthService(usuarioRepository, usuarioService, passwordEncoder, jwtService);
    }

    @Test
    void elRegistroPublicoSiempreCreaUnCliente() {
        authService.registro(new RegistroRequest("11111111-1", "Ash", "Ketchum",
                "ash@correo.cl", "Secreta1", null, null, null, null));

        ArgumentCaptor<UsuarioCrearRequest> enviado = ArgumentCaptor.forClass(UsuarioCrearRequest.class);
        verify(usuarioService).crear(enviado.capture());
        assertThat(enviado.getValue().rol()).isEqualTo(Rol.CLIENTE);
    }

    @Test
    void elLoginDaElMismoMensajeSeaElCorreoOLaContrasenaLoIncorrecto() {
        when(usuarioRepository.findByCorreoIgnoreCase("noexiste@correo.cl")).thenReturn(Optional.empty());
        Usuario usuario = usuario();
        when(usuarioRepository.findByCorreoIgnoreCase("ash@correo.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "HASH")).thenReturn(false);

        String correoMalo = mensajeDeError(new LoginRequest("noexiste@correo.cl", "cualquiera"));
        String claveMala = mensajeDeError(new LoginRequest("ash@correo.cl", "mala"));

        assertThat(correoMalo).isEqualTo(claveMala);
    }

    @Test
    void unAccessTokenNoSirveParaRenovarLaSesion() {
        String accessToken = jwtService.generarAccessToken(usuario());

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(accessToken)))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void unRefreshTokenValidoEntregaUnParDeTokensNuevo() {
        Usuario usuario = usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        String refreshToken = jwtService.generarRefreshToken(usuario);

        TokenResponse respuesta = authService.refresh(new RefreshRequest(refreshToken));

        assertThat(jwtService.esAccess(jwtService.leer(respuesta.accessToken()))).isTrue();
        assertThat(jwtService.esRefresh(jwtService.leer(respuesta.refreshToken()))).isTrue();
    }

    private String mensajeDeError(LoginRequest request) {
        try {
            authService.login(request);
        } catch (CredencialesInvalidasException ex) {
            return ex.getMessage();
        }
        throw new AssertionError("Debía lanzar CredencialesInvalidasException");
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("ash@correo.cl");
        usuario.setPassword("HASH");
        usuario.setRol(Rol.CLIENTE);
        return usuario;
    }
}
