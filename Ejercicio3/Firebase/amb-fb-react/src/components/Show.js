import React, {useState, useEffect} from 'react'
import {Link} from 'react-router-dom'
import {collection, getDocs, getDoc, deleteDoc, doc} from 'firebase/firestore'
import {db} from '../firebaseConfig/firebase'                        
import Swal from 'sweetalert2'
import withReactContent from 'sweetalert2-react-content'

const MySwal = withReactContent(Swal)

const Show = () => {
    //Configuramos los hooks
    const [products, setProducts] = useState ([])

    //Referenciamos la bd Firestore
    const productsCollection = collection(db, "products")

    //Funcion para mostrar los docs
    const getProducts = async() => {
        const data = await getDocs(productsCollection)
        //console.log(data.docs)
        setProducts(
            data.docs.map((doc) => ({...doc.data(),  id:doc.id}))
        )
        console.log(products)
    }

    //Funcion para eliminar un doc
    const deleteProduct = async (id) => {
        const productDoc = doc(db, "products", id)
        await deleteDoc(productDoc)
        getProducts()
    }

    //Funcion de confirmacion para sweet alert 2
    const confirmDelete = (id) => {
        Swal.fire({
        title: "Desea eliminar los productos?",
        text: "No podrás revertir los cambios",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Sí, eliminar!",
        cancelButtonText: "Cancelar"
        }).then((result) => {
        if (result.isConfirmed) {deleteProduct(id); Swal.fire({
            title: "Eliminado!",
            text: "Tu producto ha sido eliminado.",
            icon: "success"
        })};
        });
    }

    //usamos useEffect
    useEffect(() => {
        getProducts()
        //eslint-disable-next-line
    }, [] );

    //devolvemos la vista del componente


  return (
    <>
    <div className='container'>
        <div className='row'>
            <div className='col'>
                <div className='d-grid gap-2'>
                    <Link to="/create" className='btn btn-secondary mt-2 mb-2'>Create</Link>
                </div>

                <table className='table table-dark table-hover'>
                    <thead>
                        <tr>
                            <th>Description</th>
                            <th>Stock</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {products.map((product) => (
                            <tr key={product.id}>
                                <td>{product.description}</td>
                                <td>{product.stock}</td>
                                <td>
                                    <Link to={`/edit/${product.id}`} className='btn btn-light'>
                                    <i className="fa-regular fa-pen-to-square"></i>
                                    </Link>
                                    <button onClick={() => {confirmDelete(product.id)}} className='btn btn-danger'><i className="fa-regular fa-trash-can"></i></button>
                                </td>
                            </tr>
                        ))}
                    </tbody>

                </table>

            </div>
        </div>
    </div>
    </>
  )
}

export default Show