package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.entites.PropositionModification;
import fr.cnrs.opentheso.entites.PropositionModificationDetail;
import fr.cnrs.opentheso.models.PropositionProjection;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.propositions.*;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.mail.MailBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.bean.proposition.TraductionPropBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.repositories.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Service;


@Service
public class PropositionService {

    private Tree tree;
    private ConceptView conceptView;
    private CurrentUser currentUser;
    private IndexSetting indexSetting;
    private SelectedTheso selectedTheso;
    private RoleOnThesoBean roleOnThesoBean;
    private MailBean mailBean;
    private ConceptDcTermRepository conceptDcTermRepository;
    private ThesaurusHelper thesaurusHelper;
    private ConceptHelper conceptHelper;
    private NoteHelper noteHelper;
    private TermHelper termHelper;
    private UserHelper userHelper;
    private PreferencesHelper preferencesHelper;
    private PropositionModificationRepository propositionModificationRepository;
    private PropositionModificationDetailRepository propositionModificationDetailRepository;


    public PropositionService(Tree tree, ConceptView conceptView, CurrentUser currentUser, IndexSetting indexSetting,
                              SelectedTheso selectedTheso, RoleOnThesoBean roleOnThesoBean, MailBean mailBean,
                              ConceptDcTermRepository conceptDcTermRepository, ThesaurusHelper thesaurusHelper,
                              ConceptHelper conceptHelper, NoteHelper noteHelper, TermHelper termHelper, UserHelper userHelper,
                              PreferencesHelper preferencesHelper, PropositionModificationRepository propositionModificationRepository,
                              PropositionModificationDetailRepository propositionModificationDetailRepository) {

        this.tree = tree;
        this.conceptView = conceptView;
        this.currentUser = currentUser;
        this.indexSetting = indexSetting;
        this.selectedTheso = selectedTheso;
        this.roleOnThesoBean = roleOnThesoBean;
        this.mailBean = mailBean;
        this.conceptDcTermRepository = conceptDcTermRepository;
        this.thesaurusHelper = thesaurusHelper;
        this.conceptHelper = conceptHelper;
        this.noteHelper = noteHelper;
        this.termHelper = termHelper;
        this.userHelper = userHelper;
        this.preferencesHelper = preferencesHelper;
        this.propositionModificationRepository = propositionModificationRepository;
        this.propositionModificationDetailRepository = propositionModificationDetailRepository;
    }

    public boolean envoyerProposition(Proposition proposition, String nom, String email, String commentaire) {

        if (propositionModificationRepository.findPropositionByEmailConceptLang(email, proposition.getConceptID(),
                selectedTheso.getCurrentLang()) != null) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous avez déjà une proposition pour le même concept en cours de etraitement !");
            return false;
        }

