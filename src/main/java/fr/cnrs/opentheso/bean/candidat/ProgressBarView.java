package fr.cnrs.opentheso.bean.candidat;

import jakarta.inject.Named;
import java.io.Serializable;
import jakarta.faces.view.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;


@Named("candidatProgressBar")
@ViewScoped
public class ProgressBarView implements Serializable {

    @Autowired @Lazy
    private CandidatBean candidatBean;
    

    public Integer getProgress() {
        Integer progressValue = candidatBean.getProgressBarValue();

        if (progressValue > 100) {
            progressValue = 100;
        }

        return progressValue;
    }

}
