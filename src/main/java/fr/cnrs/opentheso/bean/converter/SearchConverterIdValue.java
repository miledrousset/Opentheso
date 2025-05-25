package fr.cnrs.opentheso.bean.converter;

import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;


@FacesConverter("searchConverterIdValue")
public class SearchConverterIdValue implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        return (value != null && value.trim().length() > 0)
                ? NodeIdValue.builder().id(value).build()
                : null;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        return o == null ? null : ((NodeIdValue)o).getId();
    }
}
