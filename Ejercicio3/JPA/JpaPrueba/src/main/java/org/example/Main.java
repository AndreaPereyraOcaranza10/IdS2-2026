package org.example;

import org.example.logic.Alumno;
import org.example.logic.Carrera;
import org.example.logic.Controladora;
import org.example.logic.Materia;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args){
        /*//lo hacemos particularmente en el main porque aun no tenemos igu, esta informacion normalmente viene de ahí
        Controladora control = new Controladora();

        Carrera carre = new Carrera(1, "IdS2");
        //control.crearCarrera(carre);

        //Alumno alu = new Alumno(73, "Lionel", "Messi", new Date(), carre);

        //control.crearAlumno(alu);
        //control.eliminarAlumno(73);
        //alu.setNombre("Juan");
        //control.editarAlumno(alu);
        Alumno alu = control.traerAlumno(73);
        System.out.println("------------busqueda individual------------");
        System.out.println("El alumno es: " + alu.getNombre() + " " + alu.getApellido());
        System.out.println("Cursa la carrera: " + alu.getCarre().getNombre());

        System.out.println("------------busqueda general------------");
        ArrayList<Alumno> listaAlumnos = control.traerListaAlumnos();
        for (Alumno al : listaAlumnos){
            System.out.println("El alumno es: " + al.toString());
        }

    */
        Controladora control = new Controladora();

        //Creamos lista de materias
        LinkedList<Materia> listaMaterias = new LinkedList<Materia>();

        //Creación Carrera
        Carrera carre = new Carrera(1001, "LCC", listaMaterias);

        //Guardamos la carrera en la bd
        control.crearCarrera(carre);

        //Creación materias
        Materia mate1 = new Materia(10, "Teoria de Bases de Datos", "Cuatrimestral", carre);
        Materia mate2 = new Materia(20, "Ingenieria del software 1", "Cuatrimestral", carre);
        Materia mate3 = new Materia(30, "Ingenieria del software 2", "Cuatrimestral", carre);

        //Guardamos las materias en la bd
        control.crearMateria(mate1);
        control.crearMateria(mate2);
        control.crearMateria(mate3);

        //Agregamos las materias a la lista
        listaMaterias.add(mate1);
        listaMaterias.add(mate2);
        listaMaterias.add(mate3);

        carre.setListaMaterias(listaMaterias);
        control.editarCarrera(carre);

        //Creamos alumnos
        Alumno alu = new Alumno(1, "Jane", "Doe", new Date(), carre);
        Alumno alu1 = new Alumno(2, "Maxine", "Caufield", new Date(), carre);
        Alumno alu2 = new Alumno(3, "Alex", "Chen", new Date(), carre);

        //Guardamos alumnos en la bd
        control.crearAlumno(alu);
        control.crearAlumno(alu2);
        control.crearAlumno(alu1);

        //Vemos el resultado
        System.out.println("-------------------------");
        System.out.println("------Datos alumnos------");
        System.out.println("-------------------------");
        System.out.println("El alumno es: " + alu.getNombre() + " " + alu.getApellido());
        System.out.println("Cursa la carrera: " + alu.getCarre().getNombre());
        System.out.println("El alumno es: " + alu1.getNombre() + " " + alu1.getApellido());
        System.out.println("Cursa la carrera: " + alu1.getCarre().getNombre());
        System.out.println("El alumno es: " + alu2.getNombre() + " " + alu2.getApellido());
        System.out.println("Cursa la carrera: " + alu2.getCarre().getNombre());


    }



}
