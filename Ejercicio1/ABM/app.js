let listaEmpleado = []

const objEmpleado= {
    id: "",
    nombre:"",
    puesto:""
}

let editando = false

const formulario = document.querySelector("#formulario")
const nombreI = document.querySelector("#nombre")
const puestoI = document.querySelector("#puesto")
const btnAgregar = document.querySelector("#btnAgregar")

formulario.addEventListener("submit", validarFormulario)

function validarFormulario(e){
    e.preventDefault()

    if(nombreI.value === "" || puestoI.value === ""){
        alert("Todos los campos son obligatorios.")
        return
    }

    if (editando){
        editarEmpleado();
        editando = false;
    } else {
        objEmpleado.id = Date.now()
        objEmpleado.nombre = nombreI.value
        objEmpleado.puesto = puestoI.value

        agregarEmpleado()
    }

}

function agregarEmpleado(){
    listaEmpleado.push({...objEmpleado})
    mostrarEmpleado()

    formulario.reset()
    limpiarObjeto()
}

function limpiarObjeto(){
    objEmpleado.id = ""
    objEmpleado.nombre = ""
    objEmpleado.puesto = ""
}

function mostrarEmpleado(){

    limpiarHTML()
    const divEmpleados = document.querySelector('.div-empleados')

    listaEmpleado.forEach(empleado =>{
        const {id, nombre, puesto} = empleado

        const parrafo = document.createElement('p')
        parrafo.textContent = `${id} - ${nombre} - ${puesto} -`
        parrafo.dataset.id = id

        const editarBtn = document.createElement('button')
        editarBtn.onclick = () => cargarEmpleado(empleado)
        editarBtn.textContent = "Editar"
        editarBtn.classList.add("btn", "btn-editar")
        parrafo.append(editarBtn)

        const eliminarBtn = document.createElement('button')
        eliminarBtn.onclick = () => eliminarEmpleado(id)
        eliminarBtn.textContent = "Eliminar"
        eliminarBtn.classList.add("btn", "btn-eliminar")
        parrafo.append(eliminarBtn)

        const hr = document.createElement("hr")

        divEmpleados.appendChild(parrafo)
        divEmpleados.appendChild(hr)
    })
}

function limpiarHTML(){
    const divEmpleados = document.querySelector(".div-empleados")
    while(divEmpleados.firstChild){
        divEmpleados.removeChild(divEmpleados.firstChild)
    }
}

function cargarEmpleado(empleado){
    const{id, nombre, puesto} = empleado
    nombreI.value = nombre
    puestoI.value = puesto

    objEmpleado.id = id

    formulario.querySelector('button[type="submit"]').textContent = "Actualizar"

    editando = true
}


function editarEmpleado(){
    objEmpleado.nombre = nombreI.value
    objEmpleado.puesto = puestoI.value

    listaEmpleado.map(empleado => {
        if (empleado.id === objEmpleado.id){
            empleado.id = objEmpleado.id
            empleado.nombre = objEmpleado.nombre
            empleado.puesto = objEmpleado.puesto
        }
    })
    limpiarHTML()
    mostrarEmpleado()
    formulario.reset()

    formulario.querySelector('button[type="submit"]').textContent = "Agregar"

    editando = false
}

function eliminarEmpleado(id){
    listaEmpleado = listaEmpleado.filter(empleado => empleado.id !== id)
    limpiarHTML()
    mostrarEmpleado()
}