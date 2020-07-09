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

    public void reset(){
        isThesoActive = true;
        resetColor();
        thesoColor = "white";
    }
    
    private void resetColor(){
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

    public boolean isIsValueSelected() {
        return isValueSelected;
    }

    public void setIsValueSelected(boolean isValueSelected) {
        this.isValueSelected = isValueSelected;
    }

    public boolean isIsThesoActive() {
        return isThesoActive;
    }

    public void setIsThesoActive(boolean isThesoActive) {
        this.isThesoActive = isThesoActive;
        isCandidateActive = false;
        isProfileActive = false; 
        isSettingActive = false;    
        isToolBoxActive = false; 
        resetColor();
        thesoColor = "white";         
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
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public boolean isIsProfileActive() {
        return isProfileActive;
    }

    public void setIsProfileActive(boolean isProfileActive) {
        this.isProfileActive = isProfileActive;
        isThesoActive = false;   
        isCandidateActive = false;
        isSettingActive = false;    
        isToolBoxActive = false; 
        resetColor();
        profileColor = "white";          
    }

    public boolean isIsSettingActive() {
        return isSettingActive;
    }

    public void setIsSettingActive(boolean isSettingActive) {
        this.isSettingActive = isSettingActive;
        isThesoActive = false;   
        isCandidateActive = false;
        isProfileActive = false; 
        isToolBoxActive = false;  
        resetColor();
        settingColor = "white";          
    }
    
    public boolean isIsToolBoxActive() {
        return isToolBoxActive;
    }

    public void setIsToolBoxActive(boolean isToolBoxActive) {
        this.isToolBoxActive = isToolBoxActive;
        isThesoActive = false;   
        isCandidateActive = false;
        isProfileActive = false; 
        isSettingActive = false;
        resetColor();
        toolBoxColor = "white";            
     
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

}
