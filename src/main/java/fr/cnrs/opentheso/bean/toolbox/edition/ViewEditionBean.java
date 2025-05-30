package fr.cnrs.opentheso.bean.toolbox.edition;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Data;
import java.io.Serializable;


@Data
@Named(value = "viewEditionBean")
@SessionScoped
public class ViewEditionBean implements Serializable {

    private String alternateColor, login, password, urlServer, nomGraphe;

    // les vues
    private boolean isViewListThesaurus;
    private boolean isViewExportSkos;
    private boolean isViewExportPDF;
    private boolean isViewExportCSV;
    private boolean isViewExportCSV_id;    
    private boolean isViewImportSkos;
    private boolean isViewImportCsv;
    private boolean isViewImportVirtuoso;
    private boolean isViewImportCSVStructure;
    private boolean isViewExportCSVStructure;

    private boolean isViewNewThesaurus;
    private boolean isViewModifyThesaurus;

    private boolean isExportStarted;

    private boolean isImportStarted;

    public ViewEditionBean() {
        alternateColor = "#C8EAD6";
        isViewListThesaurus = true;
        isViewExportSkos = false;
        isViewImportSkos = false;
        isViewImportCsv = false;
        isImportStarted = false;
        isViewNewThesaurus = false;
        isViewModifyThesaurus = false;
        isViewExportPDF = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;
    }

    public void init() {

        login = "dba";
        password = "dba";
        urlServer = "localhost:1111";
        nomGraphe = "localhost/test2";

        alternateColor = "#C8EAD6";
        isViewListThesaurus = true;
        isViewExportSkos = false;
        isExportStarted = false;
        isViewImportSkos = false;
        isViewImportCsv = false;
        isImportStarted = false;
        isViewNewThesaurus = false;
        isViewExportPDF = false;
        isViewModifyThesaurus = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;     
        isViewImportCSVStructure = false;
    }

    public String getNewAlternateColor() {
        if (alternateColor.equalsIgnoreCase("#FFFFFF")) {
            alternateColor = "#deeae3";
        } else {
            alternateColor = "#FFFFFF";
        }
        return alternateColor;
    }

    public String getAlternateColor(int position) {
        if (position == 0) {
            alternateColor = "#deeae3";
        } else {
            if (alternateColor.equalsIgnoreCase("#FAFAFA")) {
                alternateColor = "#deeae3";
            } else {
                alternateColor = "#FAFAFA";
            }
        }
        return alternateColor;
    }

    public boolean isIsViewListThesaurus() {
        return isViewListThesaurus;
    }

    public void setIsViewListThesaurus(boolean isViewListThesaurus) {
        this.isViewListThesaurus = isViewListThesaurus;
        isViewExportSkos = false;
    }

    public boolean isIsViewExportSkos() {
        return isViewExportSkos;
    }

    public void setIsViewExportSkos(boolean isViewExportSkos) {
        this.isViewExportSkos = isViewExportSkos;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewImportCsv = false;
        isViewNewThesaurus = false;
        isViewExportPDF = false;
        isViewModifyThesaurus = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;
    }

    public boolean isIsExportStarted() {
        return isExportStarted;
    }

    public void setIsExportStarted(boolean isExportStarted) {
        this.isExportStarted = isExportStarted;
    }

    public boolean isIsViewImportSkos() {
        return isViewImportSkos;
    }

    public void setIsViewImportSkos(boolean isViewImportSkos) {
        this.isViewImportSkos = isViewImportSkos;
        isViewListThesaurus = false;
        isViewExportSkos = false;
        isViewNewThesaurus = false;
        isViewModifyThesaurus = false;
        isViewImportCsv = false;
        isViewExportPDF = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;
    }

    public void setViewModifyThesaurus(boolean isViewModifyThesaurus) {
        this.isViewModifyThesaurus = isViewModifyThesaurus;
        isViewListThesaurus = false;
        isViewExportSkos = false;
        isViewNewThesaurus = false;
        isViewImportSkos = false;
        isViewImportCsv = false;
        isViewExportPDF = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;
        isViewImportCSVStructure = false;
    }

