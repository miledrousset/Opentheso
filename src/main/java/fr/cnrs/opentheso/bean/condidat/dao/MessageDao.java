package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MessageDao extends BasicDao {
    

    public List<MessageDto> getAllMessagesCandidat(Connect connect, String idconcept, String idThesaurus, String username) {
        List<MessageDto> messages = new ArrayList<>();
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            messages = getMessagesCandidat(stmt, idconcept, idThesaurus, username);;
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return messages;
    }

    public void addNewMessage(Connect connect, String msg, String idUser, String idConcept, String idThesaurus) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeUpdate("INSERT INTO candidat_messages(value, id_user, date, id_concept, id_thesaurus) "
                    + "VALUES ('" + msg + "', " + idUser + ", '" + sdf.format(new Date())
                    + "', " + idConcept + ", '" + idThesaurus + "')");
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public List<MessageDto> getMessagesCandidat(Statement stmt, String idconcept, String idThesaurus, String username) {
        List<MessageDto> messages = new ArrayList<>();
        try {
            stmt.executeQuery("SELECT users.username, cand.value, cand.date FROM candidat_messages cand, users " +
                    "WHERE id_concept = " + idconcept + " AND id_thesaurus = '" + idThesaurus + "' AND cand.id_user = users.id_user");
            ResultSet resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                MessageDto messageDto = new MessageDto();
                messageDto.setMsg(resultSet.getString("value"));
                messageDto.setNom(resultSet.getString("username"));
                messageDto.setDate(resultSet.getString("date"));
                if (username.equals(resultSet.getString("username"))) {
                    messageDto.setMine(true);
                }
                messages.add(messageDto);
            }
            resultSet.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return messages;
    }

    public List<String> getParticipantsByCandidat(Connect connect, String candidatId, String thesaurusId) {
        List<String> participants = new ArrayList<>();
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeQuery("SELECT DISTINCT users.username FROM candidat_messages msg, users users " +
                    "WHERE msg.id_user = users.id_user AND msg.id_concept = "+candidatId+" AND msg.id_thesaurus = '"+thesaurusId+"'");
            ResultSet resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                participants.add(resultSet.getString("username"));
            }
            stmt.close();
            resultSet.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return participants;
    }
    
}
