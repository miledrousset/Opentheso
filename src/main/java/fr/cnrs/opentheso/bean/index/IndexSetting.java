/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.index;

import fr.cnrs.opentheso.bean.condidat.CandidatBean;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author miledrousset
 */
@Named(value = "indexSetting")
@SessionScoped
public class IndexSetting implements Serializable {

    @Inject
    private CandidatBean candidatBean;

    // si un thésaurus est sélectionné 
    private boolean isSelectedTheso;

    // si un profil est connecté
    private boolean isConnected;

    // si un concept est sélectionné
    private boolean isValueSelected;
    
    // si un thésaurus est sélectionné, on affiche les infos de Home
    private boolean isHomeSelected;    
    
    // si une facette est sélectionnée
    private boolean isFacetSelected;

    //private boolean isConceptDiagramSelected;

////// variables pour les vues du menu
    private boolean isThesoActive;
    private boolean isCandidateActive;
    private boolean isProfileActive;
    private boolean isSettingActive;
    private boolean isToolBoxActive;

    // variables pour les couleurs de boutons
    private String thesoColor;
    private String candidateColor;
    private String profileColor;
    private String settingColor;
    private String toolBoxColor;

    public IndexSetting() {
        isThesoActive = true;
        thesoColor = "white";
        candidateColor = "#B3DDC4";
        profileColor = "#B3DDC4";
        settingColor = "#B3DDC4";
        toolBoxColor = "#B3DDC4";
    }

    public void reset() {
        isThesoActive = true;
        resetColor();
        thesoColor = "white";
    }

    private void resetColor() {
        thesoColor = "#B3DDC4";
        candidateColor = "#B3DDC4";
        profileColor = "#B3DDC4";
        settingColor = "#B3DDC4";
        toolBoxColor = "#B3DDC4";
    }

    public boolean isIsSelectedTheso() {
        return isSelectedTheso;
    }

    public void setIsSelectedTheso(boolean isSelectedTheso) {
        this.isSelectedTheso = isSelectedTheso;

    }

    public boolean isIsConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isNotConnected() {
        return !isValueSelected & !isFacetSelected;
    }
    
    public boolean isIsThesoActive() {
        return isThesoActive;
    }

    public void setIsThesoActive(boolean isThesoActive) throws IOException {
        this.isThesoActive = isThesoActive;
        isCandidateActive = false;
        isProfileActive = false;
        isSettingActive = false;
        isToolBoxActive = false;
        resetColor();
        thesoColor = "white";
        
        clear();
    }

    public boolean isIsCandidateActive() {
        return isCandidateActive;
    }

    public void setIsCandidateActive(boolean isCandidateActive) throws IOException {
        candidatBean.initCandidatModule();
        this.isCandidateActive = isCandidateActive;
        isThesoActive = false;
        isProfileActive = false;
        isSettingActive = false;
        isToolBoxActive = false;
        resetColor();
        candidateColor = "white";
        
        clear();
    }

    public boolean isIsProfileActive() {
        return isProfileActive;
    }

    public void setIsProfileActive(boolean isProfileActive) throws IOException {
        this.isProfileActive = isProfileActive;
        isThesoActive = false;
        isCandidateActive = false;
        isSettingActive = false;
        isToolBoxActive = false;
        resetColor();
        profileColor = "white";
        
        clear();
    }

    public boolean isIsSettingActive() {
        return isSettingActive;
    }

    public void setIsSettingActive(boolean isSettingActive) throws IOException {
        this.isSettingActive = isSettingActive;
        isThesoActive = false;
        isCandidateActive = false;
        isProfileActive = false;
        isToolBoxActive = false;
        resetColor();
        settingColor = "white";
        
        clear();
    }

    public boolean isIsToolBoxActive() {
        return isToolBoxActive;
    }

    public void setIsToolBoxActive(boolean isToolBoxActive) throws IOException {
        this.isToolBoxActive = isToolBoxActive;
        isThesoActive = false;
        isCandidateActive = false;
        isProfileActive = false;
        isSettingActive = false;
        resetColor();
        toolBoxColor = "white";
        
        clear();
    }
    
    public void clear() throws IOException {
        System.gc();
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public String getThesoColor() {
        return thesoColor;
    }

    public void setThesoColor(String thesoColor) {
        this.thesoColor = thesoColor;
    }

    public String getCandidateColor() {
        return candidateColor;
    }

    public void setCandidateColor(String candidateColor) {
        this.candidateColor = candidateColor;
    }

    public String getProfileColor() {
        return profileColor;
    }

    public void setProfileColor(String profileColor) {
        this.profileColor = profileColor;
    }

    public String getSettingColor() {
        return settingColor;
    }

    public void setSettingColor(String settingColor) {
        this.settingColor = settingColor;
    }

    public String getToolBoxColor() {
        return toolBoxColor;
    }

    public void setToolBoxColor(String toolBoxColor) {
        this.toolBoxColor = toolBoxColor;
    }
/*
    public boolean isConceptDiagramSelected() {
        return isConceptDiagramSelected;
    }

    public void setConceptDiagramSelected(boolean conceptDiagramSelected) {
        isConceptDiagramSelected = conceptDiagramSelected;
    }*/

    public boolean isIsValueSelected() {
        return isValueSelected;
    }
    
    public void setIsValueSelected(boolean isValueSelected) {
        this.isValueSelected = isValueSelected;
        isFacetSelected = false;
        isHomeSelected = false;
    }

    public boolean isIsHomeSelected() {
        return isHomeSelected;
    }

    public void setIsHomeSelected(boolean isHomeSelected) {
        this.isHomeSelected = isHomeSelected;
        isFacetSelected = false;
        isValueSelected = false;
    }    
    
    public boolean isIsFacetSelected() {
        return isFacetSelected;
    }

    public void setIsFacetSelected(boolean isFacetSelected) {
        this.isFacetSelected = isFacetSelected;
        isValueSelected = false;
        isHomeSelected = false;
    }

}
