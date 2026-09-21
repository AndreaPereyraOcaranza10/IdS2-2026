# Inventario Tech

Sistema de gestion de inventario: productos, proveedores y ordenes de compra
que impactan el stock al confirmarse.

## Stack
- Spring Boot 3.3.4 (Java 17)
- MVC + Thymeleaf
- Spring Data JPA / Hibernate
- MySQL (via XAMPP)
- DTOs estrictos entre Controller y Service (ninguna entidad llega a la vista)
- Plantilla visual: InApp Inventory Dashboard (Bootstrap 5.3.8), ya compilada en
  `src/main/resources/static/assets`

## Como levantarlo

1. Abrir XAMPP e iniciar el modulo **MySQL** (no hace falta Apache).
2. No es necesario crear la base a mano: `application.properties` tiene
   `createDatabaseIfNotExist=true`, asi que Hibernate crea `inventario_tech` sola
   la primera vez que arranca la aplicacion.
3. Si tu XAMPP usa usuario/password distintos a `root` / (vacio), ajustar en
   `src/main/resources/application.properties`:
   ```
   spring.datasource.username=root
   spring.datasource.password=
   ```
4. Levantar el proyecto:
   - Desde el IDE: correr `InventarioTechApplication.java`.
   - Desde la terminal: `./mvnw spring-boot:run` (o `mvn spring-boot:run` si tenes Maven instalado).
5. Abrir `http://localhost:8080/` — redirige al listado de Productos.

## Estado del proyecto

- Seguridad (login) **pospuesta**: todas las rutas estan abiertas por ahora.
  Ver comentarios `TODO` en `ServicioUsuario.java` y el bloque comentado en
  `pom.xml` para retomarla mas adelante.
- **Datos de prueba precargados** via `src/main/resources/data.sql` (se ejecuta
  solo al arrancar, es seguro reiniciar la app varias veces):
  - Usuarios: `admin` / `admin123` (rol ADMIN) y `jperez` / `jperez123` (rol EMPLEADO)
  - 3 proveedores y 6 productos (dos de ellos con stock por debajo del minimo,
    a proposito, para que el dashboard de Inicio tenga algo que mostrar desde
    el primer arranque)
  - No incluye ordenes de compra de ejemplo: crear una desde `/ordenes-compra/nueva`
    para probar el flujo completo (confirmar/anular e impacto en stock).
