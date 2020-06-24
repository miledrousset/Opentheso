package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.condidat.dto.TraductionDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LangageDao {

    private final Log LOG = LogFactory.getLog(NoteDao.class);
    
    
    public List<TraductionDto> getLangagesCandidat(Statement stmt, String idThesaurus) {
        List<TraductionDto> Traductions = new ArrayList<>();
        try {
            stmt.executeQuery("SELECT lang, lexical_value FROM term WHERE id_thesaurus = '" + idThesaurus + "'");
            ResultSet resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                TraductionDto traductionDto = new TraductionDto();
                traductionDto.setLangue(resultSet.getString("lang"));
                traductionDto.setTraduction(resultSet.getString("lexical_value"));
                Traductions.add(traductionDto);
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return Traductions;
    }
}
