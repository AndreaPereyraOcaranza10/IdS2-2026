-- Datos de prueba iniciales. Se ejecuta automaticamente al arrancar la app
-- (ver spring.sql.init.mode=always en application.properties).
-- Usa INSERT IGNORE para que sea seguro reiniciar la aplicacion varias veces
-- sin duplicar filas ni romper por las claves unicas (username, cuit, codigo).

-- =========================
-- USUARIOS
-- =========================
INSERT IGNORE INTO usuarios (username, password, nombre, rol, activo) VALUES
    ('admin', 'admin123', 'Administrador General', 'ADMIN', true),
    ('jperez', 'jperez123', 'Juan Perez', 'EMPLEADO', true);

-- =========================
-- PROVEEDORES
-- =========================
INSERT IGNORE INTO proveedores (razon_social, cuit, telefono, email, direccion, activo) VALUES
    ('Distribuidora TecnoMax S.A.', '30-71234567-8', '261-4551122', 'ventas@tecnomax.com.ar', 'Av. San Martin 1234, Mendoza', true),
    ('Componentes del Oeste SRL', '30-70987654-3', '261-4778899', 'contacto@compoeste.com.ar', 'Ruta 40 Km 12, Guaymallen', true),
    ('Importadora ElectroSur', '30-69876543-1', '261-4223344', 'info@electrosur.com.ar', 'Belgrano 567, Godoy Cruz', true);

-- =========================
-- PRODUCTOS
-- Nota: stock_actual normalmente solo deberia entrar via una orden de compra
-- confirmada (ver ServicioOrdenCompra), pero como datos de PRUEBA se cargan
-- algunos valores directamente por SQL, incluyendo un par por debajo del
-- stock_minimo a proposito, para que el dashboard de Inicio tenga contenido
-- real para mostrar (tarjeta "Stock Bajo" y su listado) desde el primer arranque.
-- =========================
INSERT IGNORE INTO productos (codigo, nombre, descripcion, categoria, precio_compra, precio_venta, stock_actual, stock_minimo, activo) VALUES
    ('PRD-001', 'Mouse Inalambrico USB', 'Mouse optico inalambrico 2.4GHz, 1600 DPI', 'Perifericos', 4500.00, 8900.00, 3, 10, true),
    ('PRD-002', 'Teclado Mecanico RGB', 'Teclado mecanico switches rojos, retroiluminado', 'Perifericos', 18000.00, 32000.00, 15, 5, true),
    ('PRD-003', 'Monitor LED 24 pulgadas', 'Monitor Full HD 1920x1080, 75Hz', 'Monitores', 95000.00, 155000.00, 8, 4, true),
    ('PRD-004', 'SSD 480GB SATA III', 'Disco solido 480GB, lectura 550MB/s', 'Almacenamiento', 22000.00, 38000.00, 2, 8, true),
    ('PRD-005', 'Memoria RAM 8GB DDR4', 'Modulo de memoria 8GB 3200MHz', 'Componentes', 15000.00, 26000.00, 20, 10, true),
    ('PRD-006', 'Webcam Full HD 1080p', 'Camara web con microfono integrado', 'Perifericos', 12000.00, 21000.00, 12, 6, true);
