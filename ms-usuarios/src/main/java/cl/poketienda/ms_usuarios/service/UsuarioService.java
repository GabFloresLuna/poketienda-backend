package cl.poketienda.ms_usuarios.service;

import cl.poketienda.ms_usuarios.dto.PerfilRequest;
import cl.poketienda.ms_usuarios.dto.UsuarioCrearRequest;
import cl.poketienda.ms_usuarios.dto.UsuarioEditarRequest;
import cl.poketienda.ms_usuarios.dto.UsuarioResponse;
import cl.poketienda.ms_usuarios.exception.RecursoNoEncontradoException;
import cl.poketienda.ms_usuarios.exception.ReglaNegocioException;
import cl.poketienda.ms_usuarios.model.Usuario;
import cl.poketienda.ms_usuarios.repository.UsuarioRepository;
import cl.poketienda.ms_usuarios.validation.ValidadorRun;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        // El RUN ya viene validado por @RunValido, aquí solo se normaliza al formato 12345678-5.
        String run = ValidadorRun.normalizar(request.run());
        String correo = normalizarCorreo(request.correo());

        if (usuarioRepository.existsByRun(run)) {
            throw new ReglaNegocioException("Ya existe un usuario con el RUN " + run);
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + correo);
        }

        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setDireccion(request.direccion());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse editar(Long id, UsuarioEditarRequest request) {
        Usuario usuario = buscar(id);
        String run = ValidadorRun.normalizar(request.run());
        String correo = normalizarCorreo(request.correo());

        if (usuarioRepository.existsByRunAndIdNot(run, id)) {
            throw new ReglaNegocioException("Ya existe un usuario con el RUN " + run);
        }
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + correo);
        }

        usuario.setRun(run);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        // Contraseña vacía o nula = no se cambia.
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        usuario.setRol(request.rol());
        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setDireccion(request.direccion());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        return UsuarioResponse.desde(usuario);
    }

    @Transactional
    public void eliminar(Long id, Long idSolicitante) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(idSolicitante)) {
            throw new ReglaNegocioException("Un administrador no puede eliminarse a sí mismo");
        }
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Long id, PerfilRequest request) {
        Usuario usuario = buscar(id);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setDireccion(request.direccion());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        return UsuarioResponse.desde(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }
}
