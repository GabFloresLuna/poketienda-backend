# PokéTienda Pearl · Backend

Backend de la tienda online PokéTienda Pearl para la Evaluación Parcial 3 de DSY1104 Desarrollo Fullstack II (Duoc UC). Son tres microservicios Spring Boot, cada uno con su propia base de datos MySQL, que exponen una API REST protegida con JWT y documentada con Swagger.

Frontend (React): https://github.com/GabFloresLuna/poketienda-frontend

## Arquitectura

| Microservicio | Responsabilidad | Puerto | Base de datos | Tablas |
|---|---|---|---|---|
| ms-usuarios | Registro, login, tokens JWT, usuarios y perfil | 8081 | `poketienda_usuarios` | usuario |
| ms-productos | Categorías, productos, ofertas y stock | 8082 | `poketienda_productos` | categoria, producto |
| ms-ventas | Compras, boletas y reportes | 8083 | `poketienda_ventas` | boleta, detalle_boleta |

Ningún microservicio lee las tablas de otro. ms-ventas consulta precios y descuenta stock llamando a la API de ms-productos (su dirección se define en `MS_PRODUCTOS_URL`). Los tres validan el token JWT con la misma clave secreta.

## Estructura de directorios

```
poketienda-backend/
├── Readme.md
├── docker-compose.yml     # MySQL + los tres microservicios
├── .env.example           # plantilla de variables de entorno
├── docker/mysql/init.sql  # crea las tres bases de datos
├── docs/
├── ms-usuarios/
├── ms-productos/
└── ms-ventas/
```

Cada microservicio tiene su propio `Dockerfile`, `pom.xml` y `mvnw`, y usa el paquete base `cl.poketienda.ms_<nombre>` (`ms_usuarios`, `ms_productos` o `ms_ventas`) con los subpaquetes `config`, `controller`, `dto`, `exception`, `model`, `repository`, `security` y `service`. Además, ms-usuarios tiene `validation` y ms-ventas tiene `client` (llamadas a ms-productos).

## Requisitos

- Git
- Docker Desktop (para ejecutar con Docker) **o** JDK 21 y MySQL 8 (para ejecutar sin Docker)
- Postman o Insomnia (para probar la API)
- Node.js (solo para ejecutar el frontend)

Maven no se instala aparte: cada microservicio trae su propio `mvnw`.

## Puesta en marcha

### 1. Clonar el repositorio

```bash
git clone https://github.com/GabFloresLuna/poketienda-backend.git
cd poketienda-backend
```

### 2. Variables de entorno

La contraseña de MySQL y la clave del JWT no se guardan en el repositorio. Los microservicios las leen de estas variables:

| Variable | Contenido |
|---|---|
| `DB_USER` | Usuario de MySQL (con Docker debe ser `root`) |
| `DB_PASSWORD` | Contraseña de MySQL |
| `JWT_SECRET` | Clave secreta del JWT (la misma en los tres microservicios, mínimo 32 caracteres) |
| `MS_PRODUCTOS_URL` | Solo ms-ventas. Dirección de ms-productos. Sin Docker ya viene configurada (`http://localhost:8082/api/v1`); Docker Compose la define como `http://ms-productos:8082/api/v1` |

### Opción A: con Docker (recomendada)

Levanta MySQL y los tres microservicios con un solo comando. No hace falta instalar JDK ni MySQL.

1. Copiar la plantilla y completar los valores:

   ```bash
   cp .env.example .env
   ```

   En Windows (PowerShell): `Copy-Item .env.example .env`

2. Construir y arrancar:

   ```bash
   docker compose up --build
   ```

   Las tres bases de datos se crean solas con `docker/mysql/init.sql`. ms-productos arranca antes que ms-ventas.

3. Para detener: `docker compose down`. Los datos de MySQL se conservan en un volumen; para borrarlos, usar `docker compose down -v`.

Dentro de Docker, MySQL queda disponible en tu PC en `localhost:3307` (el 3306 se deja libre por si ya tienes MySQL instalado).

### Opción B: sin Docker

