package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Named(value = "candidatService")
@SessionScoped
public class CandidatService implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CandidatPool candidatPool;

    private boolean myCandidatsSelected;
    private String searchValue;

    public void selectMyCandidats() {
        System.out.println("MyCandidats : " + myCandidatsSelected);
        if (myCandidatsSelected) {
            candidatPool.setCandidatList(candidatPool.getCandidatList()
                    .stream()
                    .filter(candidat -> candidat.getNomPref().equalsIgnoreCase("Firas GABSI"))
                    .collect(Collectors.toList()));
        } else {
            candidatPool.setCandidatList(candidatPool.getAllCandidats());
        }
    }

    public void searchByTermeAndAuteur() {
        System.out.println("searchValue : " + searchValue);
        if (!StringUtils.isEmpty(searchValue)) {
            candidatPool.setCandidatList(candidatPool.getCandidatList()
                .stream()
                .filter(candidat -> candidat.getNomPref().contains(searchValue))
                .collect(Collectors.toList()));
        } else {
            candidatPool.setCandidatList(candidatPool.getAllCandidats());
        }
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public boolean isMyCandidatsSelected() {
        return myCandidatsSelected;
    }

    public void setMyCandidatsSelected(boolean myCandidatsSelected) {
        this.myCandidatsSelected = myCandidatsSelected;
    }

}
