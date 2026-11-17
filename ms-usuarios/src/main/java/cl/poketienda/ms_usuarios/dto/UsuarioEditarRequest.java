package cl.poketienda.ms_usuarios.dto;

import cl.poketienda.ms_usuarios.model.Rol;
import cl.poketienda.ms_usuarios.validation.RunValido;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Usado por el ADMIN para editar. La contraseña es opcional: vacía o nula = no se cambia.
public record UsuarioEditarRequest(

        @NotBlank(message = "El RUN es obligatorio")
        @Size(max = 12, message = "El RUN no puede superar los 12 caracteres")
        @RunValido
        String run,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 80, message = "Los apellidos no pueden superar los 80 caracteres")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        String correo,

        @Pattern(regexp = "^(.{6,72})?$", message = "La contraseña debe tener entre 6 y 72 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @Size(max = 60, message = "La región no puede superar los 60 caracteres")
        String region,

        @Size(max = 60, message = "La comuna no puede superar los 60 caracteres")
        String comuna,

        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        String direccion,

        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento
) {
}
