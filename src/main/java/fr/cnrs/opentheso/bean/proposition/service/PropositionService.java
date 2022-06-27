package fr.cnrs.opentheso.bean.proposition.service;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.bean.proposition.TraductionPropBean;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDao;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDetailDao;
import fr.cnrs.opentheso.bean.proposition.helper.PropositionDetailHelper;
import fr.cnrs.opentheso.bean.proposition.helper.PropositionHelper;
import fr.cnrs.opentheso.bean.proposition.model.Proposition;
import fr.cnrs.opentheso.bean.proposition.model.PropositionActionEnum;
import fr.cnrs.opentheso.bean.proposition.model.PropositionCategoryEnum;
import fr.cnrs.opentheso.bean.proposition.model.PropositionStatusEnum;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

@SessionScoped
public class PropositionService implements Serializable {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    @Inject
    private Tree tree;

    @Inject
    private Connect connect;

    @Inject
    private ConceptView conceptView;

    @Inject
    private CurrentUser currentUser;

    @Inject
    private IndexSetting indexSetting;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private RoleOnThesoBean roleOnThesoBean;

    public void envoyerProposition(Proposition proposition, String nom, String email, String commentaire) {

        if (new PropositionHelper().searchPropositionByEmailAndConceptAndLang(connect.getPoolConnexion(), email,
                proposition.getConceptID(), selectedTheso.getCurrentLang()) != null) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous avez déjà une proposition pour le même concept en cours de etraitement !");
            return;
        }

        PropositionDao propositionDao = new PropositionDao();
        propositionDao.setNom(nom);
        propositionDao.setEmail(email);
        propositionDao.setCommentaire(commentaire);
        propositionDao.setDatePublication(DATE_FORMAT.format(new Date()));
        propositionDao.setIdConcept(proposition.getConceptID());
        propositionDao.setIdTheso(selectedTheso.getCurrentIdTheso());
        propositionDao.setLang(selectedTheso.getCurrentLang());
        propositionDao.setStatus(PropositionStatusEnum.ENVOYER.name());
        int propositionID = new PropositionHelper().createNewProposition(connect.getPoolConnexion(), propositionDao);

        if (propositionID == -1) {
            showMessage(FacesMessage.SEVERITY_WARN, "Erreur pendant l'enregistrement de la proposition !");
            return;
        }

        if (StringUtils.isNotEmpty(proposition.getNomConceptProp())) {
            PropositionDetailDao propositionDetail = new PropositionDetailDao();
            propositionDetail.setIdProposition(propositionID);
            propositionDetail.setAction(PropositionActionEnum.UPDATE.name());
            propositionDetail.setCategorie(PropositionCategoryEnum.NOM.name());
            propositionDetail.setLang(selectedTheso.getCurrentLang());
            propositionDetail.setValue(proposition.getNomConceptProp());
            new PropositionDetailHelper().createNewPropositionDetail(connect.getPoolConnexion(), propositionDetail);
        }

        if (!CollectionUtils.isEmpty(proposition.getSynonymsProp())) {
            for (SynonymPropBean synonymProp : proposition.getSynonymsProp()) {
                if (synonymProp.isToAdd() || synonymProp.isToUpdate() || synonymProp.isToRemove()) {
                    PropositionActionEnum action;
                    if (synonymProp.isToAdd()) {
                        action = PropositionActionEnum.ADD;
                    } else if (synonymProp.isToRemove()) {
                        action = PropositionActionEnum.DELETE;
                    } else {
                        action = PropositionActionEnum.UPDATE;
                    }

                    PropositionDetailDao propositionDetail = new PropositionDetailDao();
                    propositionDetail.setIdProposition(propositionID);
                    propositionDetail.setAction(action.name());
                    propositionDetail.setCategorie(PropositionCategoryEnum.SYNONYME.name());
                    propositionDetail.setLang(synonymProp.getLang());
                    propositionDetail.setValue(synonymProp.getLexical_value());
                    propositionDetail.setOldValue(synonymProp.getOldValue());
                    propositionDetail.setStatus(synonymProp.getStatus());
                    propositionDetail.setHiden(synonymProp.isHiden());
                    propositionDetail.setIdTerm(synonymProp.getIdTerm());
                    new PropositionDetailHelper().createNewPropositionDetail(connect.getPoolConnexion(), propositionDetail);
                }
            }
        }

