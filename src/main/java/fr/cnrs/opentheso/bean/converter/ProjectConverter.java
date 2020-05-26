package fr.cnrs.opentheso.bean.converter;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("projectConverter")
public class ProjectConverter implements Converter{
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            try {
                NodeUserGroup nodeUserGroup = new NodeUserGroup();
                nodeUserGroup.setIdGroup(Integer.parseInt(value));
                return nodeUserGroup;
            } catch(NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid Id Project"));
            }            
        }
        else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        if(o == null) {
            return null;
        } else {
            return "" + ((NodeUserGroup)o).getIdGroup();
        }
    }
}
