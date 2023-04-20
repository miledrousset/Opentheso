package fr.cnrs.opentheso.bean.candidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.candidat.dto.MessageDto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MessageDao {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public void addNewMessage(HikariDataSource hikariDataSource, String msg,
            int idUser, String idConcept, String idThesaurus) {
        addNewMessage(hikariDataSource, msg, idUser, idConcept, idThesaurus, sdf.format(new Date()));
    }

    public void addNewMessage(HikariDataSource hikariDataSource, String msg,
                              int idUser, String idConcept, String idThesaurus, String date) {
        msg = new StringPlus().convertString(msg);
        try (Connection connection = hikariDataSource.getConnection()){
            try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(new StringBuffer("INSERT INTO candidat_messages(value, id_user, date, id_concept, id_thesaurus) ")
                    .append("VALUES ('").append(msg).append("', ").append(idUser).append(", '")
                    .append(date).append("', '").append(idConcept)
                    .append("', '").append(idThesaurus).append("')").toString());
            }
        } catch (Exception e) {}
    }

    public List<MessageDto> getAllMessagesByCandidat(HikariDataSource hikariDataSource, String idConcept, String idTheso) {
        List<MessageDto> messages = new ArrayList<>();
        try (Connection connection = hikariDataSource.getConnection()){
            try (Statement stmt = connection.createStatement()) {
                stmt.executeQuery("SELECT * FROM candidat_messages WHERE id_concept = '"+idConcept+"' AND id_thesaurus = '"+idTheso+"'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while(resultSet.next()) {
                        MessageDto message = new MessageDto();
                        message.setMsg(resultSet.getString("value"));
                        message.setIdUser(resultSet.getInt("id_user"));
                        message.setDate(resultSet.getString("date"));
                        messages.add(message);
                    }
                }
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return messages;
    }

    public List<MessageDto> getAllMessagesByCandidat(HikariDataSource hikariDataSource, String idconcept, String idThesaurus, int userId) {
        
        List<MessageDto> messages = new ArrayList<>();
        try (Connection connection = hikariDataSource.getConnection()){
            try (Statement stmt = connection.createStatement()) {
                stmt.executeQuery(new StringBuffer("SELECT users.id_user, users.username, cand.value, cand.date FROM candidat_messages cand, users ")
                    .append("WHERE id_concept = '").append(idconcept).append("' AND id_thesaurus = '")
                    .append(idThesaurus).append("' AND cand.id_user = users.id_user order by cand.date").toString());
                try (ResultSet resultSet = stmt.getResultSet()) {
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
                }
            }
        } catch (SQLException e) {
            return messages;
        }
        return messages;
    }

    public List<NodeUser> getParticipantsByCandidat(HikariDataSource hikariDataSource, String candidatId, String thesaurusId) {
        List<Integer> participants = new ArrayList<>();
        try (Connection connection = hikariDataSource.getConnection()){
            try (Statement stmt = connection.createStatement()) {
            stmt.executeQuery(new StringBuffer("SELECT DISTINCT users.id_user FROM candidat_messages msg, users users ")
                    .append("WHERE msg.id_user = users.id_user AND msg.id_concept = '")
                    .append(candidatId).append("' AND msg.id_thesaurus = '")
                    .append(thesaurusId).append("'").toString());
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        participants.add(resultSet.getInt("id_user"));
                    }
                }
            }
        } catch (SQLException e) {
            return null;
        }
        UserHelper userHelper = new UserHelper();
        List<NodeUser> nodeUsers = new ArrayList<>();
        for (int idUser : participants) {
            NodeUser nodeUser = userHelper.getUser(hikariDataSource, idUser);
            nodeUsers.add(nodeUser);
        }
        return nodeUsers;
    }
    
}
