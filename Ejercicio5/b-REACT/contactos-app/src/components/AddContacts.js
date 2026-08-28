import React from "react";

class AddContacts extends React.Component {
    state = {
        name: "",
        email: ""
    };

    add = (e) => {
        e.preventDefault();
        if(this.state.name === "" || this.state.email === ""){
            alert("Todos los campos son obligatorios");
            return;
        }

        this.props.AddContactHandler(this.state);
        this.setState({ name: "", email: ""});
        console.log(this.state);
    }

    render() {  
        return(
            <div className="ui main">
                <h2>Agregar Contacto</h2>
                <form className="ui form" onSubmit={(this.add)}>
                    <div className="field">
                        <label>Nombre</label>
                        <input type="text" name="name" value={this.state.name} placeholder="Nombre" onChange={(e) => this.setState({ name: e.target.value})}/>
                    </div>
                    <div className="field">
                        <label>Email</label>
                        <input type="email" name="email" value={this.state.email} placeholder="Email" onChange={(e) => this.setState({ email: e.target.value})}/>
                    </div>
                    <button className="ui button blue">Agregar</button>
                </form>
            </div>
        )
    }
}

    export default AddContacts;