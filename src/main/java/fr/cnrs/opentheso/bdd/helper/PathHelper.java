/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import java.util.ArrayList;

/**
 *
 * @author miledrousset
 */
public class PathHelper {

    public PathHelper() {
    }

    public ArrayList<Path> getPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        ArrayList<Path> allPaths = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();
        path.add(idConcept);
        
        ArrayList<String> firstPath = new ArrayList<>();
        allPaths = getInvertPathOfConcept(ds, idConcept,
                idThesaurus,
                firstPath,
                path, allPaths);

/*        for (int i = 0; i < tabIdInvert.size(); i++) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (int j = tabIdInvert.get(i).size(); j > 0; j--) {
                pathTemp.add(tabIdInvert.get(i).get(j - 1));
            }
            tabIdInvert.remove(i);
            tabIdInvert.add(i, pathTemp);
        }*/
        return allPaths;
    }
    
    /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @param tabId
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<Path> getInvertPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<Path> allPaths) {

        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if (idBTs.size() > 1) {
            for (String idBT1 : path) {
                firstPath.add(0,idBT1);
            }
        }
        if (idBTs.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String id2 : firstPath) {
                pathTemp.add(id2);
            }
            for (String id1 : path) {
                if (pathTemp.indexOf(id1) == -1) {
                    pathTemp.add(0,id1);
                }
            }
            
            Path path1 = new Path();
            path1.setPath(pathTemp);
            allPaths.add(path1);
            path.clear();
        }

        for (String idBT : idBTs) {
            path.add(idBT);
            getInvertPathOfConcept(ds, idBT, idThesaurus, firstPath, path, allPaths);
        }

        return allPaths;

    }


    
}
