package cl.poketienda.ms_ventas.controller;

import cl.poketienda.ms_ventas.dto.BoletaRequest;
import cl.poketienda.ms_ventas.dto.BoletaResponse;
import cl.poketienda.ms_ventas.dto.CambiarEstadoRequest;
import cl.poketienda.ms_ventas.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
@Tag(name = "Boletas", description = "Registro de compras y gestión de boletas")
public class BoletaController {

    private final BoletaService boletaService;

    @PostMapping
    @Operation(summary = "Registrar una compra (público). Si hay sesión, se asocia al usuario")
    public ResponseEntity<BoletaResponse> crear(@Valid @RequestBody BoletaRequest request) {
        // TODO seguridad: si llega un token válido, pasar aquí el id del usuario (hoy siempre nulo)
        BoletaResponse boleta = boletaService.crear(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(boleta);
    }

    @GetMapping
    @Operation(summary = "Listar boletas (ADMIN, VENDEDOR)")
    public List<BoletaResponse> listar() {
        return boletaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver una boleta con su detalle (ADMIN, VENDEDOR)")
    public BoletaResponse obtener(@PathVariable Long id) {
        return boletaService.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de una boleta; al anular se repone el stock (ADMIN)")
    public BoletaResponse cambiarEstado(@PathVariable Long id,
                                        @Valid @RequestBody CambiarEstadoRequest request) {
        return boletaService.cambiarEstado(id, request.estado());
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Historial de compras de un usuario (ADMIN)")
    public List<BoletaResponse> porUsuario(@PathVariable Long usuarioId) {
        return boletaService.porUsuario(usuarioId);
    }
}
