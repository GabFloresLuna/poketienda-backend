package cl.poketienda.ms_ventas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.poketienda.ms_ventas.client.ProductosCliente;
import cl.poketienda.ms_ventas.dto.BoletaRequest;
import cl.poketienda.ms_ventas.dto.BoletaResponse;
import cl.poketienda.ms_ventas.dto.ProductoExternoResponse;
import cl.poketienda.ms_ventas.dto.StockRequest;
import cl.poketienda.ms_ventas.exception.ReglaNegocioException;
import cl.poketienda.ms_ventas.model.Boleta;
import cl.poketienda.ms_ventas.model.DetalleBoleta;
import cl.poketienda.ms_ventas.model.EstadoBoleta;
import cl.poketienda.ms_ventas.repository.BoletaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private ProductosCliente productosCliente;

    @InjectMocks
    private BoletaService boletaService;

    @Test
    void laCompraCalculaElTotalUsandoElPrecioDeOferta() {
        // Producto 1: oferta de 7.000 (normal 10.000). Producto 2: sin oferta, 5.000.
        when(productosCliente.obtenerProducto(1L))
                .thenReturn(new ProductoExternoResponse(1L, "Set de Cartas", 10000, 7000));
        when(productosCliente.obtenerProducto(2L))
                .thenReturn(new ProductoExternoResponse(2L, "Estuche", 5000, null));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse boleta = boletaService.crear(
                request(new BoletaRequest.Item(1L, 2), new BoletaRequest.Item(2L, 1)), null);

        // 2 x 7.000 + 1 x 5.000
        assertThat(boleta.total()).isEqualTo(19000);
        assertThat(boleta.estado()).isEqualTo(EstadoBoleta.PAGADA);
        assertThat(boleta.detalles()).hasSize(2);
        assertThat(boleta.detalles().get(0).precioUnitario()).isEqualTo(7000);
        assertThat(boleta.detalles().get(0).subtotal()).isEqualTo(14000);
        verify(productosCliente).descontarStock(List.of(
                new StockRequest.Item(1L, 2),
                new StockRequest.Item(2L, 1)));
    }

    @Test
    void laCompraSinStockSuficienteNoCreaLaBoleta() {
        when(productosCliente.obtenerProducto(1L))
                .thenReturn(new ProductoExternoResponse(1L, "Consola", 89990, null));
        doThrow(new ReglaNegocioException("Stock insuficiente para Consola"))
                .when(productosCliente).descontarStock(anyList());

        assertThatThrownBy(() -> boletaService.crear(request(new BoletaRequest.Item(1L, 5)), null))
                .isInstanceOf(ReglaNegocioException.class);

        verify(boletaRepository, never()).save(any());
    }

    @Test
    void siFallaElGuardadoSeReponeElStock() {
        when(productosCliente.obtenerProducto(1L))
                .thenReturn(new ProductoExternoResponse(1L, "Consola", 89990, null));
        when(boletaRepository.save(any(Boleta.class))).thenThrow(new RuntimeException("falla de la base"));

        assertThatThrownBy(() -> boletaService.crear(request(new BoletaRequest.Item(1L, 1)), null))
                .isInstanceOf(RuntimeException.class);

        // Compensación: lo que se descontó se devuelve
        verify(productosCliente).reponerStock(List.of(new StockRequest.Item(1L, 1)));
    }

    @Test
    void anularUnaBoletaReponeElStock() {
        Boleta boleta = boletaConDetalle(EstadoBoleta.PAGADA);
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse resultado = boletaService.cambiarEstado(1L, EstadoBoleta.ANULADA);

        assertThat(resultado.estado()).isEqualTo(EstadoBoleta.ANULADA);
        verify(productosCliente).reponerStock(List.of(new StockRequest.Item(7L, 3)));
    }

    @Test
    void noPermiteUnCambioDeEstadoFueraDelFlujo() {
        Boleta boleta = boletaConDetalle(EstadoBoleta.ENTREGADA);
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));

        assertThatThrownBy(() -> boletaService.cambiarEstado(1L, EstadoBoleta.ANULADA))
                .isInstanceOf(ReglaNegocioException.class);

        verify(productosCliente, never()).reponerStock(anyList());
        verify(boletaRepository, never()).save(any());
    }

    private BoletaRequest request(BoletaRequest.Item... items) {
        return new BoletaRequest("Ash", "Ketchum", "ash@correo.cl", "Calle Falsa 123",
                null, "Metropolitana de Santiago", "Santiago", null, List.of(items));
    }

    private Boleta boletaConDetalle(EstadoBoleta estado) {
        Boleta boleta = new Boleta();
        boleta.setId(1L);
        boleta.setEstado(estado);
        DetalleBoleta detalle = new DetalleBoleta();
        detalle.setProductoId(7L);
        detalle.setNombreProducto("Lámpara");
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(1000);
        detalle.setSubtotal(3000);
        boleta.agregarDetalle(detalle);
        return boleta;
    }
}
