package org.example.persistence;

import org.example.logic.Alumno;
import org.example.logic.Carrera;
import org.example.logic.Materia;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ControladoraPersistencia {

    AlumnoJpaController aluJpa = new AlumnoJpaController();

    //CRUD alumno
    public void crearAlumno(Alumno alu) {
        aluJpa.create(alu);
    }

    public void eliminarAlumno(int id) {
        try {
            aluJpa.destroy(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void editarAlumno(Alumno alu) {

        try {
            aluJpa.edit(alu);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Alumno traerAlumno(int id) {
        return aluJpa.findAlumno(id);
    }

    //forma correcta de traer una lista desde la persistencia
    public ArrayList<Alumno> traerListaAlumnos() {
        List<Alumno> lista = aluJpa.findAlumnoEntities();
        ArrayList<Alumno> listaAlumnos = new ArrayList<Alumno>(lista);
        return listaAlumnos;
    }

    /*---------------Carrera--------------------*/

    CarreraJpaController carreJpa = new CarreraJpaController();

    public void crearCarrera(Carrera carre) {
        carreJpa.create(carre);
    }

    public void eliminarCarrera(int id) {
        try {
            carreJpa.destroy(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void editarCarrera(Carrera carre) {
        try {
            carreJpa.edit(carre);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Carrera traerCarrera(int id) {
        return carreJpa.findCarrera(id);
    }

    public ArrayList<Carrera> traerListaCarreras() {
        List<Carrera> lista = carreJpa.findCarreraEntities();
        ArrayList<Carrera> listaCarreras = new ArrayList<Carrera>(lista);
        return listaCarreras;
    }

    /*---------------Materias--------------------*/

    MateriaJpaController mateJpa = new MateriaJpaController();

    public void crearMateria(Materia mate) {
        mateJpa.create(mate);
    }

    public void eliminarMateria(int id) {
        try {
            mateJpa.destroy(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void editarMateria(Materia mate) {
        try {
            mateJpa.edit(mate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Materia traerMateria(int id) {
        return mateJpa.findMateria(id);
    }

    public LinkedList<Materia> traerListaMaterias() {
        List<Materia> lista = mateJpa.findMateriaEntities();
        return new LinkedList<>(lista);
    }


}
