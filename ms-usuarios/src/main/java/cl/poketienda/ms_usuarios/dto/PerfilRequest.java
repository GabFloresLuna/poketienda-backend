package cl.poketienda.ms_usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Lo que el propio usuario puede editar. RUN, correo y rol no se cambian desde el perfil.
public record PerfilRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 80, message = "Los apellidos no pueden superar los 80 caracteres")
        String apellidos,

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