        if (!CollectionUtils.isEmpty(proposition.getTraductionsProp())) {
            for (TraductionPropBean traductionProp : proposition.getTraductionsProp()) {
                if (traductionProp.isToAdd() || traductionProp.isToUpdate() || traductionProp.isToRemove()) {
                    PropositionDetailDao propositionDetail = new PropositionDetailDao();
                    PropositionActionEnum action;
                    if (traductionProp.isToAdd()) {
                        action = PropositionActionEnum.ADD;
                        propositionDetail.setOldValue(traductionProp.getLexicalValue());
                    } else if (traductionProp.isToRemove()) {
                        action = PropositionActionEnum.DELETE;
                        propositionDetail.setOldValue(traductionProp.getOldValue());
                    } else {
                        action = PropositionActionEnum.UPDATE;
                        propositionDetail.setOldValue(traductionProp.getOldValue());
                    }

                    propositionDetail.setIdProposition(propositionID);
                    propositionDetail.setAction(action.name());
                    propositionDetail.setCategorie(PropositionCategoryEnum.TRADUCTION.name());
                    propositionDetail.setLang(traductionProp.getLang());
                    propositionDetail.setValue(traductionProp.getLexicalValue());
                    propositionDetail.setIdTerm(traductionProp.getIdTerm());
                    new PropositionDetailHelper().createNewPropositionDetail(connect.getPoolConnexion(), propositionDetail);
                }
            }
        }
        /*
        try {
            sendRecapEmail();
        } catch (IOException ex) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur detectée pendant l'envoie du mail de notification!");
        }*/
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

    public void refuserProposition(PropositionDao propositionSelected) {

        new PropositionHelper().updateStatusProposition(connect.getPoolConnexion(),
                PropositionStatusEnum.REFUSER.name(),
                currentUser.getNodeUser().getName(),
                DATE_FORMAT.format(new Date()),
                propositionSelected.getId());
    }

    public void supprimerPropostion(PropositionDao propositionSelected) {

        new PropositionDetailHelper().supprimerPropositionDetails(connect.getPoolConnexion(), propositionSelected.getId());
        new PropositionHelper().supprimerProposition(connect.getPoolConnexion(), propositionSelected.getId());
    }

