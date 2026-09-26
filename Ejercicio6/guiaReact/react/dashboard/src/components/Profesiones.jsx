function Profesiones({ listado }) {
    // console.log(listado);

    return (
        <>
            <section className="content">
                <h2 className="mt-3">Profesiones</h2>
                <div className="list-group shadow-sm p-3 mb-5 rounded">
                    <h4 className="list-group-item list-group-item-action active text-center"
                        aria-current="true">
                        Listado de Profesiones
                    </h4>
                    {
                        listado.map((prof) => (
                            <button
                                type="button"
                                className="list-group-item list-group-item-action text-center"
                                key = {prof.id}>
                                {prof.nombre}    
                            </button>
                        ))
                    }

                </div>
            </section>
        </>
    );
}
export default Profesiones;