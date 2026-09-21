/*
 * tablas.js - Activa buscador, paginación y orden en toda tabla con la clase "tabla-datos",
 * usando la librería simple-datatables (local en /recursos/vendor/simple-datatables). Textos en español.
 */
window.addEventListener('DOMContentLoaded', function () {
    if (typeof simpleDatatables === 'undefined') { return; }
    document.querySelectorAll('table.tabla-datos').forEach(function (tabla) {
        new simpleDatatables.DataTable(tabla, {
            perPage: 10,
            perPageSelect: [5, 10, 25, 50],
            labels: {
                placeholder: 'Buscar...',
                searchTitle: 'Buscar en la tabla',
                perPage: 'registros por página',
                noRows: 'No hay registros para mostrar',
                noResults: 'Ningún registro coincide con la búsqueda',
                info: 'Mostrando {start} a {end} de {rows} registros'
            }
        });
    });
});
