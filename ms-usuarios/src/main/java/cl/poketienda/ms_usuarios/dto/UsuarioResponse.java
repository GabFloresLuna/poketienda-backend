package cl.poketienda.ms_usuarios.dto;

import cl.poketienda.ms_usuarios.model.Rol;
import cl.poketienda.ms_usuarios.model.Usuario;
import java.time.LocalDate;

// Nunca incluye la contraseña, ni siquiera encriptada.
public record UsuarioResponse(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        Rol rol,
        String region,
        String comuna,
        String direccion,
        LocalDate fechaNacimiento
) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getRun(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getRegion(),
                usuario.getComuna(),
                usuario.getDireccion(),
                usuario.getFechaNacimiento()
        );
    }
}
