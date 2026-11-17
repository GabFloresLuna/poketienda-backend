package cl.poketienda.ms_ventas.controller;

import cl.poketienda.ms_ventas.dto.BoletaRequest;
import cl.poketienda.ms_ventas.dto.BoletaResponse;
import cl.poketienda.ms_ventas.dto.CambiarEstadoRequest;
import cl.poketienda.ms_ventas.service.BoletaService;
import cl.poketienda.ms_ventas.security.UsuarioToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar una compra (público). Si hay sesión, se asocia al usuario")
    public ResponseEntity<BoletaResponse> crear(@Valid @RequestBody BoletaRequest request,
                                                @AuthenticationPrincipal UsuarioToken actual) {
        // Sin token (o token inválido) "actual" es nulo y la boleta queda sin usuario.
        Long usuarioId = actual != null ? actual.id() : null;
        BoletaResponse boleta = boletaService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(boleta);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar boletas (ADMIN, VENDEDOR)")
    public List<BoletaResponse> listar() {
        return boletaService.listar();
    }

    @GetMapping("/mias")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Compras del usuario con sesión iniciada")
    public List<BoletaResponse> mias(@AuthenticationPrincipal UsuarioToken actual) {
        return boletaService.porUsuario(actual.id());
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ver una boleta con su detalle (ADMIN, VENDEDOR)")
    public BoletaResponse obtener(@PathVariable Long id) {
        return boletaService.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar el estado de una boleta; al anular se repone el stock (ADMIN)")
    public BoletaResponse cambiarEstado(@PathVariable Long id,
                                        @Valid @RequestBody CambiarEstadoRequest request) {
        return boletaService.cambiarEstado(id, request.estado());
    }

    @GetMapping("/usuario/{usuarioId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Historial de compras de un usuario (ADMIN)")
    public List<BoletaResponse> porUsuario(@PathVariable Long usuarioId) {
        return boletaService.porUsuario(usuarioId);
    }
}
