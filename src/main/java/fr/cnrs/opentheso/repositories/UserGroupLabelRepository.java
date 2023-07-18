package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.SessionFactoryMaker;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;


@ApplicationScoped
public class UserGroupLabelRepository {


    public List<UserGroupLabel> getAllProjects() {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Query releaseQuery = session.createQuery("SELECT label FROM UserGroupLabel label", UserGroupLabel.class);
            return releaseQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<UserGroupLabel> getProjectsByThesoStatus(boolean status) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Query releaseQuery = session.createQuery("SELECT labele "
                    + "FROM Thesaurus the, UserGroupThesaurus grp, UserGroupLabel labele "
                    + "WHERE the.privateTheso = :status "
                    + "AND grp.idThesaurus = the.thesaurusId "
                    + "AND labele.id = grp.idGroup", UserGroupLabel.class)
                    .setParameter("status", status);
            return releaseQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<UserGroupLabel> getProjectsByUserId(int userId) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            TypedQuery<UserGroupLabel> typedQuery = session.createQuery("SELECT distinct lab "
                            + "FROM UserGroupThesaurus the, UserRoleGroup grp, UserGroupLabel lab "
                            + "WHERE grp.idGroup = the.idGroup "
                            + "AND lab.id = the.idGroup "
                            + "AND grp.idUser = :userId", UserGroupLabel.class)
                    .setParameter("userId", userId);
            return typedQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
