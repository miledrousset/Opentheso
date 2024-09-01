package fr.cnrs.opentheso.models.skosapi;

import lombok.Data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 * SKOSXmlDocument est la classe qui permet de construire un document XML au
 * format SKOS (voir le site du W3C
 * http://www.w3.org/TR/2005/WD-swbp-skos-core-guide-20051102).</p>
 * <p>
 * Un objet SKOSXmlDocument est composé de :
 * <ul>
 * <li>Un ConceptScheme contenant les topConcepts</li>
 * <li>Une liste de ressources</li>
 * </ul>
 * </p>
 *
 */
@Data
public class SKOSXmlDocument {

    private SKOSResource conceptScheme;
    private ArrayList<SKOSResource> groupList;
    private ArrayList<SKOSResource> conceptList;
    private ArrayList<SKOSResource> facetList;
    private ArrayList<SKOSResource> foafImage;
    private String title;
    

    
    // c'est un tableau qui contient les équivalences entre les URI et l'identifiant des concepts
    // pour permettre de reconstruire un thésaurus avec les identifiants ARK-Handle et l'ID d'origine
    private HashMap<String,String> equivalenceUriArkHandle;

    /**
     * Constructeur qui permet de créer un nouveau SKOSXmlDocument
     */
    public SKOSXmlDocument() {
        groupList = new ArrayList<>();
        conceptList = new ArrayList<>();
        facetList = new ArrayList<>();
        foafImage = new ArrayList<>();
        equivalenceUriArkHandle = new HashMap();
    }

    public void clear(){
        if(groupList != null){
            groupList.forEach(sKOSResource -> {
                sKOSResource.clear();
            });
            groupList.clear();
            groupList = null;
        }
        if(conceptList != null){
            conceptList.forEach(sKOSResource -> {
                sKOSResource.clear();
            });
            conceptList.clear();
            conceptList = null;
        }        
        if(facetList != null){
            facetList.forEach(sKOSResource -> {
                sKOSResource.clear();
            });
            facetList.clear();
            facetList = null;
        }  
        if(foafImage != null){
            foafImage.forEach(sKOSResource -> {
                sKOSResource.clear();
            });
            foafImage.clear();
            foafImage = null;
        }         
        if(equivalenceUriArkHandle != null){
            equivalenceUriArkHandle.clear();
            equivalenceUriArkHandle = null;
        }
        if(conceptScheme != null) {
            conceptScheme.clear();
            conceptScheme = null;
        }
        title = null;
            
    }

    public void addGroup(SKOSResource r) {
        groupList.add(r);
    }

    public void addFacet(SKOSResource r) {
        facetList.add(r);
    }

    public void addFoafImage(SKOSResource r) {
        foafImage.add(r);
    }

    public void addconcept(SKOSResource r) {
        conceptList.add(r);
    }

    /**
     * Ecrit le document XML dans un fichier encodé en UTF-8
     *
     * @param fileName le nom du fichier
     * @return <code>true</code> si le fichier est correctement enregistré sur
     * le disque ;<br/>
     * <code>false</code> sinon.
     */
    public boolean write(String fileName) {
        File file = new File(fileName);
        Writer writeFile = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            writeFile.write(this.toString());
            writeFile.close();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
}
