
package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author miledrousset
 */
@Named(value = "thesaurusMetadataAdd")
@SessionScoped
public class ThesaurusMetadataAdd implements Serializable{
    private List<DcElement> dcElements;
    private List<String> dcmiResource; 
    private String idTheso;
    
    @Inject private Connect connect;
    
    public void init(String idTheso1) {
        DcElementHelper dcElementHelper = new DcElementHelper();
        dcElements = dcElementHelper.getDcElementOfThesaurus(connect.getPoolConnexion(), idTheso1);
        if(dcElements == null || dcElements.isEmpty())
            dcElements = new ArrayList<>();
        dcmiResource = new DCMIResource().getAllResources();
        this.idTheso = idTheso1;
    }

    public List<DcElement> getDcElements() {
        return dcElements;
    }
    public void setDcElements(List<DcElement> dcElements) {
        this.dcElements = dcElements;
    }

    public List<String> getDcmiResource() {
        return dcmiResource;
    }

    public void setDcmiResource(List<String> dcmiResource) {
        this.dcmiResource = dcmiResource;
    }

    public void deleteThesoMetadata(DcElement dcElement){
        DcElementHelper dcElementHelper = new DcElementHelper();
        dcElementHelper.deleteDcElementThesaurus(connect.getPoolConnexion(), dcElement, idTheso);
        init(idTheso);
        FacesMessage msg = new FacesMessage("Dcterms deleted", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }
    
    public void onRowEdit(RowEditEvent<DcElement> event) {
        DcElement dcElementTemp = (DcElement)event.getObject();
        if(StringUtils.isEmpty(dcElementTemp.getValue())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Value required", "" + dcElementTemp.getId());
            FacesContext.getCurrentInstance().addMessage(null, msg);         
            return;
        }
        DcElementHelper dcElementHelper = new DcElementHelper();
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
        DcElement dcElement = new DcElement("", "", "");
        dcElements.add(dcElement);
    }    


}
