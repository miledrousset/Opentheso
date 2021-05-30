/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.leftbody;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.annotation.PostConstruct;

/**
 *
 * @author miledrousset
 */
@Named(value = "leftBodySetting")
@SessionScoped
public class LeftBodySetting implements Serializable {

    private String index;
    @PostConstruct
    public void postInit(){
    }    
    
    public LeftBodySetting() {
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