        var propositionModification = PropositionModification.builder()
                .nom(nom)
                .email(email)
                .commentaire(commentaire)
                .idConcept(proposition.getConceptID())
                .idTheso(selectedTheso.getCurrentIdTheso())
                .lang(selectedTheso.getCurrentLang())
                .status(PropositionStatusEnum.ENVOYER.name())
                .date(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()))
                .build();

        var thesaurusName = thesaurusHelper.getTitleOfThesaurus(selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        try {
            var subject = "[Opentheso] Confirmation de l'envoi de votre proposition";
            var contentFile = "<html><body>"
                    + "Cher(e) " + propositionModification.getNom() + ", <br/> "
                    + "<p> Votre proposition a bien été reçue par nos administrateurs, elle sera étudiée dans les plus brefs délais.<br/>"
                    + "Vous recevrez un mail dès que votre proposition sera traitée.<br/></p> "
                    + "Nous vous remercions de votre contribution à l'enrichissement du thésaurus <b>" + thesaurusName + "(" + propositionModification.getIdTheso() + ")" + "</b> "
                    + "(concept : <a href=\"" + getPath() + "/?idc=" + propositionModification.getIdConcept()
                    + "&idt=" + propositionModification.getIdTheso() + "\">" + proposition.getNomConcept().getLexicalValue() + "</a>). <br/><br/> Cordialement,<br/>"
                    + "L'équipe " + thesaurusName + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";
            sendEmail(propositionModification.getEmail(), subject, contentFile);
        } catch (Exception ex) {
            showMessage(FacesMessage.SEVERITY_WARN, "Erreur detectée pendant l'envoie du mail de notification! \nVotre propostion a été enregistrée !");
        }

        var propositionSaved = propositionModificationRepository.save(propositionModification);

        if (StringUtils.isNotEmpty(proposition.getNomConceptProp())) {
            propositionModificationDetailRepository.save(PropositionModificationDetail.builder()
                    .idProposition(propositionSaved.getId())
                    .action(PropositionActionEnum.UPDATE.name())
                    .categorie(PropositionCategoryEnum.NOM.name())
                    .lang(selectedTheso.getCurrentLang())
                    .value(proposition.getNomConceptProp())
                    .oldValue(proposition.getNomConcept().getLexicalValue())
                    .build());
        }

        if (!CollectionUtils.isEmpty(proposition.getSynonymsProp())) {
            for (SynonymPropBean synonymProp : proposition.getSynonymsProp()) {
                saveSynonyms(propositionSaved.getId(), synonymProp);
            }
        }

        if (!CollectionUtils.isEmpty(proposition.getTraductionsProp())) {
            for (var traductionProp : proposition.getTraductionsProp()) {
                if (traductionProp.isToAdd() || traductionProp.isToUpdate() || traductionProp.isToRemove()) {
                    var propositionDetail = new PropositionModificationDetail();

                    if (traductionProp.isToAdd()) {
                        propositionDetail.setAction(PropositionActionEnum.ADD.name());
                        propositionDetail.setOldValue(traductionProp.getLexicalValue());
                    } else if (traductionProp.isToRemove()) {
                        propositionDetail.setAction(PropositionActionEnum.DELETE.name());
                        propositionDetail.setOldValue(traductionProp.getOldValue());
                    } else {
                        propositionDetail.setAction(PropositionActionEnum.UPDATE.name());
                        propositionDetail.setOldValue(traductionProp.getOldValue());
                    }

                    propositionDetail.setIdProposition(propositionSaved.getId());
                    propositionDetail.setCategorie(PropositionCategoryEnum.TRADUCTION.name());
                    propositionDetail.setLang(traductionProp.getLang());
                    propositionDetail.setValue(traductionProp.getLexicalValue());
                    propositionDetail.setIdTerm(traductionProp.getIdTerm());
                    propositionModificationDetailRepository.save(propositionDetail);
                }
            }
        }

        if (proposition.getNote() != null) {
            noteManagement(propositionSaved.getId(), proposition.getNote(), PropositionCategoryEnum.NOTE.name());
        }

        if (proposition.getChangeNote() != null) {
            noteManagement(propositionSaved.getId(), proposition.getChangeNote(), PropositionCategoryEnum.CHANGE_NOTE.name());
        }

        if (proposition.getDefinition() != null) {
            noteManagement(propositionSaved.getId(), proposition.getDefinition(), PropositionCategoryEnum.DEFINITION.name());
        }

        if (proposition.getEditorialNote() != null) {
            noteManagement(propositionSaved.getId(), proposition.getEditorialNote(), PropositionCategoryEnum.EDITORIAL_NOTE.name());
        }

        if (proposition.getExample() != null) {
            noteManagement(propositionSaved.getId(), proposition.getExample(), PropositionCategoryEnum.EXAMPLE.name());
        }

        if (proposition.getHistoryNote() != null) {
            noteManagement(propositionSaved.getId(), proposition.getHistoryNote(), PropositionCategoryEnum.HISTORY.name());
        }

        if (proposition.getScopeNote() != null) {
            noteManagement(propositionSaved.getId(), proposition.getScopeNote(), PropositionCategoryEnum.SCOPE.name());
        }

        return true;
    }

    private void saveSynonyms(Integer propositionId, SynonymPropBean synonymProp) {
        if (synonymProp.isToAdd() || synonymProp.isToUpdate() || synonymProp.isToRemove()) {
            PropositionActionEnum action;
            if (synonymProp.isToAdd()) {
                action = PropositionActionEnum.ADD;
            } else if (synonymProp.isToRemove()) {
                action = PropositionActionEnum.DELETE;
            } else {
                action = PropositionActionEnum.UPDATE;
            }

            propositionModificationDetailRepository.save(PropositionModificationDetail.builder()
                    .idProposition(propositionId)
                    .action(action.name())
                    .categorie(PropositionCategoryEnum.SYNONYME.name())
                    .lang(synonymProp.getLang())
                    .value(synonymProp.getLexicalValue())
                    .oldValue(synonymProp.getOldValue())
                    .status(synonymProp.getStatus())
                    .hiden(synonymProp.isHiden())
                    .idTerm(synonymProp.getIdTerm())
                    .build());
        }
    }

    private String getPath() {
        if (FacesContext.getCurrentInstance() == null) {
            return "";
        }

        return FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin")
                + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
    }

    public int searchNbrNewProposition() {

        if(StringUtils.isEmpty(selectedTheso.getCurrentIdTheso()))
            return 0;

        var result = propositionModificationRepository.findAllByIdThesoAndStatus(selectedTheso.getCurrentIdTheso(),
                PropositionStatusEnum.ENVOYER.name());
        return CollectionUtils.isEmpty(result) ? 0 : result.size();
    }

    public boolean sendEmail(String emailDestination, String subject, String contentFile) throws IOException {
        //return true;
        if (currentUser.getNodeUser() != null) {
            if(!currentUser.getNodeUser().getMail().equalsIgnoreCase(emailDestination)) {
                return mailBean.sendMail(emailDestination, subject, contentFile);
            }
            if (currentUser.getNodeUser().isAlertMail()) {
                return mailBean.sendMail(emailDestination, subject, contentFile);
            } else {
                return true;
            }
        }
        return mailBean.sendMail(emailDestination, subject, contentFile);
    }

    public void refuserProposition(PropositionDao propositionSelected, String commentaireAdmin) {

        var propositionSaved = propositionModificationRepository.findById(propositionSelected.getId());
        if (propositionSaved.isPresent()) {
            propositionSaved.get().setStatus(PropositionStatusEnum.REFUSER.name());
            propositionSaved.get().setApprouvePar(currentUser.getNodeUser().getName());
            propositionSaved.get().setDate(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
            propositionSaved.get().setAdminComment(commentaireAdmin);
            propositionModificationRepository.save(propositionSaved.get());
        }

        try {
            var subject = "[Opentheso] Résultat de votre proposition";
            var contentFile = "<html><body>"
                    + "Cher(e) " + propositionSelected.getNom() + ", <br/> "
                    + "<p>Votre proposition d’enrichissement sur le concept " + propositionSelected.getNomConcept() + " du thésaurus " + propositionSelected.getThesoName()
                    + " a été refusée par les administrateurs.<br/>"
                    + "Voici leur message: <br/>"
                    + propositionSelected.getAdminComment() + "<br/>"
                    + "N’hésitez pas à faire de nouvelles propositions, nous les étudierons avec attention. <b>"
                    + "<br/><br/> Cordialement,<br/>"
                    + "L'équipe " + propositionSelected.getThesoName()
                    + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            sendEmail(propositionSelected.getEmail(), subject, contentFile);
        } catch (IOException ex) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur detectée pendant l'envoie du mail de notification!");
        }
    }

    public void supprimerPropostion(PropositionDao propositionSelected) {

        propositionModificationDetailRepository.deleteById(propositionSelected.getId());
        propositionModificationRepository.deleteById(propositionSelected.getId());
    }

    public void insertProposition(Proposition proposition, PropositionDao propositionSelected,
                                  String commentaireAdmin, boolean prefTermeAccepted, boolean varianteAccepted, boolean traductionAccepted,
                                  boolean noteAccepted, boolean definitionAccepted, boolean changeNoteAccepted, boolean scopeAccepted,
                                  boolean editorialNotesAccepted, boolean examplesAccepted, boolean historyAccepted) throws IOException {

        if (prefTermeAccepted && proposition.isUpdateNomConcept()) {
            Term term = termHelper.getThisTerm(propositionSelected.getIdConcept(), propositionSelected.getIdTheso(),
                    propositionSelected.getLang());

            term.setLexicalValue(proposition.getNomConceptProp());
            termHelper.updateTermTraduction(term, currentUser.getNodeUser().getIdUser());

            tree.reset();
            tree.initialise(propositionSelected.getIdTheso(), propositionSelected.getLang());
            roleOnThesoBean.initNodePref(propositionSelected.getIdTheso());
            selectedTheso.setSelectedIdTheso(propositionSelected.getIdTheso());
            selectedTheso.setSelectedLang(propositionSelected.getLang());
            selectedTheso.setSelectedThesoForSearch();
            indexSetting.setIsSelectedTheso(true);
        }

        if (varianteAccepted && CollectionUtils.isNotEmpty(proposition.getSynonymsProp())) {
            for (SynonymPropBean synonymPropBean : proposition.getSynonymsProp()) {
                if (synonymPropBean.isToAdd()) {

                    String idTerm = termHelper.getIdTermOfConcept(propositionSelected.getIdConcept(), propositionSelected.getIdTheso());

                    if (!termHelper.addNonPreferredTerm(idTerm, synonymPropBean.getLexicalValue(), synonymPropBean.getLang().toLowerCase(),
                            selectedTheso.getCurrentIdTheso(), "", synonymPropBean.isHiden() ? "Hidden" : "USE",
                            synonymPropBean.isHiden(), currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La création du nouveau synonyme a échoué !");
                        return;
                    }
                } else if (synonymPropBean.isToRemove()) {

                    if (!termHelper.deleteNonPreferedTerm(synonymPropBean.getIdTerm(), synonymPropBean.getLang().toLowerCase(),
                            synonymPropBean.getLexicalValue(), selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La suppression du synonyme a échoué !");
                        return;
                    }
                } else if (synonymPropBean.isToUpdate()) {

                    if (!termHelper.updateTermSynonyme(synonymPropBean.getOldValue(), synonymPropBean.getLexicalValue(),
                            synonymPropBean.getIdTerm(), synonymPropBean.getLang().toLowerCase(),
                            selectedTheso.getCurrentIdTheso(), synonymPropBean.isHiden(), currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La modification du synonyme a échoué !");
                        return;
                    }
                }
            }
        }

        if (traductionAccepted && CollectionUtils.isNotEmpty(proposition.getTraductionsProp())) {
            for (TraductionPropBean traductionProp : proposition.getTraductionsProp()) {
                if (traductionProp.isToAdd()) {
                    if (!termHelper.addTraduction(traductionProp.getLexicalValue(), traductionProp.getIdTerm(),
                            traductionProp.getLang(), "", "", selectedTheso.getCurrentIdTheso(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "L'ajout d'une traduction a échouée !");
                        return;
                    }
                } else if (traductionProp.isToRemove()) {
                    if (!termHelper.deleteTraductionOfTerm(traductionProp.getIdTerm(), traductionProp.getLexicalValue(),
                            traductionProp.getLang(), selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La suppression d'une traduction a échouée !");
                        return;
                    }
                } else if (traductionProp.isToUpdate()) {
                    if (!termHelper.updateTraduction(traductionProp.getLexicalValue(), traductionProp.getIdTerm(),
                            traductionProp.getLang(), selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La modification de la traduction "
                                + traductionProp.getLexicalValue() + " (" + traductionProp.getLang()
                                + ") a échouée !");
                        return;
                    }
                }
            }
        }

        if (noteAccepted && proposition.getNote() != null) {
            if (proposition.getNote().isToAdd()) {
                addNewNote(proposition.getNote(), "note");
            } else if (proposition.getNote().isToUpdate()) {
                updateNote(proposition.getNote());
            } else if (proposition.getNote().isToRemove()) {
                deleteNote(proposition.getNote());
            }

        }

        if (changeNoteAccepted && proposition.getChangeNote() != null) {
            if (proposition.getChangeNote().isToAdd()) {
                addNewNote(proposition.getChangeNote(), "changeNote");
            } else if (proposition.getChangeNote().isToUpdate()) {
                updateNote(proposition.getChangeNote());
            } else if (proposition.getChangeNote().isToRemove()) {
                deleteNote(proposition.getChangeNote());
            }
        }

        if (definitionAccepted && proposition.getDefinition() != null) {
            if (proposition.getDefinition().isToAdd()) {
                addNewNote(proposition.getDefinition(), "definition");
            } else if (proposition.getDefinition().isToUpdate()) {
                updateNote(proposition.getDefinition());
            } else if (proposition.getDefinition().isToRemove()) {
                deleteNote(proposition.getDefinition());
            }
        }

        if (editorialNotesAccepted && proposition.getEditorialNote() != null) {
            if (proposition.getEditorialNote().isToAdd()) {
                addNewNote(proposition.getEditorialNote(), "editorialNote");
            } else if (proposition.getEditorialNote().isToUpdate()) {
                updateNote(proposition.getEditorialNote());
            } else if (proposition.getEditorialNote().isToRemove()) {
                deleteNote(proposition.getEditorialNote());
            }
        }

        if (examplesAccepted && proposition.getExample() != null) {
            if (proposition.getExample().isToAdd()) {
                addNewNote(proposition.getExample(), "example");
            } else if (proposition.getExample().isToUpdate()) {
                updateNote(proposition.getExample());
            } else if (proposition.getExample().isToRemove()) {
                deleteNote(proposition.getExample());
            }

        }

        if (scopeAccepted && proposition.getScopeNote() != null) {
            if (proposition.getScopeNote().isToAdd()) {
                addNewNote(proposition.getScopeNote(), "scopeNote");
            } else if (proposition.getScopeNote().isToUpdate()) {
                updateNote(proposition.getScopeNote());
            } else if (proposition.getScopeNote().isToRemove()) {
                deleteNote(proposition.getScopeNote());
            }
        }

        if (historyAccepted && proposition.getHistoryNote() != null) {
            if (proposition.getHistoryNote().isToAdd()) {
                addNewNote(proposition.getHistoryNote(), "historyNote");
            } else if (proposition.getHistoryNote().isToUpdate()) {
                updateNote(proposition.getHistoryNote());
            } else if (proposition.getHistoryNote().isToRemove()) {
                deleteNote(proposition.getHistoryNote());
            }
        }

        conceptHelper.updateDateOfConcept(propositionSelected.getIdTheso(), propositionSelected.getLang(), currentUser.getNodeUser().getIdUser());

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(propositionSelected.getIdConcept())
                .idThesaurus(propositionSelected.getIdTheso())
                .build());

        var propositionSaved = propositionModificationRepository.findById(propositionSelected.getId());
        if (propositionSaved.isPresent()) {
            propositionSaved.get().setStatus(PropositionStatusEnum.APPROUVER.name());
            propositionSaved.get().setApprouvePar(currentUser.getNodeUser().getName());
            propositionSaved.get().setDate(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
            propositionSaved.get().setAdminComment(commentaireAdmin);
            propositionModificationRepository.save(propositionSaved.get());
        }

        conceptView.getConcept(propositionSelected.getIdTheso(), propositionSelected.getIdConcept(), propositionSelected.getLang(), currentUser);

        try {
            String subject = "[Opentheso] Résultat de votre proposition";
            String contentFile = "<html><body>"
                    + "Cher(e) " + propositionSelected.getNom() + ", <br/> "
                    + "<p>Votre proposition d'enrichissement sur le concept " + propositionSelected.getNomConcept()
                    + " du thésaurus " + propositionSelected.getThesoName()
                    + " a été acceptée par les administrateurs.<br/>"
                    + " voici leur message : "
                    + propositionSelected.getAdminComment()
                    + "<br/>"
                    + "N’hésitez pas à faire de nouvelles propositions, nous les étudierons avec attention."
                    + "<br/><br/> Cordialement,<br/>"
                    + "L'équipe " + propositionSelected.getThesoName() + ".<br/> <img src=\"" + getPath()
                    + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            sendEmail(propositionSelected.getEmail(), subject, contentFile);
        } catch (IOException ex) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur detectée pendant l'envoie du mail de notification!");
        }
    }

    private void deleteNote(NotePropBean notePropBean) {
        if (!noteHelper.deleteThisNote(notePropBean.getIdNote(), notePropBean.getIdConcept(), notePropBean.getLang(),
                selectedTheso.getCurrentIdTheso(), notePropBean.getNoteTypeCode(), notePropBean.getLexicalValue(),
                currentUser.getNodeUser().getIdUser())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    private void updateNote(NotePropBean notePropBean) {
        if (!noteHelper.updateNote(notePropBean.getIdNote(), notePropBean.getIdConcept(), notePropBean.getLang(),
                selectedTheso.getCurrentIdTheso(), notePropBean.getLexicalValue(), notePropBean.getNoteSource(),
                notePropBean.getNoteTypeCode(), currentUser.getNodeUser().getIdUser())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    private void addNewNote(NotePropBean notePropBean, String typeNote) {
        if (!noteHelper.addNote(conceptView.getNodeConcept().getConcept().getIdConcept(), notePropBean.getLang(),
                selectedTheso.getCurrentIdTheso(), notePropBean.getLexicalValue(), typeNote, "", currentUser.getNodeUser().getIdUser())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Erreur pendant l'ajout d'une nouvelle note !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public boolean updateNomConcept(String newConceptName) {

        // vérification si le term à ajouter existe déjà, s oui, on a l'Id, sinon, on a Null
        String idTerm = termHelper.isTermEqualTo(newConceptName, selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());

        if (idTerm != null) {
            String label = termHelper.getLexicalValue(idTerm, selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", label + " : existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }

        if (termHelper.isAltLabelExist(idTerm, selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " un synonyme existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
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
                conceptView.getNodeConcept().getTerm().getIdTerm()));
        proposition.setTraductionsProp(toTraductionPropBeans(nodeConcept.getNodeTermTraductions(),
                nodeConcept.getTerm().getIdTerm()));

        for (NodeNote nodeNote : nodeConcept.getNodeNotes()) {
            switch (nodeNote.getNoteTypeCode()) {
                case "note":
                    proposition.setNote(toNotePropBean(nodeNote));
                    break;
                case "scopeNote":
                    proposition.setScopeNote(toNotePropBean(nodeNote));
                    break;
                case "changeNote":
                    proposition.setChangeNote(toNotePropBean(nodeNote));
                    break;
                case "definition":
                    proposition.setDefinition(toNotePropBean(nodeNote));
                    break;
                case "editorialNote":
                    proposition.setEditorialNote(toNotePropBean(nodeNote));
                    break;
                case "example":
                    proposition.setExample(toNotePropBean(nodeNote));
                    break;
                case "historyNote":
                    proposition.setHistoryNote(toNotePropBean(nodeNote));
                    break;
            }
        }

        return proposition;
    }

    private NotePropBean toNotePropBean(NodeNote nodeNote) {
        NotePropBean notePropBean = new NotePropBean();
        notePropBean.setModified(nodeNote.getModified());
        notePropBean.setCreated(nodeNote.getCreated());
        notePropBean.setIdUser(nodeNote.getIdUser());
        notePropBean.setIdConcept(nodeNote.getIdConcept());
        notePropBean.setIdNote(nodeNote.getIdNote());
        notePropBean.setIdTerm(nodeNote.getIdTerm());
        notePropBean.setLang(nodeNote.getLang());
        notePropBean.setLexicalValue(nodeNote.getLexicalValue());
        notePropBean.setOldValue(nodeNote.getLexicalValue());
        notePropBean.setNoteTypeCode(nodeNote.getNoteTypeCode());
        notePropBean.setUser(nodeNote.getUser());
        return notePropBean;
    }

    public void preparerPropositionSelect(Proposition proposition, PropositionDao propositionDao) {

        var propositionDetails = propositionModificationDetailRepository.findAllByIdProposition(propositionDao.getId());

        proposition.setConceptID(conceptView.getNodeConcept().getConcept().getIdConcept());
        proposition.setNomConceptProp(null);
        proposition.setNomConcept(conceptView.getNodeConcept().getTerm());

        proposition.setSynonymsProp(toSynonymPropBean(conceptView.getNodeConcept().getNodeEM(),
                conceptView.getNodeConcept().getTerm().getIdTerm()));

        proposition.setTraductionsProp(toTraductionPropBeans(conceptView.getNodeConcept().getNodeTermTraductions(),
                conceptView.getNodeConcept().getTerm().getIdTerm()));

        for (NodeNote nodeNote : conceptView.getNodeConcept().getNodeNotes()) {
            switch (nodeNote.getNoteTypeCode()) {
                case "note":
                    proposition.setNote(toNotePropBean(nodeNote));
                    break;
                case "scopeNote":
                    proposition.setScopeNote(toNotePropBean(nodeNote));
                    break;
                case "changeNote":
                    proposition.setChangeNote(toNotePropBean(nodeNote));
                    break;
                case "definition":
                    proposition.setDefinition(toNotePropBean(nodeNote));
                    break;
                case "editorialNote":
                    proposition.setEditorialNote(toNotePropBean(nodeNote));
                    break;
                case "example":
                    proposition.setExample(toNotePropBean(nodeNote));
                    break;
                case "historyNote":
                    proposition.setHistoryNote(toNotePropBean(nodeNote));
                    break;
            }
        }

        for (var propositionDetailDao : propositionDetails) {
            if (PropositionCategoryEnum.NOM.name().equals(propositionDetailDao.getCategorie())) {
                proposition.setUpdateNomConcept(true);
                proposition.setNomConceptProp(propositionDetailDao.getValue());
            } else if (PropositionCategoryEnum.SYNONYME.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    SynonymPropBean propAddSyn = new SynonymPropBean();
                    propAddSyn.setLexicalValue(propositionDetailDao.getValue());
                    propAddSyn.setToAdd(true);
                    propAddSyn.setIdUser(currentUser.getNodeUser().getIdUser() + "");
                    propAddSyn.setLang(propositionDetailDao.getLang());
                    propAddSyn.setStatus(propositionDetailDao.getStatus());
                    propAddSyn.setIdTerm(propositionDetailDao.getIdTerm());
                    propAddSyn.setHiden(propositionDetailDao.isHiden());
                    proposition.getSynonymsProp().add(propAddSyn);
                }

                for (int i = 0; i < proposition.getSynonymsProp().size(); i++) {
                    if (proposition.getSynonymsProp().get(i).getLexicalValue().equals(propositionDetailDao.getValue())) {
                        if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getSynonymsProp().get(i).setToRemove(true);
                        } else if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                            proposition.getSynonymsProp().get(i).setToAdd(true);
                        } else {
                            proposition.getSynonymsProp().get(i).setToUpdate(false);
                            proposition.getSynonymsProp().get(i).setToRemove(false);
                            proposition.getSynonymsProp().get(i).setToAdd(false);
                        }
                    } else if (proposition.getSynonymsProp().get(i).getLexicalValue().equals(propositionDetailDao.getOldValue())
                            && proposition.getSynonymsProp().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        proposition.getSynonymsProp().get(i).setToUpdate(true);
                        proposition.getSynonymsProp().get(i).setLexicalValue(propositionDetailDao.getValue());
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
                            proposition.getTraductionsProp().get(i).setLexicalValue(propositionDetailDao.getValue());
                        } else {
                            proposition.getTraductionsProp().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.NOTE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalValue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.setNote(notePropBean);
                }

                if (proposition.getNote().getOldValue() != null
                        && proposition.getNote().getOldValue().equals(propositionDetailDao.getOldValue())) {
                    if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getNote().setToUpdate(true);
                        proposition.getNote().setLexicalValue(propositionDetailDao.getValue());
                    } else if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getNote().setToRemove(true);
                    }
                }

            } else if (PropositionCategoryEnum.CHANGE_NOTE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalValue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.setChangeNote(notePropBean);
                }

                if (proposition.getChangeNote().getOldValue() != null
                        && proposition.getChangeNote().getOldValue().equals(propositionDetailDao.getOldValue())) {
                    if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getChangeNote().setToUpdate(true);
                        proposition.getChangeNote().setLexicalValue(propositionDetailDao.getValue());
                    } else if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getChangeNote().setToRemove(true);
                    }
                }

            } else if (PropositionCategoryEnum.DEFINITION.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalValue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.setDefinition(notePropBean);
                }

                if (proposition.getDefinition().getOldValue() != null
                        && proposition.getDefinition().getOldValue().equals(propositionDetailDao.getOldValue())) {
                    if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getDefinition().setToUpdate(true);
                        proposition.getDefinition().setLexicalValue(propositionDetailDao.getValue());
                    } else if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getDefinition().setToRemove(true);
                    }
                }

            } else if (PropositionCategoryEnum.EDITORIAL_NOTE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalValue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.setEditorialNote(notePropBean);
                }

                if (proposition.getEditorialNote().getOldValue() != null
                        && proposition.getEditorialNote().getOldValue().equals(propositionDetailDao.getOldValue())) {
                    if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getEditorialNote().setToUpdate(true);
                        proposition.getEditorialNote().setLexicalValue(propositionDetailDao.getValue());
                    } else if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getEditorialNote().setToRemove(true);
                    }
                }

            } else if (PropositionCategoryEnum.HISTORY.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalValue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.setHistoryNote(notePropBean);
                }

                if (proposition.getHistoryNote().getOldValue() != null
                        && proposition.getHistoryNote().getOldValue().equals(propositionDetailDao.getOldValue())) {
                    if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getHistoryNote().setToUpdate(true);
                        proposition.getHistoryNote().setLexicalValue(propositionDetailDao.getValue());
                    } else if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getHistoryNote().setToRemove(true);
                    }
                }

            } else if (PropositionCategoryEnum.SCOPE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalValue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.setScopeNote(notePropBean);
                }

                if (proposition.getScopeNote().getOldValue() != null
                        && proposition.getScopeNote().getOldValue().equals(propositionDetailDao.getOldValue())) {
                    if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getScopeNote().setToUpdate(true);
                        proposition.getScopeNote().setLexicalValue(propositionDetailDao.getValue());
                    } else if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getScopeNote().setToRemove(true);
                    }
                }

            } else if (PropositionCategoryEnum.EXAMPLE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalValue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.setExample(notePropBean);
                }

                if (proposition.getExample().getOldValue() != null
                        && proposition.getExample().getOldValue().equals(propositionDetailDao.getOldValue())) {
                    if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getExample().setToUpdate(true);
                        proposition.getExample().setLexicalValue(propositionDetailDao.getValue());
                        proposition.getExample().setOldValue(propositionDetailDao.getOldValue());
                    } else if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                        proposition.getExample().setToRemove(true);
                    }
                }

            }
        }

        if (propositionDao.getStatus().equals(PropositionStatusEnum.ENVOYER.name())) {
            propositionModificationRepository.updateStatus(propositionDao.getId(), PropositionStatusEnum.LU.name());
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
        List<SynonymPropBean> synonyms = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(nodesEm)) {
            for (NodeEM nodeEM : nodesEm) {
                SynonymPropBean synonymPropBean = new SynonymPropBean();
                synonymPropBean.setAction(nodeEM.getAction());
                synonymPropBean.setCreated(nodeEM.getCreated());
                synonymPropBean.setModified(nodeEM.getModified());
                synonymPropBean.setHiden(nodeEM.isHiden());
                synonymPropBean.setOldHiden(nodeEM.isHiden());
                synonymPropBean.setLang(nodeEM.getLang());
                synonymPropBean.setLexicalValue(nodeEM.getLexicalValue());
                synonymPropBean.setOldValue(nodeEM.getLexicalValue());
                synonymPropBean.setSource(nodeEM.getSource());
                synonymPropBean.setStatus(nodeEM.getStatus());
                synonymPropBean.setIdTerm(idTerm);
                synonyms.add(synonymPropBean);
            }
        }

        return synonyms;
    }

    public List<PropositionDao> searchAllPropositions(String idTheso) {
        List<PropositionDao> propositions = new ArrayList<>();
        propositions.addAll(searchPropositionsNonTraitee(idTheso));
        propositions.addAll(getAllPropositionByStatus(PropositionStatusEnum.APPROUVER.name(), idTheso));
        propositions.addAll(getAllPropositionByStatus(PropositionStatusEnum.REFUSER.name(), idTheso));
        return propositions;
    }

    public List<PropositionDao> searchPropositionsNonTraitee(String idTheso) {
        List<PropositionDao> propositions = new ArrayList<>();
        propositions.addAll(getAllPropositionByStatus(PropositionStatusEnum.ENVOYER.name(), idTheso));
        propositions.addAll(getAllPropositionByStatus(PropositionStatusEnum.LU.name(), idTheso));
        return propositions;
    }

    private List<PropositionDao> getAllPropositionByStatus(String status, String idTheso) {
        return propositionModificationRepository
                .findAllPropositionsByStatusAndTheso(status, idTheso)
                .stream()
                .map(this::mapToDao)
                .toList();
    }

    private PropositionDao mapToDao(PropositionProjection projection) {
        PropositionDao dao = new PropositionDao();
        dao.setId(projection.getId());
        dao.setIdConcept(projection.getIdConcept());
        dao.setLang(projection.getLang());
        dao.setIdTheso(projection.getIdTheso());
        dao.setStatus(projection.getStatus());
        dao.setDatePublication(projection.getDate());
        dao.setNom(projection.getNom());
        dao.setEmail(projection.getEmail());
        dao.setCommentaire(projection.getCommentaire());
        dao.setUserAction(projection.getApprouvePar());
        dao.setDateUpdate(projection.getApprouveDate());
        dao.setAdminComment(projection.getAdminComment());
        dao.setNomConcept(projection.getLexicalValue());
        dao.setCodeDrapeau(projection.getCodePays());
        return dao;
    }


    public void createProposition(PropositionFromApi proposition, int userId) {

        var user = userHelper.getUser(userId);
        var thesaurusLang = preferencesHelper.getWorkLanguageOfTheso(proposition.getIdTheso());

        int propositionId = propositionModificationRepository.save(PropositionModification.builder()
                        .nom(user.getName())
                        .email(user.getMail())
                        .commentaire(proposition.getCommentaire())
                        .date(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()))
                        .idConcept(proposition.getConceptID())
                        .idTheso(proposition.getIdTheso())
                        .lang(thesaurusLang)
                        .status(PropositionStatusEnum.ENVOYER.name())
                        .build())
                .getId();

        saveTerme(proposition, propositionId, thesaurusLang);

        if (CollectionUtils.isNotEmpty(proposition.getTraductionsProp())) {
            saveTraductions(proposition, propositionId, thesaurusLang);
        }

        if (!CollectionUtils.isEmpty(proposition.getSynonymsProp())) {
            saveSynonymes(proposition, propositionId);
        }

        if (CollectionUtils.isNotEmpty(proposition.getNotes())) {
            for (NotePropBean note : proposition.getNotes()) {
                noteManagement(propositionId, note, PropositionCategoryEnum.NOTE.name());
            }
        }

        if (CollectionUtils.isNotEmpty(proposition.getDefinitions())) {
            for (NotePropBean definition : proposition.getDefinitions()) {
                noteManagement(propositionId, definition, PropositionCategoryEnum.DEFINITION.name());
            }
        }
    }

    private void saveTerme(PropositionFromApi proposition,  Integer propositionId, String thesaurusLang) {
        var prefLabel = proposition.getTraductionsProp().stream()
                .filter(element -> element.getLang().equals(thesaurusLang))
                .findFirst();
        prefLabel.ifPresent(traductionPropBean -> propositionModificationDetailRepository.save(PropositionModificationDetail.builder()
                .idProposition(propositionId)
                .action(PropositionActionEnum.UPDATE.name())
                .categorie(PropositionCategoryEnum.NOM.name())
                .lang(traductionPropBean.getLang())
                .value(traductionPropBean.getLexicalValue())
                .oldValue(traductionPropBean.getOldValue())
                .build()));
    }

    private void saveTraductions(PropositionFromApi proposition, Integer propositionId,  String thesaurusLang) {

        var traduction = proposition.getTraductionsProp().stream().filter(element -> !element.getLang().equals(thesaurusLang)).findFirst();

        if (traduction.isPresent()) {
            if (traduction.get().isToAdd() || traduction.get().isToUpdate() || traduction.get().isToRemove()) {
                var propositionDetail = new PropositionModificationDetail();
                if (traduction.get().isToAdd()) {
                    propositionDetail.setAction(PropositionActionEnum.ADD.name());
                    propositionDetail.setOldValue(traduction.get().getLexicalValue());
                } else if (traduction.get().isToRemove()) {
                    propositionDetail.setAction(PropositionActionEnum.DELETE.name());
                    propositionDetail.setOldValue(traduction.get().getOldValue());
                } else {
                    propositionDetail.setAction(PropositionActionEnum.UPDATE.name());
                    propositionDetail.setOldValue(traduction.get().getOldValue());
                }

                var idTerm = termHelper.getIdTermOfConcept(proposition.getConceptID(), proposition.getIdTheso());
                if (StringUtils.isNotEmpty(idTerm)) {
                    propositionDetail.setIdProposition(propositionId);
                    propositionDetail.setCategorie(PropositionCategoryEnum.TRADUCTION.name());
                    propositionDetail.setLang(traduction.get().getLang());
                    propositionDetail.setValue(traduction.get().getLexicalValue());
                    propositionDetail.setIdTerm(idTerm);
                    propositionModificationDetailRepository.save(propositionDetail);
                }
            }
        }
    }

    private void saveSynonymes(PropositionFromApi proposition, Integer propositionId) {
        for (SynonymPropBean synonymProp : proposition.getSynonymsProp()) {
            saveSynonyms(propositionId, synonymProp);
        }
    }

    private void noteManagement(int propositionID, NotePropBean note, String category) {
        if (note.isToAdd() || note.isToUpdate() || note.isToRemove()) {
            var propositionDetail = new PropositionModificationDetail();
            if (note.isToAdd()) {
                propositionDetail.setAction(PropositionActionEnum.ADD.name());
                propositionDetail.setOldValue(note.getLexicalValue());
            } else if (note.isToRemove()) {
                propositionDetail.setAction(PropositionActionEnum.DELETE.name());
                propositionDetail.setOldValue(note.getOldValue());
            } else {
                propositionDetail.setAction(PropositionActionEnum.UPDATE.name());
                propositionDetail.setOldValue(note.getOldValue());
            }

            propositionDetail.setIdProposition(propositionID);
            propositionDetail.setCategorie(category);
            propositionDetail.setLang(note.getLang());
            propositionDetail.setValue(note.getLexicalValue());
            propositionDetail.setIdTerm(note.getIdTerm());
            propositionModificationDetailRepository.save(propositionDetail);
        }
    }

}
