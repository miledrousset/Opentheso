package fr.cnrs.opentheso.bean.leftbody;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@SessionScoped
@Named(value = "leftBodySetting")
public class LeftBodySetting implements Serializable {

    private String index;

}
