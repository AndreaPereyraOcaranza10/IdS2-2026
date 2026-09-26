package org.example.logic;

import javax.persistence.*;
import java.util.Date;

@Entity /*Crea la tabla alumno en la base de datos*/
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) /*genera las id automaticamente*/
    private int id;
    @Basic
    private String nombre;
    private String apellido;
    @Temporal(TemporalType.DATE)
    private Date fechaNac;

    @OneToOne
    private Carrera carre;

    public Alumno(){

    }

    public Alumno(int id, String nombre, String apellido, Date fechaNac, Carrera carre) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNac = fechaNac;
        this.carre = carre;
    }

    public Carrera getCarre() {
        return carre;
    }

    public void setCarre(Carrera carre) {
        this.carre = carre;
    }

    public Date getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(Date fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Alumno{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", fechaNac=" + fechaNac +
                '}';
    }
}
