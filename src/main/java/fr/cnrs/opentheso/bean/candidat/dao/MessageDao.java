package fr.cnrs.opentheso.bean.candidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.candidat.dto.MessageDto;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MessageDao extends BasicDao {

    public void addNewMessage(HikariDataSource hikariDataSource, String msg,
            int idUser, String idConcept, String idThesaurus) {
        addNewMessage(hikariDataSource, msg, idUser, idConcept, idThesaurus, sdf.format(new Date()));
    }

    public void addNewMessage(HikariDataSource hikariDataSource, String msg,
                              int idUser, String idConcept, String idThesaurus, String date) {
        msg = new StringPlus().convertString(msg);
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate(new StringBuffer("INSERT INTO candidat_messages(value, id_user, date, id_concept, id_thesaurus) ")
                    .append("VALUES ('").append(msg).append("', ").append(idUser).append(", '")
                    .append(date).append("', '").append(idConcept)
                    .append("', '").append(idThesaurus).append("')").toString());
            closeDataBase();
        } catch (SQLException e) {
            try {
                closeDataBase();
            } catch (Exception ed) {
            }
            LOG.error(e);
        }
    }

    public List<MessageDto> getAllMessagesByCandidat(HikariDataSource hikariDataSource, String idConcept, String idTheso) {
        List<MessageDto> messages = new ArrayList<>();
        try {
            openDataBase(hikariDataSource);
            String requet = "SELECT * FROM candidat_messages WHERE id_concept = '"+idConcept+"' AND id_thesaurus = '"+idTheso+"'";
            stmt.executeQuery(requet);
            resultSet = stmt.getResultSet();
            while(resultSet.next()) {
                MessageDto message = new MessageDto();
                message.setMsg(resultSet.getString("value"));
                message.setIdUser(resultSet.getInt("id_user"));
                message.setDate(resultSet.getString("date"));
                messages.add(message);
            }
            closeDataBase();
        } catch (Exception e) {

        }

        return messages;
    }

    public List<MessageDto> getAllMessagesByCandidat(HikariDataSource hikariDataSource, String idconcept, 
            String idThesaurus, int userId) {
        
        List<MessageDto> messages = new ArrayList<>();
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT users.id_user, users.username, cand.value, cand.date FROM candidat_messages cand, users ")
                    .append("WHERE id_concept = '").append(idconcept).append("' AND id_thesaurus = '")
                    .append(idThesaurus).append("' AND cand.id_user = users.id_user").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                MessageDto messageDto = new MessageDto();
                messageDto.setMsg(resultSet.getString("value"));
                messageDto.setNom(resultSet.getString("username"));
                messageDto.setDate(resultSet.getString("date"));
                if (userId == resultSet.getInt("id_user")) {
                    messageDto.setMine(true);
                }
                messages.add(messageDto);
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return messages;
    }

    public List<String> getParticipantsByCandidat(HikariDataSource hikariDataSource, String candidatId, String thesaurusId) {
        List<String> participants = new ArrayList<>();
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT DISTINCT users.username FROM candidat_messages msg, users users ")
                    .append("WHERE msg.id_user = users.id_user AND msg.id_concept = '")
                    .append(candidatId).append("' AND msg.id_thesaurus = '")
                    .append(thesaurusId).append("'").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                participants.add(resultSet.getString("username"));
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return participants;
    }
    
}
