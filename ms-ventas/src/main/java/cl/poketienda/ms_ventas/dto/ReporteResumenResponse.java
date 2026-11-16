package cl.poketienda.ms_ventas.dto;

import java.util.List;

public record ReporteResumenResponse(
        Integer totalVendido,
        Integer cantidadBoletas,
        Integer ticketPromedio,
        List<ProductoMasVendido> masVendidos
) {

    public record ProductoMasVendido(
            Long productoId,
            String nombreProducto,
            Integer cantidadVendida
    ) {
    }
}
