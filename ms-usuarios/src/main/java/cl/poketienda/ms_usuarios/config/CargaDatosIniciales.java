package cl.poketienda.ms_usuarios.config;

import cl.poketienda.ms_usuarios.model.Rol;
import cl.poketienda.ms_usuarios.model.Usuario;
import cl.poketienda.ms_usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Se ejecuta al arrancar y solo carga datos si la tabla está vacía.
// Contraseñas de prueba: Admin123, Vendedor123 y Cliente123 (se guardan encriptadas).
@Slf4j
@Component
@RequiredArgsConstructor
public class CargaDatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.save(crear("11111111-1", "Admin", "Pearl", "admin@duoc.cl", "Admin123", Rol.ADMIN));
        usuarioRepository.save(crear("12345678-5", "Vendedor", "Demo", "vendedor@duoc.cl", "Vendedor123", Rol.VENDEDOR));
        usuarioRepository.save(crear("22222222-2", "Cliente", "Demo", "cliente@gmail.com", "Cliente123", Rol.CLIENTE));
        log.info("Usuarios de prueba creados (ADMIN, VENDEDOR y CLIENTE)");
    }

    private Usuario crear(String run, String nombre, String apellidos, String correo, String clave, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(clave));
        usuario.setRol(rol);
        usuario.setRegion("Metropolitana de Santiago");
        usuario.setComuna("Santiago");
        usuario.setDireccion("Calle Falsa 123");
        return usuario;
    }
}
