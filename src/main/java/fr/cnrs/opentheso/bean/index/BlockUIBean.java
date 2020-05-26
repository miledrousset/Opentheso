/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.index;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author miledrousset
 */
@Named(value = "blockUIBean")
@ViewScoped

public class BlockUIBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public void waitFiveSeconds() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
    }

    public boolean isBlockUIActive() {
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if ("/forms/blockUI.xhtml".equals(viewId)) {
            return true;
        }
        if ("/bootstrap/SocialShare.xhtml".equals(viewId)) {
            return true;
        }
        return false;
    }

}
