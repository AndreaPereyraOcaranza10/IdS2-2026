document.getElementById('myForm').addEventListener('submit', function(event){
    event.preventDefault()
    const username = document.getElementById('username').value
    const email = document.getElementById('email').value

    if (username.length < 8){
        console.log('El usuario debe tener más de 8 caracteres')
        return
    }

    if (username && email){
        console.log(`Usuario: ${username}`)
        console.log(`Email: ${email}`)
        console.log('Todo en orden')
    } else {
        console.log('Algunos campos estan vacíos')
    }
})
