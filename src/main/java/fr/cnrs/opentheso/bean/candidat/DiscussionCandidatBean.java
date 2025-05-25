package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.services.CandidatService;
import fr.cnrs.opentheso.utils.EmailUtils;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "discussionCandidatBean")
public class DiscussionCandidatBean implements Serializable {

    private final CandidatBean candidatBean;
    private final LanguageBean languageBean;
    private final CandidatService candidatService;

    private String email;
    private List<NodeUser> nodeUsers;


    public void clear() {
        if (nodeUsers != null) {
            nodeUsers.clear();
            nodeUsers = null;
        }
        email = null;
    }

    public void getParticipantsInConversation() {

        nodeUsers = candidatService.setListUsersForMail(candidatBean.getCandidatSelected());
        if (CollectionUtils.isEmpty(nodeUsers)) {
            MessageUtils.showWarnMessage(languageBean.getMsg("candidat.send_message.msg8"));
        } else {
            PrimeFaces.current().executeScript("PF('participantsList').show();");
        }
    }

    public void sendMessage() {
        if (candidatBean.getInitialCandidat() == null) {
            MessageUtils.showWarnMessage(languageBean.getMsg("candidat.send_message.msg7"));
            return;
        }

        if (StringUtils.isEmpty(candidatBean.getMessage())) {
            MessageUtils.showWarnMessage(languageBean.getMsg("candidat.send_message.msg1"));
            return;
        }

        candidatService.sendMessage(candidatBean.getCandidatSelected(), candidatBean.getMessage(),
                candidatBean.getCurrentUser().getNodeUser().getIdUser());

        reloadMessage();
        candidatBean.setMessage("");
        MessageUtils.showInformationMessage(languageBean.getMsg("candidat.send_message.msg2"));
    }


    public void reloadMessage() {
        candidatBean.getCandidatSelected().setMessages(candidatService.getAllMessagesByCandidat(candidatBean.getCandidatSelected(),
                candidatBean.getCurrentUser().getNodeUser().getIdUser()));
    }

    public void sendInvitation() {

        if (StringUtils.isEmpty(email)) {
            MessageUtils.showWarnMessage(languageBean.getMsg("candidat.send_message.msg3"));
        } else if (!EmailUtils.isValidEmailAddress(email)) {
            MessageUtils.showWarnMessage(languageBean.getMsg("candidat.send_message.msg4"));
        } else {
            candidatService.sendMailInvitation(email);
        }
    }
}
