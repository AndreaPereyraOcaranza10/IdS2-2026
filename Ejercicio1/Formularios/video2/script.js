document.addEventListener('DOMContentLoaded', () => {
    const formulario = document.querySelector('.formulario')
    const respuesta = document.querySelector('#parrafo')

    respuesta.style.display = 'none'

    formulario.addEventListener('submit', (e) => {
        e.preventDefault()

        const nombre = document.querySelector('#nombre').value
        const password = document.querySelector('#password').value

        if(nombre === '' || password === ''){

            respuesta.style.display = 'block'
            respuesta.textContent= 'Debe llenar todos los campos'
            respuesta.style.backgroundColor = 'darkred'

        } else{
            respuesta.style.display = 'block'
            respuesta.textContent = 'Formulario enviado'
            respuesta.style.backgroundColor = 'darkgreen'

        }
    })
})