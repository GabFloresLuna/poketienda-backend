package cl.poketienda.ms_ventas.dto;

import cl.poketienda.ms_ventas.model.Boleta;
import cl.poketienda.ms_ventas.model.EstadoBoleta;
import java.time.LocalDateTime;
import java.util.List;

public record BoletaResponse(
        Long id,
        LocalDateTime fecha,
        EstadoBoleta estado,
        Integer total,
        Long usuarioId,
        String nombre,
        String apellidos,
        String correo,
        String calle,
        String departamento,
        String region,
        String comuna,
        String indicaciones,
        List<DetalleBoletaResponse> detalles
) {

    public static BoletaResponse desde(Boleta boleta) {
        return new BoletaResponse(
                boleta.getId(),
                boleta.getFecha(),
                boleta.getEstado(),
                boleta.getTotal(),
                boleta.getUsuarioId(),
                boleta.getNombre(),
                boleta.getApellidos(),
                boleta.getCorreo(),
                boleta.getCalle(),
                boleta.getDepartamento(),
                boleta.getRegion(),
                boleta.getComuna(),
                boleta.getIndicaciones(),
                boleta.getDetalles().stream()
                        .map(DetalleBoletaResponse::desde)
                        .toList()
        );
    }
}