    public void insertProposition(Proposition proposition, PropositionDao propositionSelected) throws IOException {

        TermHelper termHelper = new TermHelper();

        if (proposition.isUpdateNomConcept()) {
            Term term = termHelper.getThisTerm(connect.getPoolConnexion(),
                    propositionSelected.getIdConcept(),
                    propositionSelected.getIdTheso(),
                    propositionSelected.getLang());
            term.setLexical_value(proposition.getNomConceptProp());
            termHelper.updateTermTraduction(connect.getPoolConnexion(), term, currentUser.getNodeUser().getIdUser());

            tree.reset();
            tree.initialise(propositionSelected.getIdTheso(), propositionSelected.getLang());
            roleOnThesoBean.initNodePref(propositionSelected.getIdTheso());
            selectedTheso.setSelectedIdTheso(propositionSelected.getIdTheso());
            selectedTheso.setSelectedLang(propositionSelected.getLang());
            selectedTheso.setSelectedThesoForSearch();
            indexSetting.setIsSelectedTheso(true);
        }

        if (CollectionUtils.isNotEmpty(proposition.getSynonymsProp())) {
            for (SynonymPropBean synonymPropBean : proposition.getSynonymsProp()) {
                if (synonymPropBean.isToAdd()) {

                    String idTerm = new TermHelper().getIdTermOfConcept(
                            connect.getPoolConnexion(),
                            propositionSelected.getIdConcept(),
                            propositionSelected.getIdTheso());

                    if (!termHelper.addNonPreferredTerm(connect.getPoolConnexion(),
                            idTerm,
                            synonymPropBean.getLexical_value(),
                            synonymPropBean.getLang(),
                            selectedTheso.getCurrentIdTheso(),
                            "",
                            synonymPropBean.isHiden() ? "Hidden" : "USE",
                            synonymPropBean.isHiden(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La création du nouveau synonyme a échoué !");
                        return;
                    }
                } else if (synonymPropBean.isToRemove()) {

                    if (!termHelper.deleteNonPreferedTerm(connect.getPoolConnexion(),
                            synonymPropBean.getIdTerm(),
                            synonymPropBean.getLang(),
                            synonymPropBean.getLexical_value(),
                            selectedTheso.getCurrentIdTheso(),
                            synonymPropBean.getStatus(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La suppression du synonyme a échoué !");
                        return;
                    }
                } else if (synonymPropBean.isToUpdate()) {

                    if (!termHelper.updateTermSynonyme(connect.getPoolConnexion(),
                            synonymPropBean.getOldValue(),
                            synonymPropBean.getLexical_value(),
                            synonymPropBean.getIdTerm(),
                            synonymPropBean.getLang(),
                            selectedTheso.getCurrentIdTheso(),
                            synonymPropBean.isHiden(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La modification du synonyme a échoué !");
                        return;
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(proposition.getTraductionsProp())) {
            for (TraductionPropBean traductionProp : proposition.getTraductionsProp()) {
                if (traductionProp.isToAdd()) {
                    if (!termHelper.addTraduction(connect.getPoolConnexion(),
                            traductionProp.getLexicalValue(),
                            traductionProp.getIdTerm(),
                            traductionProp.getLang(),
                            "", "",
                            selectedTheso.getCurrentIdTheso(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "L'ajout d'une traduction a échouée !");
                        return;
                    }
                } else if (traductionProp.isToRemove()) {
                    if (!new TermHelper().deleteTraductionOfTerm(connect.getPoolConnexion(),
                            traductionProp.getIdTerm(),
                            traductionProp.getLexicalValue(),
                            traductionProp.getLang(),
                            selectedTheso.getCurrentIdTheso(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La suppression d'une traduction a échouée !");
                        return;
                    }
                } else if (traductionProp.isToUpdate()) {
                    if (!termHelper.updateTraduction(connect.getPoolConnexion(),
                            traductionProp.getLexicalValue(),
                            traductionProp.getIdTerm(),
                            traductionProp.getLang(),
                            selectedTheso.getCurrentIdTheso(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La modification de la traduction "
                                + traductionProp.getLexicalValue() + " ("+traductionProp.getLang()
                                + ") a échouée !");
                        return;
                    }
                }
            }
        }

        new ConceptHelper().updateDateOfConcept(connect.getPoolConnexion(),
                propositionSelected.getIdTheso(),
                propositionSelected.getLang(),
                currentUser.getNodeUser().getIdUser());

        new PropositionHelper().updateStatusProposition(connect.getPoolConnexion(),
                PropositionStatusEnum.APPROUVER.name(),
                currentUser.getNodeUser().getName(),
                DATE_FORMAT.format(new Date()),
                propositionSelected.getId());

        conceptView.getConcept(propositionSelected.getIdTheso(), propositionSelected.getIdConcept(),
                propositionSelected.getLang());
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        FacesMessage msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public Proposition selectProposition(NodeConcept nodeConcept) {
        Proposition proposition = new Proposition();
        proposition.setConceptID(nodeConcept.getConcept().getIdConcept());
        proposition.setUpdateNomConcept(false);
        proposition.setNomConceptProp(null);
        proposition.setNomConcept(nodeConcept.getTerm());
        proposition.setSynonymsProp(toSynonymPropBean(nodeConcept.getNodeEM(),
                conceptView.getNodeConcept().getTerm().getId_term()));
        proposition.setTraductionsProp(toTraductionPropBeans(nodeConcept.getNodeTermTraductions(),
                nodeConcept.getTerm().getId_term()));
        return proposition;
    }

    public void preparerPropositionSelect(Proposition proposition, PropositionDao propositionDao) {
        List<PropositionDetailDao> propositionDetails = new PropositionDetailHelper()
                .getPropositionDetail(connect.getPoolConnexion(), propositionDao.getId());

        proposition = new Proposition();
        proposition.setConceptID(conceptView.getNodeConcept().getConcept().getIdConcept());
        proposition.setNomConceptProp(null);
        proposition.setNomConcept(conceptView.getNodeConcept().getTerm());

        proposition.setSynonymsProp(toSynonymPropBean(conceptView.getNodeConcept().getNodeEM(),
                conceptView.getNodeConcept().getTerm().getId_term()));

        proposition.setTraductionsProp(toTraductionPropBeans(conceptView.getNodeConcept().getNodeTermTraductions(),
                conceptView.getNodeConcept().getTerm().getId_term()));

        for (PropositionDetailDao propositionDetailDao : propositionDetails) {
            if (PropositionCategoryEnum.NOM.name().equals(propositionDetailDao.getCategorie())) {
                proposition.setNomConceptProp(propositionDetailDao.getValue());
            } else if (PropositionCategoryEnum.SYNONYME.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    SynonymPropBean propAddSyn = new SynonymPropBean();
                    propAddSyn.setLexical_value(propositionDetailDao.getValue());
                    propAddSyn.setToAdd(true);
                    propAddSyn.setIdUser(currentUser.getNodeUser().getIdUser() + "");
                    propAddSyn.setLang(propositionDetailDao.getLang());
                    propAddSyn.setStatus(propositionDetailDao.getStatus());
                    propAddSyn.setIdTerm(propositionDetailDao.getIdTerm());
                    propAddSyn.setHiden(propositionDetailDao.isHiden());
                    proposition.getSynonymsProp().add(propAddSyn);
                }

                for (int i = 0; i < proposition.getSynonymsProp().size(); i++) {

                    if (proposition.getSynonymsProp().get(i).getOldValue() != null
                            && proposition.getSynonymsProp().get(i).getOldValue().equals(propositionDetailDao.getOldValue())) {
                        proposition.getSynonymsProp().get(i).setLexical_value(propositionDetailDao.getValue());
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getSynonymsProp().get(i).setToUpdate(true);
                        } else if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                            proposition.getSynonymsProp().get(i).setToAdd(true);
                        } else {
                            proposition.getSynonymsProp().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.TRADUCTION.name().equals(propositionDetailDao.getCategorie())) {
                
                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    TraductionPropBean traductionPropBean = new TraductionPropBean();
                    traductionPropBean.setLexicalValue(propositionDetailDao.getValue());
                    traductionPropBean.setToAdd(true);
                    traductionPropBean.setLang(propositionDetailDao.getLang());
                    traductionPropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.getTraductionsProp().add(traductionPropBean);
                }

                for (int i = 0; i < proposition.getTraductionsProp().size(); i++) {
                    if (proposition.getTraductionsProp().get(i).getOldValue() != null
                            && proposition.getTraductionsProp().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getTraductionsProp().get(i).setToUpdate(true);
                        } else if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                            proposition.getTraductionsProp().get(i).setToAdd(true);
                        } else {
                            proposition.getTraductionsProp().get(i).setToRemove(true);
                        }
                    }
                }
            }
        }

        if (propositionDao.getStatus().equals(PropositionStatusEnum.ENVOYER.name())) {
            new PropositionHelper().setLuStatusProposition(connect.getPoolConnexion(), propositionDao.getId());
        }
    }

    private List<TraductionPropBean> toTraductionPropBeans(List<NodeTermTraduction> nodesTermTraductions, String idTerm) {
        List<TraductionPropBean> traduction = new ArrayList<>();
        for (NodeTermTraduction nodeTermTraduction : nodesTermTraductions) {
            TraductionPropBean traductionPropBean = new TraductionPropBean();
            traductionPropBean.setLang(nodeTermTraduction.getLang());
            traductionPropBean.setLexicalValue(nodeTermTraduction.getLexicalValue());
            traductionPropBean.setOldValue(nodeTermTraduction.getLexicalValue());
            traductionPropBean.setIdTerm(idTerm);
            traduction.add(traductionPropBean);
        }
        return traduction;
    }

    public List<SynonymPropBean> toSynonymPropBean(List<NodeEM> nodesEm, String idTerm) {
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
                synonymPropBean.setIdTerm(idTerm);
                synonyms.add(synonymPropBean);
            }
        }

        return synonyms;
    }

    public List<PropositionDao> searchAllPropositions() {
        return new PropositionHelper().getAllProposition(connect.getPoolConnexion());
    }

}