    public boolean isIsImportStarted() {
        return isImportStarted;
    }

    public void setIsImportStarted(boolean isImportStarted) {
        this.isImportStarted = isImportStarted;
        isExportStarted = false;
    }

    public boolean isIsViewNewThesaurus() {
        return isViewNewThesaurus;
    }

    public void setIsViewNewThesaurus(boolean isViewNewThesaurus) {
        this.isViewNewThesaurus = isViewNewThesaurus;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewImportCsv = false;
        isViewModifyThesaurus = false;
        isViewExportPDF = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;
    }

    public boolean isIsViewModifyThesaurus() {
        return isViewModifyThesaurus;
    }

    public void setIsViewModifyThesaurus(boolean isViewModifyThesaurus) {
        this.isViewModifyThesaurus = isViewModifyThesaurus;
        isViewNewThesaurus = false;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewImportCsv = false;
        isViewExportPDF = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;
    }

    public boolean isIsViewImportCsv() {
        return isViewImportCsv;
    }

    public void setIsViewImportCsv(boolean isViewImportCsv) {
        this.isViewImportCsv = isViewImportCsv;
        isViewNewThesaurus = false;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewModifyThesaurus = false;
        isViewExportPDF = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;
    }

    public boolean isExportView() {
        return isViewExportPDF || isViewExportCSV || isViewExportCSV_id || isViewExportSkos 
                || isViewImportVirtuoso || isViewExportCSVStructure;
    }

    public boolean isIsViewExportPDF() {
        return isViewExportPDF;
    }

    public void setIsViewExportPDF(boolean isViewExportPDF) {
        this.isViewExportPDF = isViewExportPDF;
        isViewNewThesaurus = false;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewModifyThesaurus = false;
        isViewImportCsv = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;
    }

    public void setIsViewExportCSV(boolean isViewExportCSV) {
        this.isViewExportCSV = isViewExportCSV;
        isViewExportCSV_id = false;
        isViewExportPDF = false;
        isViewNewThesaurus = false;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewModifyThesaurus = false;
        isViewImportCsv = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;
    }

    public boolean isIsViewExportCSV_id() {
        return isViewExportCSV_id;
    }

    public void setIsViewExportCSV_id(boolean isViewExportCSV_id) {
        this.isViewExportCSV_id = isViewExportCSV_id;
        isViewExportCSV = false;
        isViewExportPDF = false;
        isViewNewThesaurus = false;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewModifyThesaurus = false;
        isViewImportCsv = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;        
    }

    public void setIsViewExportCSVStructure(boolean isViewExportCSVStructure) {
        this.isViewExportCSVStructure = isViewExportCSVStructure;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewExportPDF = false;
        isViewNewThesaurus = false;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewModifyThesaurus = false;
        isViewImportCsv = false;
        isViewImportVirtuoso = false;  
        isViewImportCSVStructure = false;
    }

    public void setIsViewExportVirtuoso(boolean isViewImportVirtuoso) {
        this.isViewImportVirtuoso = isViewImportVirtuoso;
        isViewExportPDF = false;
        isViewNewThesaurus = false;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewModifyThesaurus = false;
        isViewImportCsv = false;
        isViewExportCSVStructure = false;  
        isViewImportCSVStructure = false;
    }

    public boolean isIsViewImportCSVStructure() {
        return isViewImportCSVStructure;
    }

    public void setIsViewImportCSVStructure(boolean isViewImportCSVStructure) {
        this.isViewImportCSVStructure = isViewImportCSVStructure;
        isViewImportCsv = false;
        isViewNewThesaurus = false;
        isViewExportSkos = false;
        isViewListThesaurus = false;
        isViewImportSkos = false;
        isViewModifyThesaurus = false;
        isViewExportPDF = false;
        isViewExportCSV = false;
        isViewExportCSV_id = false;
        isViewImportVirtuoso = false;
        isViewExportCSVStructure = false;
    }
    
}
