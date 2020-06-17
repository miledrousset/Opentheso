package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.CorpusDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named(value = "corpusService")
@SessionScoped
public class CorpusService implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CandidatPool candidatPool;

    private String value;
    private String url;

    private String valueOld;
    private String urlOld;

    private String newValue;
    private String newURL;

    private boolean readyToSave;

    public CorpusService() {
        value = null;
        url = null;
    }

    public void init(CorpusDto corpusDto) {
        value = corpusDto.getValue();
        url = corpusDto.getUrl();

        valueOld = value;
        urlOld = url;

        readyToSave = true;
    }

    public void init() {
        newValue = "";
        newURL = "";
        readyToSave = true;
    }

    public void addCorpus() {
        if (readyToSave) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Corpus ajoutée avec sucée", null);
            FacesContext.getCurrentInstance().addMessage(null, message);

            candidatPool.getCorpusList().add(new CorpusDto(newValue, newURL));
            candidatPool.setIsNewCandidatActivate(true);

            readyToSave = false;
        }

    }

    public void deleteCorpus() {
        if (readyToSave) {
            List<CorpusDto> temps = candidatPool.getCorpusList();

            for (int i = 0; i < temps.size(); i++) {
                if (value != null && value.equals(temps.get(i).getValue())) {
                    temps.remove(i);
                }
            }

            if (candidatPool.getCorpusList().size() != temps.size()) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Corpus supprimée avec succée !", null);
                FacesContext.getCurrentInstance().addMessage(null, message);

                candidatPool.setCorpusList(temps);
                candidatPool.setIsNewCandidatActivate(true);
            }
            readyToSave = false;
        }
    }

    public void updateCorpus() {
        if (readyToSave) {
            List<CorpusDto> temps = candidatPool.getCorpusList();

            for (CorpusDto corpusDto : temps) {
                if (valueOld != null && valueOld.equals(corpusDto.getValue())
                        && urlOld != null && urlOld.equals(corpusDto.getUrl())) {
                    corpusDto.setValue(value);
                    corpusDto.setUrl(url);
                }
            }

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Corpus mise à jour avec succée !", null);
            FacesContext.getCurrentInstance().addMessage(null, message);

            candidatPool.setCorpusList(temps);
            candidatPool.setIsNewCandidatActivate(true);

            readyToSave = false;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getValueOld() {
        return valueOld;
    }

    public void setValueOld(String valueOld) {
        this.valueOld = valueOld;
    }

    public String getUrlOld() {
        return urlOld;
    }

    public void setUrlOld(String urlOld) {
        this.urlOld = urlOld;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getNewURL() {
        return newURL;
    }

    public void setNewURL(String newURL) {
        this.newURL = newURL;
    }

}
