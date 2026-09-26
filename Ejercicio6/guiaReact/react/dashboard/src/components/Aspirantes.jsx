import foto1 from '../assets/img/foto1.jpg';
import foto2 from '../assets/img/foto2.jpg';
import foto3 from '../assets/img/foto3.jpg';
import foto4 from '../assets/img/foto4.jpg';
import foto5 from '../assets/img/foto5.jpg';
import foto6 from '../assets/img/foto6.jpg';

function Aspirantes() {
    return (
        <>
            <main className="content-wrap">
                <section className="content">
                    <h2>Aspirantes</h2>
                    <article className="person-boxes">
                        <div className="person-box shadow p-3 mb-5 bg-body-tertiary rounded">
                            <div className="box-avatar">
                                <img src={foto1} alt="Max"/>
                            </div>
                            <div className="box-bio">
                                <h2 className="bio-name">Maxine Caufield</h2>
                                <p className="bio-position">Profesor</p>
                            </div>
                            <div className="box-actions">
                                <button>
                                    <i className="bi bi-star"></i>
                                </button>
                                <button>
                                    <i className="bi bi-chat"></i>
                                </button>
                                <button>
                                    <i className="bi bi-envelope"></i>
                                </button>
                            </div>
                        </div>
                        <div className="person-box shadow p-3 mb-5 bg-body-tertiary rounded">
                            <div className="box-avatar">
                                <img src={foto2} alt="Alex"/>
                            </div>
                            <div className="box-bio">
                                <h2 className="bio-name">Alex Chen</h2>
                                <p className="bio-position">Técnico de sonido</p>
                            </div>
                            <div className="box-actions">
                                <button>
                                    <i className="bi bi-star"></i>
                                </button>
                                <button>
                                    <i className="bi bi-chat"></i>
                                </button>
                                <button>
                                    <i className="bi bi-envelope"></i>
                                </button>
                            </div>
                        </div>
                        <div className="person-box shadow p-3 mb-5 bg-body-tertiary rounded">
                            <div className="box-avatar">
                                <img src={foto3} alt="Lara"/>
                            </div>
                            <div className="box-bio">
                                <h2 className="bio-name">Lara Croft</h2>
                                <p className="bio-position">Linguista</p>
                            </div>
                            <div className="box-actions">
                                <button>
                                    <i className="bi bi-star"></i>
                                </button>
                                <button>
                                    <i className="bi bi-chat"></i>
                                </button>
                                <button>
                                    <i className="bi bi-envelope"></i>
                                </button>
                            </div>
                        </div>
                        <div className="person-box shadow p-3 mb-5 bg-body-tertiary rounded">
                            <div className="box-avatar">
                                <img src={foto4} alt="Steve"/>
                            </div>
                            <div className="box-bio">
                                <h2 className="bio-name">Steve Rogers</h2>
                                <p className="bio-position">Administrador</p>
                            </div>
                            <div className="box-actions">
                                <button>
                                    <i className="bi bi-star"></i>
                                </button>
                                <button>
                                    <i className="bi bi-chat"></i>
                                </button>
                                <button>
                                    <i className="bi bi-envelope"></i>
                                </button>
                            </div>
                        </div>
                        <div className="person-box shadow p-3 mb-5 bg-body-tertiary rounded">
                            <div className="box-avatar">
                                <img src={foto5} alt="Tony"/>
                            </div>
                            <div className="box-bio">
                                <h2 className="bio-name">Tony Stark</h2>
                                <p className="bio-position">Computista</p>
                            </div>
                            <div className="box-actions">
                                <button>
                                    <i className="bi bi-star"></i>
                                </button>
                                <button>
                                    <i className="bi bi-chat"></i>
                                </button>
                                <button>
                                    <i className="bi bi-envelope"></i>
                                </button>
                            </div>
                        </div>
                        <div className="person-box shadow p-3 mb-5 bg-body-tertiary rounded">
                            <div className="box-avatar">
                                <img src={foto6} alt="Nat"/>
                            </div>
                            <div className="box-bio">
                                <h2 className="bio-name">Natasha Romanoff</h2>
                                <p className="bio-position">Economista</p>
                            </div>
                            <div className="box-actions">
                                <button>
                                    <i className="bi bi-star"></i>
                                </button>
                                <button>
                                    <i className="bi bi-chat"></i>
                                </button>
                                <button>
                                    <i className="bi bi-envelope"></i>
                                </button>
                            </div>
                        </div>
                    </article>
                </section>
            </main>
        </>
    );
}
export default Aspirantes;