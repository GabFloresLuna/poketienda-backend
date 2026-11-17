package cl.poketienda.ms_ventas.service;

import cl.poketienda.ms_ventas.dto.ReporteResumenResponse;
import cl.poketienda.ms_ventas.dto.ReporteResumenResponse.ProductoMasVendido;
import cl.poketienda.ms_ventas.model.Boleta;
import cl.poketienda.ms_ventas.model.DetalleBoleta;
import cl.poketienda.ms_ventas.model.EstadoBoleta;
import cl.poketienda.ms_ventas.repository.BoletaRepository;
import cl.poketienda.ms_ventas.repository.DetalleBoletaRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private static final int CANTIDAD_MAS_VENDIDOS = 5;

    private final BoletaRepository boletaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    public ReporteResumenResponse resumen() {
        // Las boletas anuladas no cuentan en los reportes
        List<Boleta> boletas = boletaRepository.findByEstadoNot(EstadoBoleta.ANULADA);

        int totalVendido = boletas.stream().mapToInt(Boleta::getTotal).sum();
        int cantidadBoletas = boletas.size();
        int ticketPromedio = cantidadBoletas == 0
                ? 0
                : (int) Math.round((double) totalVendido / cantidadBoletas);

        return new ReporteResumenResponse(
                totalVendido,
                cantidadBoletas,
                ticketPromedio,
                masVendidos());
    }

    private List<ProductoMasVendido> masVendidos() {
        return detalleBoletaRepository.findByBoletaEstadoNot(EstadoBoleta.ANULADA).stream()
                .collect(Collectors.groupingBy(DetalleBoleta::getProductoId))
                .values().stream()
                .map(grupo -> new ProductoMasVendido(
                        grupo.get(0).getProductoId(),
                        grupo.get(0).getNombreProducto(),
                        grupo.stream().mapToInt(DetalleBoleta::getCantidad).sum()))
                .sorted(Comparator.comparing(ProductoMasVendido::cantidadVendida).reversed())
                .limit(CANTIDAD_MAS_VENDIDOS)
                .toList();
    }
}