1. Crear las bases de datos en MySQL Workbench (las tablas las crea JPA al iniciar cada microservicio):

   ```sql
   CREATE DATABASE poketienda_usuarios  CHARACTER SET utf8mb4;
   CREATE DATABASE poketienda_productos CHARACTER SET utf8mb4;
   CREATE DATABASE poketienda_ventas    CHARACTER SET utf8mb4;
   ```

2. Definir las variables en la terminal. Valen solo para esa terminal, así que hay que repetirlo en cada una antes de ejecutar un microservicio.

   Windows (PowerShell):

   ```powershell
   $env:DB_USER="root"
   $env:DB_PASSWORD="tu_contraseña"
   $env:JWT_SECRET="una_clave_larga_de_al_menos_32_caracteres"
   ```

   macOS o Linux:

   ```bash
   export DB_USER=root
   export DB_PASSWORD=tu_contraseña
   export JWT_SECRET=una_clave_larga_de_al_menos_32_caracteres
   ```

3. Ejecutar cada microservicio en su propia terminal. ms-productos debe estar arriba antes de registrar compras en ms-ventas.

   Windows:

   ```powershell
   cd ms-productos
   .\mvnw.cmd spring-boot:run
   ```

   macOS o Linux:

   ```bash
   cd ms-productos
   ./mvnw spring-boot:run
   ```

   Repetir con `ms-usuarios` y `ms-ventas`.

### 3. Verificar

| Microservicio | Swagger |
|---|---|
| ms-usuarios | http://localhost:8081/swagger-ui.html |
| ms-productos | http://localhost:8082/swagger-ui.html |
| ms-ventas | http://localhost:8083/swagger-ui.html |

Para probar un endpoint protegido: iniciar sesión en `POST /api/v1/auth/login` (ms-usuarios), copiar el access token y pegarlo en el botón **Authorize** de Swagger.

### Usuarios de prueba

Se crean solos al primer arranque de ms-usuarios.

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | admin@duoc.cl | Admin123 |
| VENDEDOR | vendedor@duoc.cl | Vendedor123 |
| CLIENTE | cliente@gmail.com | Cliente123 |

## API

Todas las rutas comienzan con `/api/v1`. El detalle de cada endpoint está en Swagger.

| Microservicio | Recursos |
|---|---|
| ms-usuarios | `/auth`, `/perfil`, `/usuarios` |
| ms-productos | `/categorias`, `/productos` |
| ms-ventas | `/boletas`, `/reportes` |

Roles: `ADMIN`, `VENDEDOR` y `CLIENTE`. Sin token o con token vencido la API responde 401; con un rol sin permiso responde 403.

### Autenticación

Los endpoints de ms-usuarios son:

- `POST /auth/registro`: crea una cuenta.
- `POST /auth/login`: devuelve un access token (dura 15 minutos) y un refresh token (dura 7 días).
- `POST /auth/refresh`: entrega un nuevo access token a partir del refresh token.

### Conexión con el frontend

El frontend usa estas URL base: `http://localhost:8081/api/v1` (usuarios), `http://localhost:8082/api/v1` (productos) y `http://localhost:8083/api/v1` (ventas). Los tres microservicios aceptan peticiones (CORS) desde `http://localhost:5173` (Vite) y `http://localhost:3000`.

## Pruebas

Hay 9 archivos de pruebas en total:

| Microservicio | Archivos |
|---|---|
| ms-usuarios | `MsUsuariosApplicationTests`, `AuthServiceTest`, `UsuarioServiceTest`, `ValidadorRunTest` |
| ms-productos | `MsProductosApplicationTests`, `CategoriaServiceTest`, `ProductoServiceTest` |
| ms-ventas | `MsVentasApplicationTests`, `BoletaServiceTest` |

Para ejecutar las pruebas de un microservicio:

```bash
cd ms-productos
./mvnw test
```

En Windows: `.\mvnw.cmd test`

## Trabajo en equipo

- Una rama por funcionalidad, por ejemplo `productos/crud-categorias`.
- Se une a `main` mediante Pull Request revisado por otro integrante.
- Los mensajes de commit dicen qué cambió, por ejemplo "valida stock antes de registrar la compra".
- No se suben contraseñas, claves ni la carpeta `target`.
