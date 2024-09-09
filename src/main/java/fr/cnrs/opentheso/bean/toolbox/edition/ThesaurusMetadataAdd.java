package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.RowEditEvent;


@Data
@Named(value = "thesaurusMetadataAdd")
@SessionScoped
public class ThesaurusMetadataAdd implements Serializable{
    private List<DcElement> dcElements;
    private List<String> dcmiResource; 
    private List<String> dcmiTypes;     
    private String idTheso;
    
    @Autowired @Lazy
    private Connect connect;

    @Autowired
    private DcElementHelper dcElementHelper;
    
    public void init(String idTheso1) {

        dcElements = dcElementHelper.getDcElementOfThesaurus(connect.getPoolConnexion(), idTheso1);
        if(dcElements == null || dcElements.isEmpty())
            dcElements = new ArrayList<>();
        dcmiResource = new DCMIResource().getAllResources();
        dcmiTypes = new DCMIResource().getAllTypes();        
        this.idTheso = idTheso1;
    }
    
    /**
     * retour des méta-données à la demande
     * @param idTheso
     * @return 
     */
    public List<DcElement> getThesaurusMetadata(String idTheso){
        return dcElementHelper.getDcElementOfThesaurus(connect.getPoolConnexion(), idTheso);
    }
    
    public void initlanguage(DcElement dcElement){
        if(!StringUtils.isEmpty(dcElement.getType()))
            dcElement.setLanguage("");
    }
    public void initType(DcElement dcElement){
        if(!StringUtils.isEmpty(dcElement.getLanguage()))
            dcElement.setType("");
    }

    public void deleteThesoMetadata(DcElement dcElement){
        dcElementHelper.deleteDcElementThesaurus(connect.getPoolConnexion(), dcElement, idTheso);
        init(idTheso);
        FacesMessage msg = new FacesMessage("Dcterms deleted", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }
    
    public void onRowEdit(RowEditEvent<DcElement> event) {
        DcElement dcElementTemp = event.getObject();
        if(StringUtils.isEmpty(dcElementTemp.getValue())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Value required", "" + dcElementTemp.getId());
            FacesContext.getCurrentInstance().addMessage(null, msg);         
            return;
        }

        if(dcElementTemp.getId() == -1) {
            int id = dcElementHelper.addDcElementThesaurus(connect.getPoolConnexion(), dcElementTemp, idTheso);
            if(id != -1) {
                dcElementTemp.setId(id);
            } else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Dcterms update failed", dcElementTemp.getValue());
                FacesContext.getCurrentInstance().addMessage(null, msg);                
            }
        } else {
            dcElementHelper.updateDcElementThesaurus(connect.getPoolConnexion(), dcElementTemp, idTheso);
        } 
        FacesMessage msg = new FacesMessage("Dcterms updated", dcElementTemp.getValue());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowCancel(RowEditEvent<DcElement> event) {
    }

    public void onAddNew() {
        DcElement dcElement = new DcElement("", "", "","");
        dcElements.add(dcElement);
    }    


}
