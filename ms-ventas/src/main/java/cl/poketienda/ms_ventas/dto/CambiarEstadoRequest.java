package cl.poketienda.ms_ventas.dto;

import cl.poketienda.ms_ventas.model.EstadoBoleta;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoBoleta estado
) {
}
