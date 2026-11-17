package cl.poketienda.ms_productos.controller;

import cl.poketienda.ms_productos.dto.ProductoRequest;
import cl.poketienda.ms_productos.dto.ProductoResponse;
import cl.poketienda.ms_productos.dto.StockRequest;
import cl.poketienda.ms_productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo, ofertas, inventario y stock")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Catálogo con filtros opcionales por categoría y texto (público)")
    public List<ProductoResponse> listar(@RequestParam(required = false) Long categoriaId,
                                         @RequestParam(required = false) String texto) {
        return productoService.listar(categoriaId, texto);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Productos con precio de oferta (público)")
    public List<ProductoResponse> listarOfertas() {
        return productoService.listarOfertas();
    }

    @GetMapping("/criticos")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Productos con stock igual o menor al stock crítico (ADMIN)")
    public List<ProductoResponse> listarCriticos() {
        return productoService.listarCriticos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto (público)")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear un producto (ADMIN)")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        return productoService.crear(request);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Editar un producto (ADMIN)")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar un producto (ADMIN)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }

    @PostMapping("/stock/descontar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Descontar stock de una compra, todo o nada (interno: solo ms-ventas)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void descontarStock(@Valid @RequestBody StockRequest request) {
        productoService.descontarStock(request);
    }

    @PostMapping("/stock/reponer")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Devolver stock al anular una boleta (interno: solo ms-ventas)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reponerStock(@Valid @RequestBody StockRequest request) {
        productoService.reponerStock(request);
    }
}
