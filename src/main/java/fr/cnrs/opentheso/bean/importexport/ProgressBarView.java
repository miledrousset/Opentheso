package fr.cnrs.opentheso.bean.importexport;

import javax.inject.Named;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

@Named
@ViewScoped
public class ProgressBarView implements Serializable {

    @Inject
    private ExportFileBean exportFileBean;
    

    public Integer getProgress() {
        Integer progressValue = (int) exportFileBean.getProgressBar();

        if (progressValue > 100) {
            progressValue = 100;
        }

        return progressValue;
    }

}
