package fr.cnrs.opentheso.bean.candidat;

import javax.inject.Named;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;


@Named("candidatProgressBar")
@ViewScoped
public class ProgressBarView implements Serializable {

    @Inject
    private CandidatBean candidatBean;
    

    public Integer getProgress() {
        Integer progressValue = candidatBean.getProgressBarValue();

        if (progressValue > 100) {
            progressValue = 100;
        }

        return progressValue;
    }

}
