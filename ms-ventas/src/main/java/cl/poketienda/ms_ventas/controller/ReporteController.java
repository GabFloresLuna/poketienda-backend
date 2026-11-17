package cl.poketienda.ms_ventas.controller;

import cl.poketienda.ms_ventas.dto.ReporteResumenResponse;
import cl.poketienda.ms_ventas.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes", description = "Resumen de ventas")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/resumen")
    @Operation(summary = "Total vendido, cantidad de boletas, ticket promedio y más vendidos (ADMIN)")
    public ReporteResumenResponse resumen() {
        return reporteService.resumen();
    }
}
