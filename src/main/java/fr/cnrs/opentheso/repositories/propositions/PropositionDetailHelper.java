package fr.cnrs.opentheso.repositories.propositions;

import fr.cnrs.opentheso.models.propositions.PropositionDetailDao;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import fr.cnrs.opentheso.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Service
public class PropositionDetailHelper {

    @Autowired
    private DataSource dataSource;


    public List<PropositionDetailDao> getPropositionDetail(int propositionId) {

        List<PropositionDetailDao> propositionDetails = new ArrayList<>();

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from proposition_modification_detail where id_proposition = " + propositionId);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        PropositionDetailDao propositionDetail = new PropositionDetailDao();
                        propositionDetail.setId(resultSet.getInt("id"));
                        propositionDetail.setIdProposition(resultSet.getInt("id_proposition"));
                        propositionDetail.setCategorie(resultSet.getString("categorie"));
                        propositionDetail.setValue(resultSet.getString("value"));
                        propositionDetail.setOldValue(resultSet.getString("old_value"));
                        propositionDetail.setAction(resultSet.getString("action"));
                        propositionDetail.setLang(resultSet.getString("lang"));
                        propositionDetail.setHiden(resultSet.getBoolean("hiden"));
                        propositionDetail.setStatus(resultSet.getString("status"));
                        propositionDetail.setIdTerm(resultSet.getString("id_term"));
                        propositionDetails.add(propositionDetail);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Erreur lors de la recherche de détails de proposition : " + sqle.getMessage());
        }

        return propositionDetails;
    }

    public boolean createNewPropositionDetail(PropositionDetailDao propositionDetail) {
        
        propositionDetail.setValue(StringUtils.convertString(propositionDetail.getValue()));
        propositionDetail.setOldValue(StringUtils.convertString(propositionDetail.getOldValue()));
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO public.proposition_modification_detail(id_proposition, categorie, value, old_value, action, lang, hiden, status, id_term) "
                        + "VALUES (" + propositionDetail.getIdProposition() + ", '" + propositionDetail.getCategorie()
                        + "', '" + propositionDetail.getValue() + "', '" + propositionDetail.getOldValue() + "', '"
                        + propositionDetail.getAction() + "', '" + propositionDetail.getLang() + "', " + propositionDetail.isHiden()
                        + ", '" + propositionDetail.getStatus() + "', '" + propositionDetail.getIdTerm()+ "');");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Erreur lors de la creation d'une nouvelle proposition : " + sqle.getMessage());
            return false;
        }
    }

    public boolean supprimerPropositionDetails(int propositionID) {
        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM proposition_modification_detail WHERE id_proposition = " + propositionID);
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error lords de la suppression de la proposition : " + propositionID);
        }
        return status;
    }

}
