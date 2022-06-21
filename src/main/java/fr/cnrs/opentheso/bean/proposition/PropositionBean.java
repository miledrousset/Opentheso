package fr.cnrs.opentheso.bean.proposition;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDao;
import fr.cnrs.opentheso.bean.proposition.helper.PropositionHelper;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.search.SearchBean;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.PrimeFaces;


@Named(value = "propositionBean")
@SessionScoped
public class PropositionBean implements Serializable {
    
    @Inject 
    private CurrentUser currentUser;
    
    @Inject 
    private ConceptView conceptView;
    
    @Inject 
    private RoleOnThesoBean roleOnThesoBean;
    
    @Inject 
    private SelectedTheso selectedTheso;
    
    @Inject
    private RightBodySetting rightBodySetting;
    
    @Inject
    private Connect connect;
    
    private boolean isRubriqueVisible;
    private Proposition proposition;
    private String nom, email, commentaire;
    
    private List<PropositionDao> propositions;
    
    public void onSelectConcept(String idTheso, String idConcept, String idLang) {
        roleOnThesoBean.initNodePref(idTheso);
        selectedTheso.setSelectedIdTheso(idTheso);
        selectedTheso.setSelectedLang(idLang);
        try {
            selectedTheso.setSelectedThesoForSearch();
        } catch (IOException ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        conceptView.getConcept(idTheso, idConcept, idLang);
        rightBodySetting.setIndex("4");
        
        isRubriqueVisible = true;
    }

    public void afficherListPropositions() {
        propositions = new PropositionHelper().getAllProposition(connect.getPoolConnexion());
        PrimeFaces.current().executeScript("PF('listNotification').show();");
    }
    
    public void switchToNouvelleProposition(NodeConcept nodeConcept) {
        isRubriqueVisible = true;
        
        if (currentUser.getNodeUser() == null) {
            rightBodySetting.setIndex("2");
        } else {
            rightBodySetting.setIndex("3");
        }
        
        proposition = new Proposition();
        
        proposition.setNomConceptProp(null);
        proposition.setNomConcept(nodeConcept.getTerm());
        
        proposition.setSynonymsProp(toSynonymPropBean(nodeConcept.getNodeEM()));
    }

    public void annulerPropostion() {
        
        proposition = null;
        isRubriqueVisible = false;     
        rightBodySetting.setIndex("1");
    }
    
    public void envoyerProposition() {
        try {
            sendRecapEmail();
        } catch (IOException ex) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Erreur detectée pendant l'envoie du mail de notification!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }
    
    private List<SynonymPropBean> toSynonymPropBean(List<NodeEM> nodesEm) {
        List<SynonymPropBean> synonyms = null;
        
        if (CollectionUtils.isNotEmpty(nodesEm)) {
            synonyms = new ArrayList<>();
            for (NodeEM nodeEM : nodesEm) {
                SynonymPropBean synonymPropBean = new SynonymPropBean();
                synonymPropBean.setAction(nodeEM.getAction());
                synonymPropBean.setCreated(nodeEM.getCreated());
                synonymPropBean.setModified(nodeEM.getModified());
                synonymPropBean.setHiden(nodeEM.isHiden());
                synonymPropBean.setOldHiden(nodeEM.isHiden());
                synonymPropBean.setLang(nodeEM.getLang());
                synonymPropBean.setLexical_value(nodeEM.getLexical_value());
                synonymPropBean.setOldValue(nodeEM.getLexical_value());
                synonymPropBean.setSource(nodeEM.getSource());
                synonymPropBean.setStatus(nodeEM.getStatus());
                synonyms.add(synonymPropBean);
            }
        }
        
        return synonyms;
    }
    
    public boolean isIsRubriqueVisible() {
        return isRubriqueVisible;
    }

    public void setIsRubriqueVisible(boolean isRubriqueVisible) {
        this.isRubriqueVisible = isRubriqueVisible;
    }
    
    public void ajouterPropNomConcept() {
        //proposition.set(proposition.getAncienNom());
        //proposition.getNouveauNom().setLexical_value(proposition.getNomConceptProp());
    }
    
    public void sendRecapEmail() throws IOException {
       Email from = new Email("firas.gabsi@gmail.com");
       Email to = new Email("firas.gabsi@gmail.com"); // use your own email address here

       String subject = "Confirmation de la soumission de votre proposition";
       Content content = new Content("text/html", "<p>Votre proposition a été bien reçue par nos administrateurs, elle sera traitée dans les plus brefs délais.</p>");

       Mail mail = new Mail(from, subject, to, content);

       SendGrid sg = new SendGrid("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");
       Request request = new Request();

       request.setMethod(Method.POST);
       request.setEndpoint("mail/send");
       request.setBody(mail.build());

       Response response = sg.api(request);

       System.out.println(response.getStatusCode());
       System.out.println(response.getHeaders());
       System.out.println(response.getBody());
    }

    public Proposition getProposition() {
        return proposition;
    }

    public void setProposition(Proposition proposition) {
        this.proposition = proposition;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
    
    public List<PropositionDao> getPropositions() {
        return propositions;
    }

    public void setPropositions(List<PropositionDao> propositions) {
        this.propositions = propositions;
    }
    
    
}
