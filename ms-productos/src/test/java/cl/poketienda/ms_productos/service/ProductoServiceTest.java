package cl.poketienda.ms_productos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.poketienda.ms_productos.dto.ProductoRequest;
import cl.poketienda.ms_productos.dto.StockRequest;
import cl.poketienda.ms_productos.exception.ReglaNegocioException;
import cl.poketienda.ms_productos.model.Producto;
import cl.poketienda.ms_productos.repository.CategoriaRepository;
import cl.poketienda.ms_productos.repository.ProductoRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void noCreaUnProductoConCodigoRepetido() {
        when(productoRepository.existsByCodigoIgnoreCase("P001")).thenReturn(true);

        // El código se normaliza (trim y mayúsculas) antes de buscar repetidos
        assertThatThrownBy(() -> productoService.crear(request(" p001 ", 10000, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("P001");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void noCreaUnProductoConOfertaMayorOIgualAlPrecio() {
        assertThatThrownBy(() -> productoService.crear(request("P009", 10000, 12000)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("precio de oferta");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void descontarStockEsTodoONada() {
        Producto conStock = producto(1L, 5);
        Producto sinStock = producto(2L, 1);
        when(productoRepository.findAllById(anyIterable())).thenReturn(List.of(conStock, sinStock));

        StockRequest pedido = new StockRequest(List.of(
                new StockRequest.Item(1L, 2),
                new StockRequest.Item(2L, 3)));

        assertThatThrownBy(() -> productoService.descontarStock(pedido))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente");

        // Ningún producto se descuenta, ni siquiera el que sí tenía stock
        assertThat(conStock.getStock()).isEqualTo(5);
        assertThat(sinStock.getStock()).isEqualTo(1);
        verify(productoRepository, never()).saveAll(any());
    }

    @Test
    void descontarStockBajaElStockCuandoAlcanza() {
        Producto producto = producto(1L, 10);
        when(productoRepository.findAllById(anyIterable())).thenReturn(List.of(producto));

        productoService.descontarStock(new StockRequest(List.of(new StockRequest.Item(1L, 4))));

        assertThat(producto.getStock()).isEqualTo(6);
        verify(productoRepository).saveAll(List.of(producto));
    }

    private ProductoRequest request(String codigo, Integer precio, Integer precioOferta) {
        return new ProductoRequest(codigo, "Producto de prueba", null, null,
                precio, precioOferta, 10, 5, null, 1L);
    }

    private Producto producto(Long id, int stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Producto " + id);
        producto.setStock(stock);
        return producto;
    }
}
