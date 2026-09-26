package org.example.logic;

import javax.persistence.*;

@Entity
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String nombre;
    private String tipo;

    @ManyToOne
    private Carrera carre;

    public Materia() {
    }

    public Materia(int id, String nombre, String tipo, Carrera carre) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.carre = carre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Carrera getCarre() {
        return carre;
    }

    public void setCarre(Carrera carre) {
        this.carre = carre;
    }
}
