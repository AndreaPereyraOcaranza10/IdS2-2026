package org.example.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.example.logic.Carrera;
import org.example.logic.Materia;

public class CarreraJpaController implements Serializable {

    private EntityManagerFactory emf = null;

    public CarreraJpaController() {
        this.emf = Persistence.createEntityManagerFactory("pruebaJPAPU");
    }

    public CarreraJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Carrera carrera) {
        if (carrera.getListaMaterias() == null) {
            carrera.setListaMaterias(new LinkedList<Materia>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();

            // Se asegura de asociar cada materia existente a esta carrera
            LinkedList<Materia> attachedListaMaterias = new LinkedList<>();
            for (Materia materiaItem : carrera.getListaMaterias()) {
                materiaItem = em.getReference(materiaItem.getClass(), materiaItem.getId());
                attachedListaMaterias.add(materiaItem);
            }
            carrera.setListaMaterias(attachedListaMaterias);

            em.persist(carrera);

            for (Materia materiaItem : carrera.getListaMaterias()) {
                Carrera oldCarre = materiaItem.getCarre();
                materiaItem.setCarre(carrera);
                materiaItem = em.merge(materiaItem);
                if (oldCarre != null) {
                    oldCarre.getListaMaterias().remove(materiaItem);
                    em.merge(oldCarre);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Carrera carrera) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();

            Carrera persistentCarrera = em.find(Carrera.class, carrera.getId());
            LinkedList<Materia> listaMateriasOld = persistentCarrera.getListaMaterias();
            LinkedList<Materia> listaMateriasNew = carrera.getListaMaterias();

            LinkedList<Materia> attachedListaMateriasNew = new LinkedList<>();
            if (listaMateriasNew != null) {
                for (Materia materiaItem : listaMateriasNew) {
                    materiaItem = em.getReference(materiaItem.getClass(), materiaItem.getId());
                    attachedListaMateriasNew.add(materiaItem);
                }
                carrera.setListaMaterias(attachedListaMateriasNew);
            }

            carrera = em.merge(carrera);

            // Sincroniza las claves foráneas si se removieron materias
            if (listaMateriasOld != null) {
                for (Materia materiaOld : listaMateriasOld) {
                    if (attachedListaMateriasNew == null || !attachedListaMateriasNew.contains(materiaOld)) {
                        materiaOld.setCarre(null);
                        em.merge(materiaOld);
                    }
                }
            }

            // Sincroniza las claves foráneas de las materias nuevas agregadas
            if (attachedListaMateriasNew != null) {
                for (Materia materiaNew : attachedListaMateriasNew) {
                    if (listaMateriasOld == null || !listaMateriasOld.contains(materiaNew)) {
                        Carrera oldCarre = materiaNew.getCarre();
                        materiaNew.setCarre(carrera);
                        em.merge(materiaNew);
                        if (oldCarre != null && !oldCarre.equals(carrera)) {
                            oldCarre.getListaMaterias().remove(materiaNew);
                            em.merge(oldCarre);
                        }
                    }
                }
            }

            em.getTransaction().commit();
        } catch (Exception ex) {
            int id = carrera.getId();
            if (findCarrera(id) == null) {
                throw new EntityNotFoundException("La carrera con id " + id + " no existe.");
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(int id) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Carrera carrera;
            try {
                carrera = em.getReference(Carrera.class, id);
                carrera.getId();
            } catch (EntityNotFoundException enfe) {
                throw new Exception("La carrera con id " + id + " no existe.", enfe);
            }

            // Desenlaza las materias asociadas antes de borrar la carrera
            LinkedList<Materia> listaMaterias = carrera.getListaMaterias();
            if (listaMaterias != null) {
                for (Materia materia : listaMaterias) {
                    materia.setCarre(null);
                    em.merge(materia);
                }
            }

            em.remove(carrera);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Carrera> findCarreraEntities() {
        return findCarreraEntities(true, -1, -1);
    }

    public List<Carrera> findCarreraEntities(int maxResults, int firstResult) {
        return findCarreraEntities(false, maxResults, firstResult);
    }

    private List<Carrera> findCarreraEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Carrera.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Carrera findCarrera(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Carrera.class, id);
        } finally {
            em.close();
        }
    }

    public int getCarreraCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Carrera> rt = cq.from(Carrera.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
