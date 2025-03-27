package fr.cnrs.opentheso.bean.profile;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@SessionScoped
@NoArgsConstructor
@Named(value = "profileBean")
public class ProfileBean implements Serializable {

    private MyAccountBean myAccountBean;
 
    private boolean isMyAccountActive;
    private boolean isMyProjectActive;
    private boolean isUsersActive;
    private boolean isProjectsActive; 
    private boolean isThesaurusActive;     
    
    private String myAccountColor;
    private String myProjectColor;
    private String usersColor;
    private String projectsColor;
    private String thesaurusColor;


    @Inject
    public ProfileBean(MyAccountBean myAccountBean) {
        this.myAccountBean = myAccountBean;
    }

    public void reset() {
        isMyAccountActive = true;
        isMyProjectActive = false;
        isUsersActive = false;
        isProjectsActive = false;
        isThesaurusActive = false;

        // activation de la couleur par defaut
        resetColor();
        myAccountColor = "white";
        myAccountBean.loadDataPage();
    }

    public void setIsMyAccountActive(boolean isMyAccountActive) {
        this.isMyAccountActive = isMyAccountActive;
        isMyProjectActive = false;
        isUsersActive = false;
        isProjectsActive = false;
        isThesaurusActive = false;
        resetColor();
        myAccountColor = "white";      
    }

    public void setIsMyProjectActive(boolean isMyProjectActive) {
        this.isMyProjectActive = isMyProjectActive;
        isMyAccountActive = false;
        isUsersActive = false;
        isProjectsActive = false;
        isThesaurusActive = false;
        resetColor();
        myProjectColor = "white";          
    }

    public void setIsUsersActive(boolean isUsersActive) {
        this.isUsersActive = isUsersActive;
        isMyAccountActive = false;
        isMyProjectActive = false;
        isProjectsActive = false;
        isThesaurusActive = false;
        resetColor();
        usersColor = "white";          
    }

    public void setIsProjectsActive(boolean isProjectsActive) {
        this.isProjectsActive = isProjectsActive;
        isMyAccountActive = false;
        isMyProjectActive = false;
        isThesaurusActive = false;
        isUsersActive = false;
        resetColor();
        projectsColor = "white";           
    }

    public void setIsThesaurusActive(boolean isThesaurusActive) {
        this.isThesaurusActive = isThesaurusActive;
        isMyAccountActive = false;
        isMyProjectActive = false;
        isProjectsActive = false;
        isUsersActive = false;
        resetColor();
        thesaurusColor = "white";           
    }
    
    private void resetColor(){
        myAccountColor = "#B3DDC4";
        myProjectColor = "#B3DDC4";
        usersColor = "#B3DDC4";
        projectsColor = "#B3DDC4";
        thesaurusColor = "#B3DDC4";
    }
}
