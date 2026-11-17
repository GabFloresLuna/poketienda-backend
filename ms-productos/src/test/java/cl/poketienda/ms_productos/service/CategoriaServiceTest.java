package cl.poketienda.ms_productos.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.poketienda.ms_productos.exception.ReglaNegocioException;
import cl.poketienda.ms_productos.model.Categoria;
import cl.poketienda.ms_productos.repository.CategoriaRepository;
import cl.poketienda.ms_productos.repository.ProductoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void noEliminaUnaCategoriaQueTieneProductos() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new Categoria()));
        when(productoRepository.existsByCategoriaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.eliminar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("productos asociados");

        verify(categoriaRepository, never()).delete(any());
    }
}
