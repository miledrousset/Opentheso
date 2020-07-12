package fr.cnrs.opentheso.bean.condidat;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.condidat.dao.TermeDao;
import fr.cnrs.opentheso.bean.condidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.condidat.enumeration.LanguageEnum;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;

@Named(value = "traductionService")
@SessionScoped
public class TraductionService implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CandidatBean candidatBean;

    private String langage;
    private String traduction;

    private String langageOld;
    private String traductionOld;

    private String newLangage;
    private String newTraduction;
    

    public TraductionService() {
        langage = null;
        traduction = null;
    }

    public void init(TraductionDto traductionDto) {
        langage = traductionDto.getLangue();
        traduction = traductionDto.getTraduction();

        langageOld = langage;
        traductionOld = traduction;
    }
    
    public void init() {
        newLangage = "";
        newTraduction = "";
    }

    public void addTraductionCandidat() throws SQLException {

        HikariDataSource connection = connect.getPoolConnexion();

        TermeDao termeDao = new TermeDao();

        String idTerm = termeDao.getIdTermeByCandidatAndThesaurus(connection, candidatBean.getCandidatSelected().getIdThesaurus(),
                candidatBean.getCandidatSelected().getIdConcepte());

        Term term = new Term();
        term.setStatus("D");
        term.setLang(newLangage.toLowerCase());
        term.setLexical_value(newTraduction);
        term.setId_thesaurus(candidatBean.getCandidatSelected().getIdThesaurus());
        term.setContributor(candidatBean.getCurrentUser().getNodeUser().getIdUser());
        term.setIdUser(candidatBean.getCurrentUser().getNodeUser().getIdUser()+"");
        term.setId_term(idTerm);

        termeDao.addNewTerme(connection, term);

        refrechTraductions(connection);

        connection.close();

        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, "Traduction ajoutée avec succée");
        candidatBean.setIsNewCandidatActivate(true);
    }

    private void refrechTraductions(HikariDataSource connection) {
        candidatBean.getCandidatSelected().setTraductions(new TermHelper().getTraductionsOfConcept(connection,
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus(),
                candidatBean.getCandidatSelected().getLang())
                .stream().map(termPref -> new TraductionDto(LanguageEnum.valueOf(termPref.getLang().toUpperCase()).getLanguage(),
                        termPref.getLexicalValue())).collect(Collectors.toList()));
    }

    public void deleteTraduction() throws SQLException {

        HikariDataSource connection = connect.getPoolConnexion();

        TermeDao termeDao = new TermeDao();

        String idTerm = termeDao.getIdTermeByCandidatAndThesaurus(connection, candidatBean.getCandidatSelected().getIdThesaurus(),
                candidatBean.getCandidatSelected().getIdConcepte());

        termeDao.deleteTermByIdTermAndLang(connection, idTerm, LanguageEnum.fromString(langage).name().toLowerCase());

        refrechTraductions(connection);

        connection.close();

        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, "Traduction supprimée avec succée");
        candidatBean.setIsNewCandidatActivate(true);
    }

    public void updateTraduction() throws SQLException {

        HikariDataSource connection = connect.getPoolConnexion();

        TermeDao termeDao = new TermeDao();

        String idTerm = termeDao.getIdTermeByCandidatAndThesaurus(connection, candidatBean.getCandidatSelected().getIdThesaurus(),
                candidatBean.getCandidatSelected().getIdConcepte());

        termeDao.updateTerme(connection, idTerm, traduction, LanguageEnum.fromString(langage).name().toLowerCase());

        refrechTraductions(connection);

        connection.close();

        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, "Traduction mise à jour avec succée !");
        candidatBean.setIsNewCandidatActivate(true);
    }

    public String getLangage() {
        return langage;
    }

    public void setLangage(String langage) {
        this.langage = langage;
    }

    public String getTraduction() {
        return traduction;
    }

    public void setTraduction(String traduction) {
        this.traduction = traduction;
    }

    public String getLangageOld() {
        return langageOld;
    }

    public void setLangageOld(String langageOld) {
        this.langageOld = langageOld;
    }

    public String getTraductionOld() {
        return traductionOld;
    }

    public void setTraductionOld(String traductionOld) {
        this.traductionOld = traductionOld;
    }

    public String getNewLangage() {
        return newLangage;
    }

    public void setNewLangage(String newLangage) {
        this.newLangage = newLangage;
    }

    public String getNewTraduction() {
        return newTraduction;
    }

    public void setNewTraduction(String newTraduction) {
        this.newTraduction = newTraduction;
    }

}
