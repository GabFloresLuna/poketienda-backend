package cl.poketienda.ms_usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.poketienda.ms_usuarios.dto.UsuarioCrearRequest;
import cl.poketienda.ms_usuarios.dto.UsuarioEditarRequest;
import cl.poketienda.ms_usuarios.exception.ReglaNegocioException;
import cl.poketienda.ms_usuarios.model.Rol;
import cl.poketienda.ms_usuarios.model.Usuario;
import cl.poketienda.ms_usuarios.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void laContrasenaSeGuardaEncriptadaYElCorreoEnMinusculas() {
        when(passwordEncoder.encode("Secreta1")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.crear(new UsuarioCrearRequest("11.111.111-1", "Ash", "Ketchum",
                "  ASH@Correo.CL ", "Secreta1", Rol.CLIENTE, null, null, null, null));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getPassword()).isEqualTo("HASH").isNotEqualTo("Secreta1");
        assertThat(guardado.getValue().getCorreo()).isEqualTo("ash@correo.cl");
        assertThat(guardado.getValue().getRun()).isEqualTo("11111111-1");
    }

    @Test
    void noCreaUnUsuarioConRunRepetido() {
        when(usuarioRepository.existsByRun("11111111-1")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(new UsuarioCrearRequest("11111111-1", "Ash",
                "Ketchum", "ash@correo.cl", "Secreta1", Rol.CLIENTE, null, null, null, null)))
                .isInstanceOf(ReglaNegocioException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void editarConContrasenaVaciaNoCambiaLaContrasena() {
        Usuario existente = usuario(1L, "HASH_ANTERIOR");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        usuarioService.editar(1L, new UsuarioEditarRequest("11111111-1", "Ash", "Ketchum",
                "ash@correo.cl", "", Rol.CLIENTE, null, null, null, null));

        assertThat(existente.getPassword()).isEqualTo("HASH_ANTERIOR");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void unAdministradorNoPuedeEliminarseASiMismo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "HASH")));

        assertThatThrownBy(() -> usuarioService.eliminar(1L, 1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("a sí mismo");

        verify(usuarioRepository, never()).delete(any());
    }

    private Usuario usuario(Long id, String password) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRun("11111111-1");
        usuario.setCorreo("ash@correo.cl");
        usuario.setPassword(password);
        usuario.setRol(Rol.ADMIN);
        return usuario;
    }
}
