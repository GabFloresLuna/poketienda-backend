package cl.poketienda.ms_productos.config;

import cl.poketienda.ms_productos.model.Categoria;
import cl.poketienda.ms_productos.model.Producto;
import cl.poketienda.ms_productos.repository.CategoriaRepository;
import cl.poketienda.ms_productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Se ejecuta al arrancar y solo carga datos si no hay categorías ni productos.
// Son las categorías y los 8 productos de la EP2 (db.js), para que la tienda se vea igual que antes.
@Slf4j
@Component
@RequiredArgsConstructor
public class CargaDatosIniciales implements CommandLineRunner {

    private static final String IMG = "/images/";
    // db.js no tenía stock crítico; se usa 5 para todos los productos de ejemplo.
    private static final int STOCK_CRITICO = 5;

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0 || productoRepository.count() > 0) {
            return;
        }

        Categoria figuras = categoriaRepository.save(categoria("Figuras"));
        Categoria decoracion = categoriaRepository.save(categoria("Decoración"));
        Categoria cartas = categoriaRepository.save(categoria("Cartas"));
        Categoria consolas = categoriaRepository.save(categoria("Consolas"));
        Categoria accesorios = categoriaRepository.save(categoria("Accesorios"));
        Categoria coleccion = categoriaRepository.save(categoria("Colección"));

        productoRepository.save(producto("P001", "Figura Palkia Legendaria",
                "Figura coleccionable de Palkia en posición de ataque.",
                24990, null, 8, "figuraPalkiaLegendaria1.webp", figuras));
        productoRepository.save(producto("P002", "Perla Cósmica Decorativa",
                "Perla decorativa con acabado nacarado y aura púrpura.",
                12490, null, 15, "perlaCosmicaDecorativa.jpg", decoracion));
        productoRepository.save(producto("P003", "Set de Cartas Sinnoh",
                "Set de cartas coleccionables de la región de Sinnoh.",
                8990, 6990, 40, "setCartasSinnoh.jpeg", cartas));
        productoRepository.save(producto("P004", "Consola Edición Perla",
                "Consola portátil edición Perla con detalles en magenta.",
                89990, null, 3, "consolaEdicionPerla.jpg", consolas));
        productoRepository.save(producto("P005", "Estuche Perla Cósmica",
                "Estuche rígido con impresión de Palkia en acabado nacarado.",
                18990, 14990, 12, "estuchePokemonPearl.jpeg", accesorios));
        productoRepository.save(producto("P006", "Álbum Tapa Dura Sinnoh",
                "Álbum de 360 bolsillos con arte de la Generación IV.",
                24990, null, 7, "albumColeccionistaSinnoh.webp", coleccion));
        productoRepository.save(producto("P007", "Lámpara Cósmica Giratina",
                "Lámpara cósmica con diseño de Giratina.",
                15990, null, 9, "lamparaCosmicaGiratina.webp", accesorios));
        productoRepository.save(producto("P008", "Figura Dialga Temporal",
                "Figura coleccionable de Dialga en posición de ataque.",
                24990, null, 8, "figuraPalkiaTemporal1.jpg", figuras));

        log.info("Categorías y productos de ejemplo creados");
    }

    private Categoria categoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoria;
    }

    private Producto producto(String codigo, String nombre, String descripcion, int precio,
                              Integer precioOferta, int stock, String imagen, Categoria categoria) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setPrecioOferta(precioOferta);
        producto.setStock(stock);
        producto.setStockCritico(STOCK_CRITICO);
        producto.setImagenUrl(IMG + imagen);
        producto.setCategoria(categoria);
        return producto;
    }
}
