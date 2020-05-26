/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.importexport;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author miledrousset
 */
//@Named(value = "progressBarView")
@Named
//@ViewScoped
@SessionScoped
public class ProgressBarView implements Serializable {
     
    private Integer progress1;
 
    public Integer getProgress1() {
        progress1 = updateProgress(progress1);
        return progress1;
    }

 
    private Integer updateProgress(Integer progress) {
        if(progress == null) {
            progress = 0;
        }
        else {
            progress = progress + (int)(Math.random() * 35);
             
            if(progress > 100)
                progress = 100;
        }
         
        return progress;
    }
 
    public void setProgress1(Integer progress1) {
        this.progress1 = progress1;
    }
 

    public void startTestFunction(String test){
        String i= test;
    }
    
    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Progress Completed"));
    }
 
    public void cancel() {
        progress1 = null;
    }
}
