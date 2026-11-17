package cl.poketienda.ms_usuarios.controller;

import cl.poketienda.ms_usuarios.dto.UsuarioCrearRequest;
import cl.poketienda.ms_usuarios.dto.UsuarioEditarRequest;
import cl.poketienda.ms_usuarios.dto.UsuarioResponse;
import cl.poketienda.ms_usuarios.security.UsuarioToken;
import cl.poketienda.ms_usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión de usuarios (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios (ADMIN)")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un usuario (ADMIN)")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un usuario de cualquier rol (ADMIN)")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioCrearRequest request) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar un usuario; si la contraseña viene vacía no se cambia (ADMIN)")
    public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody UsuarioEditarRequest request) {
        return usuarioService.editar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un usuario; un ADMIN no puede eliminarse a sí mismo (ADMIN)")
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioToken actual) {
        usuarioService.eliminar(id, actual.id());
    }
}
