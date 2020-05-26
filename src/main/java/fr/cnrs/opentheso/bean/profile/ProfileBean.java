/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "profileBean")
@SessionScoped
public class ProfileBean implements Serializable {
    @Inject private MyAccountBean myAccountBean;
 
    private boolean isMyAccountActive;
    private boolean isMyProjectActive;
    private boolean isUsersActive;    
    
    private String myAccountColor;
    private String myProjectColor;
    private String usersColor;     
    
    
    public ProfileBean() {
    }

    public void reset() {
        isMyAccountActive = true;
        isMyProjectActive = false;
        isUsersActive = false;

        
        // activation de la couleur par defaut
        resetColor();
        myAccountColor = "white";
        myAccountBean.reset();
    }

    public boolean isIsMyAccountActive() {
        return isMyAccountActive;
    }

    public void setIsMyAccountActive(boolean isMyAccountActive) {
        this.isMyAccountActive = isMyAccountActive;
        isMyProjectActive = false;
        isUsersActive = false;
        resetColor();
        myAccountColor = "white";      
    }

    public boolean isIsMyProjectActive() {
        return isMyProjectActive;
    }

    public void setIsMyProjectActive(boolean isMyProjectActive) {
        this.isMyProjectActive = isMyProjectActive;
        isMyAccountActive = false;
        isUsersActive = false;
        resetColor();
        myProjectColor = "white";          
    }

    public boolean isIsUsersActive() {
        return isUsersActive;
    }

    public void setIsUsersActive(boolean isUsersActive) {
        this.isUsersActive = isUsersActive;
        isMyAccountActive = false;
        isMyProjectActive = false;
        resetColor();
        usersColor = "white";          
    }

    public String getUsersColor() {
        return usersColor;
    }

    public void setUsersColor(String usersColor) {
        this.usersColor = usersColor;
    }
    

    public String getMyProjectColor() {
        return myProjectColor;
    }

    public void setMyProjectColor(String myProjectColor) {
        this.myProjectColor = myProjectColor;
    }

    public String getMyAccountColor() {
        return myAccountColor;
    }

    public void setMyAccountColor(String myAccountColor) {
        this.myAccountColor = myAccountColor;
    }
    
    private void resetColor(){
        myAccountColor = "#B3DDC4";
        myProjectColor = "#B3DDC4";
        usersColor = "#B3DDC4";
    }
    
    
}
