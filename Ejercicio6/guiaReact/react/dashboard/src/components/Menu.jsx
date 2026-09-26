import logo from '../assets/img/logo.jpg';
import { Routes, Route, Link } from 'react-router-dom';
import Contenido from './Contenido';
import Preventa from './Preventa';
import Aspirantes from './Aspirantes';
import Profesiones from './Profesiones';
function Menu() {
    const profesiones = [
        {
            id: 1,
            nombre: 'Desarrollador web'
        },
        {
            id: 2,
            nombre: 'Econometrista'
        },
        {
            id: 3,
            nombre: 'Profesor'
        },
        {
            id: 4,
            nombre: 'Forense'
        },
        {
            id: 5,
            nombre: 'Diseñador'
        }
    ]
    return (
        <>
            {/* Esto es un comentario en JSX*/}
            <header className="menu-wrap">
                <figure className="user">
                    <div className="user-avatar">
                        <Link to="/" ><img className="w-100" src={logo} alt="ids2"/></Link> 
                    </div>
                    <figcaption>
                        Ing del Software 2
                    </figcaption>
                </figure>
                <nav>
                    <section className="menu">
                        <h3>Opciones</h3>
                        <ul>
                            <li>
                                <Link to="/empresa">
                                    <i className="bi bi-building" style={{fontSize: "1.2rem", color: "cornflowerblue"}}></i>
                                    - Empresas
                                </Link>
                            </li>
                            <li>
                                <Link to="/aspirantes">
                                    <i className="bi bi-person" style={{fontSize: "1.2rem", color: "cornflowerblue"}}></i>
                                    - Aspirantes
                                </Link>
                            </li>
                            <li>
                                <Link to="/profesiones">
                                    <i className="bi bi-list-check"></i>
                                    - Profesiones
                                </Link>
                            </li>
                            <li>
                                <Link to="#">
                                    <i className="bi bi-person-vcard" style={{fontSize: "1.2rem", color: "cornflowerblue"}}></i>
                                    - Postulate aquí
                                </Link>
                            </li>
                            <li>
                                <Link to="#">
                                    <i className="bi bi-chat-left-text"></i>
                                    - Contacto
                                </Link>
                            </li>
                        </ul>
                    </section>
                </nav>
            </header>
            {/*Creando nuestro enrutamiento*/}
            <Routes>
                <Route path='/' element={<Contenido/>} />  
                <Route path='/empresa' element={<Preventa/>}/>
                <Route path='/aspirantes' element={<Aspirantes/>} />
                <Route path='/profesiones' element={<Profesiones  listado={profesiones} />} />
            </Routes>
        </>
    )
}
export default Menu;