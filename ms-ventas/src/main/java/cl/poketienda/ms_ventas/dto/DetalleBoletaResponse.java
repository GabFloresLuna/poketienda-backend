package cl.poketienda.ms_ventas.dto;

import cl.poketienda.ms_ventas.model.DetalleBoleta;

public record DetalleBoletaResponse(
        Long id,
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        Integer precioUnitario,
        Integer subtotal
) {

    public static DetalleBoletaResponse desde(DetalleBoleta detalle) {
        return new DetalleBoletaResponse(
                detalle.getId(),
                detalle.getProductoId(),
                detalle.getNombreProducto(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }
}
