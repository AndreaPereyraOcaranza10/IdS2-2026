import imagen from '../assets/img/banner.jpg';
function Preventa() {
    return (
        <>
            <header className="preventa">

                <h2>Búsqueda y selección</h2>
                <p className="text-center h5">Encontramos talento para tu empresa, en todos los cargos administrativos, profesionales y técnicos.</p>
                <div>
                    <img src={imagen} alt="Recursos Humanos"/>
                </div>

                {/* <!--Fin Encabezado--> */}
            </header>
        </>
    );
}
export default Preventa;