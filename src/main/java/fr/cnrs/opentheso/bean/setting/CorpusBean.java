/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.bdd.helper.CorpusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "corpusBean")
@SessionScoped
public class CorpusBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private SelectedTheso selectedTheso;

    private ArrayList<NodeCorpus> nodeCorpuses;

    private String oldName;
    private NodeCorpus nodeCorpusForEdit;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeCorpuses != null) {
            nodeCorpuses.clear();
            nodeCorpuses = null;
        }
        oldName = null;
        nodeCorpusForEdit = null;
    }

    public CorpusBean() {
    }

    public void init() {
        // récupération des informations sur les corpus liés 
        CorpusHelper corpusHelper = new CorpusHelper();
        nodeCorpuses = corpusHelper.getAllCorpus(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso());
        nodeCorpusForEdit = null;
        oldName = null;
    }

    public void updateCorpus() {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (nodeCorpusForEdit == null) {
            return;
        }

        if (nodeCorpusForEdit.getCorpusName().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " le nom du corpus est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (nodeCorpusForEdit.getUriLink().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'URI du lien est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        CorpusHelper corpusHelper = new CorpusHelper();
        if (corpusHelper.isCorpusExist(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit.getCorpusName())) {
            if (!nodeCorpusForEdit.getCorpusName().equalsIgnoreCase(oldName)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " ce corpus existe déjà, changez de nom !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
                return;
            }
        }

        if (!corpusHelper.updateCorpus(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                oldName,
                nodeCorpusForEdit)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification de corpus !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Corpus modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('editCorpus').hide();");
        init();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public void addNewCorpus() {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (nodeCorpusForEdit == null) {
            return;
        }

        if (nodeCorpusForEdit.getCorpusName().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " le nom du corpus est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (nodeCorpusForEdit.getUriLink().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'URI du lien est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        CorpusHelper corpusHelper = new CorpusHelper();
        if (corpusHelper.isCorpusExist(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit.getCorpusName())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " ce corpus existe déjà, changez de nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (!corpusHelper.addNewCorpus(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeCorpusForEdit)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification de corpus !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Corpus modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('newCorpus').hide();");
        init();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public void deleteCorpus() {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (nodeCorpusForEdit == null) {
            return;
        }

        CorpusHelper corpusHelper = new CorpusHelper();
        if (!corpusHelper.deleteCorpus(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeCorpusForEdit.getCorpusName())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification de corpus !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Corpus modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('newCorpus').hide();");
        init();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public void setCorpusForEdit(NodeCorpus nodeCorpus) {
        nodeCorpusForEdit = nodeCorpus;
        oldName = nodeCorpus.getCorpusName();
    }

    public void setCorpusForNew() {
        nodeCorpusForEdit = new NodeCorpus();
    }

    public ArrayList<NodeCorpus> getNodeCorpuses() {
        return nodeCorpuses;
    }

    public void setNodeCorpuses(ArrayList<NodeCorpus> nodeCorpuses) {
        this.nodeCorpuses = nodeCorpuses;
    }

    public NodeCorpus getNodeCorpusForEdit() {
        return nodeCorpusForEdit;
    }

    public void setNodeCorpusForEdit(NodeCorpus nodeCorpusForEdit) {
        this.nodeCorpusForEdit = nodeCorpusForEdit;
    }

}
