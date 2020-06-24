package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MessageDao {

    private final Log LOG = LogFactory.getLog(MessageDao.class);
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    

    public List<MessageDto> getAllMessagesCandidat(Connect connect, Statement stmt, String idconcept, String idThesaurus, String username) {
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

    public void addNewMessage(Connect connect, Statement stmt, String msg, String idUser, String idConcept, String idThesaurus) {
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

    public List<MessageDto> getMessagesCandidat(Statement stmt, String idconcept, String idThesaurus, String username) throws SQLException {
        List<MessageDto> messages = new ArrayList<>();
        try {
            stmt.executeQuery("SELECT users.username, cand.value, cand.date "
                    + "FROM candidat_messages cand, users "
                    + "WHERE id_concept = " + idconcept + " "
                    + "AND id_thesaurus = '" + idThesaurus + "' "
                    + "AND cand.id_user = users.id_user");
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
    
}
