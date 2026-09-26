package org.example.logic;

import javax.persistence.*;
import java.util.LinkedList;

@Entity
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String nombre;
    @OneToMany (mappedBy = "carre")
    private LinkedList<Materia> listaMaterias;

    public Carrera() {
    }

    public Carrera(int id, String nombre, LinkedList<Materia> listaMaterias) {
        this.id = id;
        this.nombre = nombre;
        this.listaMaterias = listaMaterias;
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

    public LinkedList<Materia> getListaMaterias() {
        return listaMaterias;
    }

    public void setListaMaterias(LinkedList<Materia> listaMaterias) {
        this.listaMaterias = listaMaterias;
    }
}
