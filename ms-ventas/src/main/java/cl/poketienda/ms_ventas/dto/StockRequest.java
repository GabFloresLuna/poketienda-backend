package cl.poketienda.ms_ventas.dto;

import java.util.List;

// Cuerpo que ms-ventas envía a ms-productos para descontar o reponer stock
public record StockRequest(List<Item> items) {

    public record Item(Long productoId, Integer cantidad) {
    }
}
