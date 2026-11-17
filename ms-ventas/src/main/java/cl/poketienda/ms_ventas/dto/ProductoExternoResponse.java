package cl.poketienda.ms_ventas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoExternoResponse(
        Long id,
        String nombre,
        Integer precio,
        Integer precioOferta
) {
}
