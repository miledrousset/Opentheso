package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.SessionFactoryMaker;
import fr.cnrs.opentheso.entites.Gps;
import jakarta.persistence.Query;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;


@ApplicationScoped
public class GpsRepository {

    public void removeGps(Gps gps) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            session.update(String.format("DELETE FROM gps WHERE id_concept = %s AND id_theso = %s AND latitude = %s AND longitude = %s",
                    gps.getIdConcept(), gps.getIdTheso(), gps.getLatitude(), gps.getLongitude()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void deleteGps(Gps gps) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            session.save(gps);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement de la release : " + ex.getMessage());
        }
    }


    public void saveNewGps(Gps gps) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            session.save(gps);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement de la release : " + ex.getMessage());
        }
    }

    public List<Gps> getGpsByConceptAndThesorus(String idConcept, String idTheso) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Query rleaseQuery = session.createQuery("SELECT gps FROM Gps gps WHERE gps.idConcept = :idConcept AND gps.idTheso = :idTheso", Gps.class)
                    .setParameter("idConcept", idConcept)
                    .setParameter("idTheso", idTheso);
            return rleaseQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

}
