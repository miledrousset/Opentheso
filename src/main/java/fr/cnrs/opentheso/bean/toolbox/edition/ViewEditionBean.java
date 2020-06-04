/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.toolbox.edition;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 *
 * @author miledrousset
 */
@Named(value = "viewEditionBean")
@SessionScoped
public class ViewEditionBean implements Serializable {

    private String alternateColor;
    
    // les vues
    private boolean isViewListTheso;
    private boolean isViewExportSkos;
    private boolean isViewImportSkos;
    private boolean isViewImportCsv;    
    
    private boolean isViewNewTheso;
    private boolean isViewModifyTheso;
    
    
    private boolean isExportStarted;
    

    private boolean isImportStarted;
    
    public ViewEditionBean() {
        alternateColor = "#C8EAD6";
        isViewListTheso = true;
        isViewExportSkos = false;
        isViewImportSkos = false;
        isViewImportCsv = false;       
        isImportStarted = false;
        isViewNewTheso = false;
        isViewModifyTheso = false;
    }
    
    public void init(){
        alternateColor = "#C8EAD6";
        isViewListTheso = true;
        isViewExportSkos = false;
        isExportStarted = false;
        isViewImportSkos = false;
        isViewImportCsv = false;          
        isImportStarted = false;
        isViewNewTheso = false;
        isViewModifyTheso = false;
    }
    
    public String getNewAlternateColor(){
        if(alternateColor.equalsIgnoreCase("#FFFFFF")) {
            alternateColor = "#C8EAD6";
        } else {
            alternateColor = "#FFFFFF";
        }
        return alternateColor;
    }
    
    public String getAlternateColor(Boolean isNewLine){
        return (isNewLine) ? getNewAlternateColor() : alternateColor;
    }

    public boolean isIsViewListTheso() {
        return isViewListTheso;
    }

    public void setIsViewListTheso(boolean isViewListTheso) {
        this.isViewListTheso = isViewListTheso;
        isViewExportSkos = false;        
    }

    public boolean isIsViewExportSkos() {
        return isViewExportSkos;
    }

    public void setIsViewExportSkos(boolean isViewExportSkos) {
        this.isViewExportSkos = isViewExportSkos;
        isViewListTheso = false;
        isViewImportSkos = false;
        isViewImportCsv = false;          
        isViewNewTheso = false;
        isViewModifyTheso = false;
    }

    public boolean isIsExportStarted() {
        return isExportStarted;
    }

    public void setIsExportStarted(boolean isExportStarted) {
        this.isExportStarted = isExportStarted;
//        PrimeFaces pf = PrimeFaces.current(); 
//        if (pf.isAjaxRequest()) {
//            //    pf.ajax().update("messageIndex");
//            pf.ajax().update("toolBoxForm");
//            pf.ajax().update("toolBoxForm:listThesoForm:viewExportSkosForm");
//        }        
    }

    public boolean isIsViewImportSkos() {
        return isViewImportSkos;
    }

    public void setIsViewImportSkos(boolean isViewImportSkos) {
        this.isViewImportSkos = isViewImportSkos;
        isViewListTheso = false;
        isViewExportSkos = false;
        isViewNewTheso = false;
        isViewModifyTheso = false;
        isViewImportCsv = false;          
    }

    public boolean isIsImportStarted() {
        return isImportStarted;
    }

    public void setIsImportStarted(boolean isImportStarted) {
        this.isImportStarted = isImportStarted;
        isExportStarted = false;
    }

    public boolean isIsViewNewTheso() {
        return isViewNewTheso;
    }

    public void setIsViewNewTheso(boolean isViewNewTheso) {
        this.isViewNewTheso = isViewNewTheso;
        isViewExportSkos = false;
        isViewListTheso = false;
        isViewImportSkos = false;
        isViewImportCsv = false;          
        isViewModifyTheso = false;
    }

    public boolean isIsViewModifyTheso() {
        return isViewModifyTheso;
    }

    public void setIsViewModifyTheso(boolean isViewModifyTheso) {
        this.isViewModifyTheso = isViewModifyTheso;
        isViewNewTheso = false;
        isViewExportSkos = false;
        isViewListTheso = false;
        isViewImportSkos = false; 
        isViewImportCsv = false;          
    }

    public boolean isIsViewImportCsv() {
        return isViewImportCsv;
    }

    public void setIsViewImportCsv(boolean isViewImportCsv) {
        this.isViewImportCsv = isViewImportCsv;
        isViewNewTheso = false;
        isViewExportSkos = false;
        isViewListTheso = false;
        isViewImportSkos = false; 
        isViewModifyTheso = false;
    }

    
    
    
}
