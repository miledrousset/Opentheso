package fr.cnrs.opentheso.models.timeJob;

import java.util.ArrayList;
import java.util.Date;
import fr.cnrs.opentheso.models.candidats.NodeProposition;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import lombok.Data;

/**
 *
 * @author jm.prudham
 */
@Data
public class LineCdt {

    private String id_thesaurus;
    private String title_thesaurus;
    private String Id_concept;
    private String valeur_lexical;
    private Date created;
    private Date modified;
    private String status;
    private String admin_message;
    private String note;
    private ArrayList<NodeProposition> nodeProposition;
    private ArrayList<NodeTermTraduction> nodeTermTraductions;

    public String getMessage() {
        // #MR
        StringBuilder message = new StringBuilder();
        String status1;
        switch (status) {
            case ("a"):
                status1 = "en attente";
                break;
            case ("r"):
                status1 = "refusé";
                break;
            case ("v"):
                status1 = "validé";
                break;
            case ("i"):
                status1 = "inséré";
                break;
            default:
                status1 = "inconnu";
                break;
        }

        message.append("<table style=\"width: 715px; height: 321px;\" border=\"0\">");
        message.append("<tbody>");
        
        // la liste des candidats avec les traductions
        for (NodeTermTraduction nodeTermTraduction : nodeTermTraductions) {
            message.append("<tr>");
            message.append("<td><i> Candidat :");
            message.append("</i></td>");
            message.append("<td><b>");
            message.append(nodeTermTraduction.getLexicalValue());
            message.append("@");
            message.append(nodeTermTraduction.getLang());
            message.append("</b></td>");
            message.append("</tr>");            
        }
        
        message.append("<tr>");
        message.append("<td><i>id :");
        message.append("</i></td>");
        message.append("<td><b>");
        message.append(Id_concept);
        message.append("</b></td>");
        message.append("</tr>");
        message.append("<tr>");
        message.append("<td><i>créé le :");
        message.append("</i></td>");
        message.append("<td><b>");
        message.append(created);
        message.append("</b></td>");
        message.append("</tr>");
        message.append("<tr>");
        message.append("<td><i>modifié le :");
        message.append("</i></td>");
        message.append("<td><b>");
        message.append(modified);
        message.append("</b></td>");
        message.append("</tr>");
        message.append("<tr>");
        message.append("<td><i>Status :");
        message.append("</i></td>");
        message.append("<td><b>");
        message.append(status1);
        message.append("</b></td>");
        message.append("</tr>");
        message.append("<tr>");
        message.append("<td><i>proposé par :");
        message.append("</i>");

        // données concernant les propositions faites par les utilisateurs
        for (NodeProposition nodeProposition1 : nodeProposition) {
            message.append("<tr>");
            message.append("<td><b>");
            message.append(nodeProposition1.getUser());
            message.append("</b></td>");
            message.append("</tr>");

            message.append("<tr>");
            message.append("<td><b>");
            message.append(nodeProposition1.getNote());
            message.append("</b></td>");
            message.append("</tr>");

            message.append("<tr>");
            message.append("<td><b>");
            if (nodeProposition1.getLabelConceptParent() != null) {
                message.append(nodeProposition1.getLabelConceptParent());
            }
            message.append("</b></td>");
            message.append("</tr>");
        }
        message.append("</td>");

        if (admin_message != null && !admin_message.isEmpty()) {
            message.append("<tr>");
            message.append("<td><i>Message de l'admin :");
            message.append("</i></td>");
            message.append("<td><b>");
            message.append(admin_message);
            message.append("</b></td>");
            message.append("</tr>");
        }

        message.append("<tr>");
        message.append("<td><i>Thésaurus :");
        message.append("</i></td>");
        message.append("<td><b>");
        message.append(title_thesaurus);
        message.append("</b></td>");
        message.append("</tr>");
        message.append("</tbody>");
        message.append("</table>");
        message.append("<hr color=\"blue\"> ");
        message.append("<br>");
        return message.toString();
    }

}
