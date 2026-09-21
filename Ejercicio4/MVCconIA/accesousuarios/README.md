# Acceso de Usuarios — ISW2

Sistema de **registro e ingreso de usuarios**: MVC + Thymeleaf + Spring Data JPA (ORM) + MySQL.

## Enunciado cubierto
- Las personas se registran con **Nombre, Apellido, Documento, Fecha de Nacimiento y Correo Personal** (+ clave).
- El **usuario del sistema es el correo personal**.
- Si al ingresar el usuario **no está registrado**, se lo redirige al registro (con el correo ya cargado).
- Si está registrado y **se equivoca 3 veces con la clave, se bloquea**.

## Requisitos
- JDK 21 · Maven · MySQL/MariaDB en `localhost:3306` (XAMPP, usuario `root` sin clave).
- Si tu MySQL tiene otra clave/puerto, editar `src/main/resources/application.properties`.
- La base `acceso_usuarios` y las tablas `personas` y `usuarios` se crean solas al arrancar.

## Ejecutar
```
mvn spring-boot:run
```
Abrir http://localhost:8080

## Estructura (`com.isw2.accesousuarios`)
| Capa | Paquete / archivo |
|---|---|
| Modelo (entidades JPA) | `entities/Persona`, `entities/Usuario` (1 a 1) |
| Repositorio | `repositories/RepositorioPersona`, `RepositorioUsuario` |
| Servicio (reglas de negocio) | `services/ServicioUsuario` |
| Controlador | `controllers/ControladorAutenticacion`, `ControladorRegistro` |
| DTOs y mapper | `dtos/*`, `mappers/MapeadorUsuario` |
| Excepción de negocio | `exceptions/ExcepcionAcceso` |
| Vista (Thymeleaf) | `templates/login`, `registro`, `inicio`, `fragments/layout` |

## Plantilla
Se usa **Waggy** (HTML/CSS + Bootstrap 5): su hoja de estilos, paleta, tipografías y fondo.
La licencia de Waggy exige conservar el crédito en el pie de página (ya incluido).
