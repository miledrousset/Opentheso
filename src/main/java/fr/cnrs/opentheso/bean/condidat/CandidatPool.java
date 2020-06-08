
package fr.cnrs.opentheso.bean.condidat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class CandidatPool implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static int SIZE_OF_INITIAL_CAR_POOL = 15;
    
    private Candidat candidatSelect;

    private List<Candidat> candidatPool;

    @PostConstruct
    public void initRandomCarPool() {
        candidatPool = new ArrayList<Candidat>();
        for (int i = 0; i < SIZE_OF_INITIAL_CAR_POOL; i++) {
            candidatPool.add(getRandomCar(i));
        }
    }

    public void setCarPool(List<Candidat> carpool) {
        this.candidatPool = carpool;
    }

    private Candidat getRandomCar(int i) {
        Candidat car = new Candidat();
        car.setCandidat("Terme " + i);
        car.setDateNaissance("22/11/1986");
        car.setDemande(1+i + "");
        car.setEtat("En attente");
        car.setParticipant(2+i + "");
        return car;
    }

    public void onSelect(Candidat car) {
        System.out.println("OnSelect:" + car);
    }

    public Candidat getCandidatSelect() {
        return candidatSelect;
    }

    public void setCandidatSelect(Candidat candidatSelect) {
        this.candidatSelect = candidatSelect;
    }

    public List<Candidat> getCandidatPool() {
        return candidatPool;
    }

    public void setCandidatPool(List<Candidat> candidatPool) {
        this.candidatPool = candidatPool;
    }
    
}
