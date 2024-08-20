package fr.cnrs.opentheso.bean.candidat;

import jakarta.inject.Named;
import java.io.Serializable;
import jakarta.faces.view.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;


@Named("candidatProgressBar")
@ViewScoped
public class ProgressBarView implements Serializable {

    @Autowired
    private CandidatBean candidatBean;
    

    public Integer getProgress() {
        Integer progressValue = candidatBean.getProgressBarValue();

        if (progressValue > 100) {
            progressValue = 100;
        }

        return progressValue;
    }

}
