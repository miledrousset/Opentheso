package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.SessionFactoryMaker;
import fr.cnrs.opentheso.entites.ProjectDescription;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ProjectDescriptionRepository {

    public void saveProjectDescription(ProjectDescription projectDescription) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            session.save(projectDescription);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement de la description du projet : " + ex.getMessage());
        }
    }

    public ProjectDescription getProjectDescription(String idGroup, String lang) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            String query = "SELECT projectDesc "
                    + "FROM ProjectDescription projectDesc "
                    + "WHERE projectDesc.idGroup = :idGroup "
                    + "AND projectDesc.lang = :lang";
            TypedQuery<ProjectDescription> typedQuery = session.createQuery(query, ProjectDescription.class)
                    .setParameter("idGroup", idGroup)
                    .setParameter("lang", lang)
                    .setMaxResults(1);
            return typedQuery.getSingleResult();
        } catch (Exception ex) {
            return null;
        }
    }

}
