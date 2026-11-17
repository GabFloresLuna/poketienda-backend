package cl.poketienda.ms_ventas.service;

import cl.poketienda.ms_ventas.client.ProductosCliente;
import cl.poketienda.ms_ventas.dto.BoletaRequest;
import cl.poketienda.ms_ventas.dto.BoletaResponse;
import cl.poketienda.ms_ventas.dto.ProductoExternoResponse;
import cl.poketienda.ms_ventas.dto.StockRequest;
import cl.poketienda.ms_ventas.exception.RecursoNoEncontradoException;
import cl.poketienda.ms_ventas.exception.ReglaNegocioException;
import cl.poketienda.ms_ventas.model.Boleta;
import cl.poketienda.ms_ventas.model.DetalleBoleta;
import cl.poketienda.ms_ventas.model.EstadoBoleta;
import cl.poketienda.ms_ventas.repository.BoletaRepository;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoletaService {

    // Cambios de estado permitidos. ENTREGADA y ANULADA son estados finales.
    private static final Map<EstadoBoleta, Set<EstadoBoleta>> TRANSICIONES = Map.of(
            EstadoBoleta.PAGADA, EnumSet.of(EstadoBoleta.ENVIADA, EstadoBoleta.ANULADA),
            EstadoBoleta.ENVIADA, EnumSet.of(EstadoBoleta.ENTREGADA, EstadoBoleta.ANULADA),
            EstadoBoleta.ENTREGADA, EnumSet.noneOf(EstadoBoleta.class),
            EstadoBoleta.ANULADA, EnumSet.noneOf(EstadoBoleta.class));

    private final BoletaRepository boletaRepository;
    private final ProductosCliente productosCliente;

    // Sin @Transactional a propósito: save() abre y cierra su propia transacción dentro del
    // try, así un fallo del guardado se captura aquí y se puede reponer el stock (compensación).
    public BoletaResponse crear(BoletaRequest request, Long usuarioId) {
        // Si el mismo producto viene repetido, se suman las cantidades
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (BoletaRequest.Item item : request.items()) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        // El precio nunca viene del frontend: se pide a ms-productos
        Boleta boleta = new Boleta();
        boleta.setFecha(LocalDateTime.now());
        boleta.setEstado(EstadoBoleta.PAGADA);
        boleta.setUsuarioId(usuarioId);
        boleta.setNombre(request.nombre());
        boleta.setApellidos(request.apellidos());
        boleta.setCorreo(request.correo());
        boleta.setCalle(request.calle());
        boleta.setDepartamento(request.departamento());
        boleta.setRegion(request.region());
        boleta.setComuna(request.comuna());
        boleta.setIndicaciones(request.indicaciones());

        int total = 0;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            ProductoExternoResponse producto = productosCliente.obtenerProducto(entrada.getKey());
            int precioUnitario = producto.precioOferta() != null
                    ? producto.precioOferta()
                    : producto.precio();
            int subtotal = precioUnitario * entrada.getValue();

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setProductoId(producto.id());
            detalle.setNombreProducto(producto.nombre());
            detalle.setCantidad(entrada.getValue());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            boleta.agregarDetalle(detalle);

            total += subtotal;
        }
        boleta.setTotal(total);

        // Todo o nada: si falta stock de algún producto, ms-productos responde 409 y no se guarda nada
        List<StockRequest.Item> itemsStock = cantidades.entrySet().stream()
                .map(e -> new StockRequest.Item(e.getKey(), e.getValue()))
                .toList();
        productosCliente.descontarStock(itemsStock);

        try {
            return BoletaResponse.desde(boletaRepository.save(boleta));
        } catch (RuntimeException ex) {
            log.error("Falló el guardado de la boleta; se repone el stock", ex);
            reponerSinFallar(itemsStock);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar() {
        return boletaRepository.findAllByOrderByFechaDesc().stream()
                .map(BoletaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse obtener(Long id) {
        return BoletaResponse.desde(buscar(id));
    }

    // Sirve para "mis compras" y para el historial de un usuario (ADMIN)
    @Transactional(readOnly = true)
    public List<BoletaResponse> porUsuario(Long usuarioId) {
        return boletaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(BoletaResponse::desde)
                .toList();
    }

    @Transactional
    public BoletaResponse cambiarEstado(Long id, EstadoBoleta nuevoEstado) {
        Boleta boleta = buscar(id);
        EstadoBoleta actual = boleta.getEstado();

        if (!TRANSICIONES.get(actual).contains(nuevoEstado)) {
            throw new ReglaNegocioException(
                    "No se puede cambiar una boleta de " + actual + " a " + nuevoEstado);
        }

        // Al anular, primero se repone el stock; si ms-productos falla, el estado no cambia
        if (nuevoEstado == EstadoBoleta.ANULADA) {
            List<StockRequest.Item> items = boleta.getDetalles().stream()
                    .map(d -> new StockRequest.Item(d.getProductoId(), d.getCantidad()))
                    .toList();
            productosCliente.reponerStock(items);
        }

        boleta.setEstado(nuevoEstado);
        return BoletaResponse.desde(boletaRepository.save(boleta));
    }

    private Boleta buscar(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la boleta con id " + id));
    }

    private void reponerSinFallar(List<StockRequest.Item> items) {
        try {
            productosCliente.reponerStock(items);
        } catch (RuntimeException ex) {
            log.error("No se pudo reponer el stock tras el fallo; revisar a mano: {}", items, ex);
        }
    }
}
