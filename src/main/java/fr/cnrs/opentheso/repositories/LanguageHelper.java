package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import fr.cnrs.opentheso.models.languages.Languages_iso639;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Service
public class LanguageHelper {

    @Autowired
    private DataSource dataSource;
    
    
    public String normalizeIdLang(String idLang){
        switch (idLang) {
            case "en-GB":
                return "en";
            case "en-US":
                return "en";   
            case "pt-BR":
                return "pt";     
            case "pt-PT":
                return "pt";                  
        }
        return idLang;
    }    
    
    /**
     * Permet de retourner le code du drapeau d'un pays à partir du code de la langue
     *
     * @param idLang
     * @return Objet Class Thesaurus
     */
    public String getFlagFromIdLang(String idLang) {
        String flag = "";
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select code_pays from languages_iso639 where iso639_1 = '" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        flag = resultSet.getString("code_pays");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Flag : " + idLang, sqle);
        }
        return flag;
    }

    public List<Languages_iso639> getLanguagesByProject(String idProject) {

        List<Languages_iso639> language = null;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lang.* from languages_iso639 lang, project_description pro " +
                        "where lang.iso639_1 = pro.lang and pro.id_group = '"+idProject+"'");
                try ( ResultSet resultSet = stmt.getResultSet()) {

                    if (resultSet != null) {
                        language = new ArrayList<>();
                        while (resultSet.next()) {
                            Languages_iso639 languageTmp = new Languages_iso639();
                            languageTmp.setId_iso639_1(resultSet.getString("iso639_1").trim());
                            languageTmp.setId_iso639_2(resultSet.getString("iso639_2").trim());
                            languageTmp.setFrench_name(resultSet.getString("french_name"));
                            languageTmp.setEnglish_name(resultSet.getString("english_name"));
                            languageTmp.setCodePays(resultSet.getString("code_pays"));
                            language.add(languageTmp);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting languages of project : " + idProject, sqle);
        }
        return language ;
    }

    /**
     * Permet de retourner un ArrayList d'Objet Languages_iso639 de toute la table
     * Language_iso639 c'est la liste des langues ISO639 / ou null si rien
     *
     * @return Objet Class Thesaurus
     */
    public ArrayList<Languages_iso639> getAllLanguages() {
        ArrayList<Languages_iso639> language = new ArrayList<>();
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select distinct * from languages_iso639 ORDER BY iso639_1");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        language = new ArrayList<>();
                        while (resultSet.next()) {
                            Languages_iso639 languageTmp = new Languages_iso639();
                            languageTmp.setId_iso639_1(resultSet.getString("iso639_1"));
                            languageTmp.setId_iso639_2(resultSet.getString("iso639_2"));
                            languageTmp.setFrench_name(resultSet.getString("french_name"));
                            languageTmp.setEnglish_name(resultSet.getString("english_name"));
                            languageTmp.setCodePays(resultSet.getString("code_pays"));
                            language.add(languageTmp);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while adding element : " + language, sqle);
        }
        return language;
    }

}
