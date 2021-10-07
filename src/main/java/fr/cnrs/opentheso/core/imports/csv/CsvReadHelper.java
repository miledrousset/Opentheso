/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.imports.csv;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentImport;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentSmall;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author miled.rousset
 */
public class CsvReadHelper {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private String message = "";
    private char delimiter = ',';
    private String uri;
    
    private ArrayList<String> langs;

    private ArrayList<ConceptObject> conceptObjects;
    
    private ArrayList <NodeAlignmentImport> nodeAlignmentImports;
    private ArrayList<NodeNote> nodeNotes;
    
    public CsvReadHelper(char delimiter) {
        this.delimiter = delimiter;
        conceptObjects = new ArrayList<>();
    }
    
    /**
     * permet de lire un fichier CSV complet pour importer les alignements
     * @param in
     * @return 
     */
    public boolean readFileAlignmentToDelete(Reader in){
        try {
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().
                    withDelimiter(delimiter).withIgnoreEmptyLines().withIgnoreHeaderCase().withTrim().parse(in);
            String value;
            for (CSVRecord record : records) {
                ConceptObject conceptObject = new ConceptObject();
                // setId, si l'identifiant n'est pas renseigné, on récupère un NULL 
                try {
                    value = record.get("localId");
                    if(value == null) continue;
                    conceptObject.setLocalId(value);
                } catch (Exception e) {continue; }                
                // on récupère les uris à supprimer
                try {
                    value = record.get("Uri");
                    if(value == null) continue;
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId("");
                    nodeIdValue.setValue(value.trim());
                    conceptObject.alignments.add(nodeIdValue);
                } catch (Exception e) {continue;}                  

                conceptObjects.add(conceptObject);
            }
            return true;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CsvReadHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
   
    /**
     * permet de lire un fichier CSV complet pour importer les alignements
     * @param in
     * @return 
     */
    public boolean readFileAlignment(Reader in){
        try {
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().
                    withDelimiter(delimiter).withIgnoreEmptyLines().withIgnoreHeaderCase().withTrim().parse(in);
            String value;
            if(nodeAlignmentImports != null)
                nodeAlignmentImports = new ArrayList<>();
            else
                nodeAlignmentImports.clear();
            for (CSVRecord record : records) {
                NodeAlignmentImport nodeAlignmentImport = new NodeAlignmentImport();
                // setId, si l'identifiant n'est pas renseigné, on récupère un NULL 
                try {
                    value = record.get("localId");
                    if(value == null) continue;
                    nodeAlignmentImport.setLocalId(value);
                } catch (Exception e) {continue; }                
                
                // on récupère les alignements 
                
                nodeAlignmentImport = getNewAlignment(nodeAlignmentImport, record);
                if(nodeAlignmentImport != null)
                    nodeAlignmentImports.add(nodeAlignmentImport);
            }
            return true;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CsvReadHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }      
    private NodeAlignmentImport getNewAlignment(
            NodeAlignmentImport nodeAlignmentImport,
            CSVRecord record) {
        String uri1;

        /// types alignements 1=exactMatch ; 2=closeMatch ; 3=broadMatch ; 4=relatedMatch ; 5=narrowMatch
        try {
            uri1 = record.get("Wikidata");
            nodeAlignmentImport = getAlignmentSource(nodeAlignmentImport, "Wikidata", uri1);
        } catch (Exception e) {}        
        
        try {
            uri1 = record.get("AAT");
            nodeAlignmentImport = getAlignmentSource(nodeAlignmentImport, "AAT", uri1);
        } catch (Exception e) {}
        
        try {
            uri1 = record.get("BNF");
            nodeAlignmentImport = getAlignmentSource(nodeAlignmentImport, "BNF", uri1);       
        } catch (Exception e) {}
        
        try {
            uri1 = record.get("IdRef");
            nodeAlignmentImport = getAlignmentSource(nodeAlignmentImport, "IdRef", uri1);         
        } catch (Exception e) {}
        
        try {
            uri1 = record.get("Pleiades");
            nodeAlignmentImport = getAlignmentSource(nodeAlignmentImport, "Pleiades", uri1);            
        } catch (Exception e) {}       
        
        try {
            uri1 = record.get("PeriodO");
            nodeAlignmentImport = getAlignmentSource(nodeAlignmentImport, "PeriodO", uri1);            
        } catch (Exception e) {}       
        try {
            uri1 = record.get("Geonames");
            nodeAlignmentImport = getAlignmentSource(nodeAlignmentImport, "Geonames", uri1);            
        } catch (Exception e) {}          
        
        return nodeAlignmentImport;
    }
    private NodeAlignmentImport getAlignmentSource(NodeAlignmentImport nodeAlignmentImport, String source, String uri){
        String[] valueType;
        /// types alignements 1=exactMatch ; 2=closeMatch ; 3=broadMatch ; 4=relatedMatch ; 5=narrowMatch
        try {
            if(source != null && !source.isEmpty()) {
                NodeAlignmentSmall nodeAlignmentSmall = new NodeAlignmentSmall();
                nodeAlignmentSmall.setSource(source);
                
                //on récupère le type d'alignement (url##1)
                if(uri.contains("##")) {
                    valueType = uri.split("##");
                    if(valueType.length == 2){
                        nodeAlignmentSmall.setUri_target(valueType[0]);
                        try {
                            nodeAlignmentSmall.setAlignement_id_type(Integer.parseInt(valueType[1]));
                        } catch (Exception e) {
                            nodeAlignmentSmall.setAlignement_id_type(1);
                        }
                    } else {
                        nodeAlignmentSmall.setUri_target(uri);
                        nodeAlignmentSmall.setAlignement_id_type(1);
                    }
                } else {
                    nodeAlignmentSmall.setUri_target(uri);
                    nodeAlignmentSmall.setAlignement_id_type(1);
                }
                nodeAlignmentImport.getNodeAlignmentSmalls().add(nodeAlignmentSmall);
                return nodeAlignmentImport;
            }
        } catch (Exception e) {}
        return null;
    }
    
    /**
     * permet de lire un fichier CSV complet pour importer les notes
     * avec option de vider les notes avant
     * @param in
     * @return 
     */
    public boolean readFileNote(Reader in){
        try {
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().
                    withDelimiter(delimiter).withIgnoreEmptyLines().withIgnoreHeaderCase().withTrim().parse(in);
            
            String idConcept;
            for (CSVRecord record : records) {
                ConceptObject conceptObject = new ConceptObject();                
                // setId, si l'identifiant n'est pas renseigné, on récupère un NULL 
                try {
                    idConcept = record.get("localId");
                    if(idConcept == null || idConcept.isEmpty()) continue;
                    conceptObject.setIdConcept(idConcept);
                } catch (Exception e) {continue; }                
                
                // on récupère les notes 
                conceptObject = getNotes(conceptObject, record);
                
                if(conceptObject != null) {
                    conceptObjects.add(conceptObject);
                }
            }
            return true;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CsvReadHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }        
    
    
    
    
    
    
    
    
    
    
    
////////////////////////////////////////////////////////////////////////////////    
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    
    public boolean setLangs(Reader in){
        langs = new ArrayList<>();
        try {
            Map<String, Integer> headers = CSVFormat.RFC4180.withFirstRecordAsHeader().withIgnoreHeaderCase().withIgnoreEmptyLines().withDelimiter(delimiter).parse(in).getHeaderMap();
            String values[];
            for (String columnName : headers.keySet()) {
                if(columnName.contains("@")) {
                    values = columnName.split("@");
                    if(values[1] != null)
                        if(!langs.contains(values[1]))
                            langs.add(values[1]);//columnName.substring(columnName.indexOf("@"), columnName.indexOf("@" +2)));
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CsvReadHelper.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return !langs.isEmpty(); 
    }
    
    
    /**
     * permet de lire un fichier CSV complet pour changer un thésaurus
     * @param in
     * @return 
     */
    public boolean readFile(Reader in){
        try {
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().
                    withDelimiter(delimiter).withIgnoreEmptyLines().withIgnoreHeaderCase().withTrim().parse(in);
            String uri1; 
            for (CSVRecord record : records) {
                ConceptObject conceptObject = new ConceptObject();
                
                // setId, si l'identifiant n'est pas renseigné, on récupère un NULL 
                // puis on génère un nouvel identifiant
                
                try {
                    uri1 = record.get("URI");
                    conceptObject.setUri(uri1);
                } catch (Exception e) { }                

                if(record.isMapped("identifier")) {
                    try {
                        uri1 = record.get("identifier");
                        conceptObject.setIdConcept(uri1);
                    } catch (Exception e) { }                    
                } else {
                    try {
                        uri1 = record.get("URI");
                        conceptObject.setIdConcept(getId(uri1));
                    } catch (Exception e) { }                    
                }
                
                // on récupère l'id Ark s'il existe
                conceptObject = getArkId(conceptObject, record);                
                
                // on récupère les labels
                conceptObject = getLabels(conceptObject, record);
                
                // on récupère les notes
                conceptObject = getNotes(conceptObject, record);
 
                // on récupère le type
                conceptObject.setType(getType(record));
                
                // on récupère la notation
                conceptObject.setNotation(getNotation(record));

                // on récupère les relations (BT, NT, RT)
                conceptObject = getRelations(conceptObject, record);
                
                // on récupère les alignements 
                conceptObject = getAlignments(conceptObject, record);
                
                // on récupère la localisation
                conceptObject = getGeoLocalisation(conceptObject, record);
                
                // on récupère les membres (l'appartenance du concept à un groupe, collection ...
                conceptObject = getMembers(conceptObject, record);
                
                // on récupère la date
                conceptObject = getDates(conceptObject, record);
                
                conceptObjects.add(conceptObject);
                uri1 = null;
            }
            return true;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CsvReadHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }    
    
    
    /**
     * permet de lire une liste en CSV, la première colonne n'est pas obligatoire 
     * pour charger une liste de concepts
     * @param in
     * @return 
     */
    public boolean readListFile(Reader in){
        try {
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().
                    withDelimiter(delimiter).withIgnoreEmptyLines().withTrim().parse(in);
            String uri1 = null;
  //          boolean first = true;
            
            for (CSVRecord record : records) {
//                if(first) {
//                    // set the Uri
//                    try {
//                        uri = record.get("URI");
//                        uri = uri.substring(0, uri.lastIndexOf("/"));
//                    } catch (Exception e) {
//                        //System.err.println("");
//                    }
//                    first = false;
//                }
                ConceptObject conceptObject = new ConceptObject();
                
                // setId, si l'identifiant n'est pas renseigné, on récupère un NULL 
                // puis on génère un nouvel identifiant
                try {
                    uri1 = record.get("URI");
                    conceptObject.setUri(uri1);
                } catch (Exception e) { }                

                try {
                    uri1 = record.get("identifier");
                } catch (Exception e) { }
                
                try {
                    if(uri1 == null || uri1.isEmpty()) {
                        uri1 = record.get("URI");
                        uri1 = getId(uri1);
                    }
                    conceptObject.setIdConcept(uri1);
                } catch (Exception e) { }

                // on récupère l'id Ark s'il existe
                conceptObject = getArkId(conceptObject, record);   

                
                // on récupère les labels
                conceptObject = getLabels(conceptObject, record);
                
                // on récupère les notes
                conceptObject = getNotes(conceptObject, record);
 
                // on récupère le type
                conceptObject.setType(getType(record));
                
                // on récupère la notation
                conceptObject.setNotation(getNotation(record));

                // on récupère les relations (BT, NT, RT)
                conceptObject = getRelations(conceptObject, record);
                
                // on récupère les alignements 
                conceptObject = getAlignments(conceptObject, record);
                
                // on récupère la localisation
                conceptObject = getGeoLocalisation(conceptObject, record);
                
                // on récupère les membres (l'appartenance du concept à un groupe, collection ...
                conceptObject = getMembers(conceptObject, record);
                
                // on récupère la date
                conceptObject = getDates(conceptObject, record);
                
                conceptObjects.add(conceptObject);
                uri1 = null;
            }
            return true;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CsvReadHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * permet de récupérer l'identifiant d'près une URI
     * @return 
     */
    private String getId(String uri) {
        String id;

        if(uri == null || uri.isEmpty()) return null;
    
        if (uri.contains("idg=")) {
            if (uri.contains("&")) {
                id = uri.substring(uri.indexOf("idg=") + 4, uri.indexOf("&"));
            } else {
                id = uri.substring(uri.indexOf("idg=") + 4, uri.length());
            }
        } else {
            if (uri.contains("idc=")) {
                if (uri.contains("&")) {
                    id = uri.substring(uri.indexOf("idc=") + 4, uri.indexOf("&"));
                } else {
                    id = uri.substring(uri.indexOf("idc=") + 4, uri.length());
                }
            } else {
                if (uri.contains("#")) {
                    id = uri.substring(uri.indexOf("#") + 1, uri.length());
                } else {
                    id = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
                }
            }
        }
        StringPlus stringPlus = new StringPlus();
        id = stringPlus.normalizeStringForIdentifier(id);
        return id;
    }
    
    /**
     * permet de récupérer le type du concept (collection, groupe ...)
     * @param record
     * @return 
     */
    private String getType(CSVRecord record) {
        String type = "";
        try {
            type = record.get("rdf:type");
        } catch (Exception e) {
            //System.err.println("");
        }
        return type.trim();
    } 
    
    /**
     * permet de récupérer la notation du concept
     * @param record
     * @return 
     */
    private String getNotation(CSVRecord record) {
        String notation = "";
        try {
            notation = record.get("skos:notation");
        } catch (Exception e) {
            //System.err.println("");
        }
        return notation.trim();
    }  
    
    /**
     * permet de charger tous les alignements d'un concept
     * @param conceptObject
     * @param record
     * @return 
     */
    private ConceptObject getDates(ConceptObject conceptObject, CSVRecord record) {

        String value;
        
        // dct:created
        try {
            value = record.get("dct:created");
            if(!value.isEmpty()) {
                conceptObject.setCreated(value.trim());
            }
        } catch (Exception e) {
            //System.err.println("");
        }
        // dct:modified
        try {
            value = record.get("dct:modified");
            if(!value.isEmpty()) {
                conceptObject.setModified(value.trim());
            }
        } catch (Exception e) {
            //System.err.println("");
        }         

        return conceptObject;
    }      

    /**
     * permet de charger tous les alignements d'un concept
     * @param conceptObject
     * @param record
     * @return 
     */
    private ConceptObject getMembers(ConceptObject conceptObject, CSVRecord record) {

        String value;
        String values[];
        
        // skos:member
        try {
            value = record.get("skos:member");
            values = value.split("##");
            for (String value1 : values) {
                if(!value1.isEmpty()) {
                    conceptObject.members.add(getId(value.trim()));
                }
            }            
        } catch (Exception e) {
            //System.err.println("");
        }   

        return conceptObject;
    }      
    
    /**
     * permet de charger tous les alignements d'un concept
     * @param conceptObject
     * @param record
     * @return 
     */
    private ConceptObject getArkId(ConceptObject conceptObject, CSVRecord record) {
        String arkId;
        try {
            arkId = record.get("arkId");
            if(arkId != null) {
                conceptObject.setArkId(arkId.trim());
            }
        } catch (Exception e) {
            //System.err.println("");
        }   
        return conceptObject;
    }      
    
    /**
     * permet de charger tous les alignements d'un concept
     * @param conceptObject
     * @param record
     * @return 
     */
    private ConceptObject getGeoLocalisation(
            ConceptObject conceptObject,
            CSVRecord record) {
        String lat;
        String longitude;
        // geo:lat
        try {
            lat = record.get("geo:lat");
            longitude = record.get("geo:long");
            if(!lat.isEmpty()) {
                if(!longitude.isEmpty()) {
                    conceptObject.setLatitude(lat.trim());
                    conceptObject.setLongitude(longitude.trim());
                }
            }
        } catch (Exception e) {
            //System.err.println("");
        }
        return conceptObject;
    }      
    
    /**
     * permet de charger tous les alignements d'un concept
     * @param conceptObject
     * @param record
     * @return 
     */
    private ConceptObject getAlignments(
            ConceptObject conceptObject,
            CSVRecord record) {
        String value;
        String values[];
        
        // skos:exactMatch
        try {
            value = record.get("skos:exactMatch");
            values = value.split("##");
            for (String value1 : values) {
                if(!value1.isEmpty()) {
                    conceptObject.exactMatchs.add(value1.trim());
                }
            }            
        } catch (Exception e) {
            //System.err.println("");
        }
        
        // skos:closeMatch
        try {
            value = record.get("skos:closeMatch");
            values = value.split("##");
            for (String value1 : values) {
                if(!value1.isEmpty()) {
                    conceptObject.closeMatchs.add(value1.trim());
                }
            }            
        } catch (Exception e) {
            //System.err.println("");
        }
        // skos:broadMatch
        try {
            value = record.get("skos:broadMatch");
            values = value.split("##");
            for (String value1 : values) {
                if(!value1.isEmpty()) {
                    conceptObject.broadMatchs.add(value1.trim());
                }
            }            
        } catch (Exception e) {
            //System.err.println("");
        }     
        // skos:narrowMatch
        try {
            value = record.get("skos:narrowMatch");
            values = value.split("##");
            for (String value1 : values) {
                if(!value1.isEmpty()) {
                    conceptObject.narrowMatchs.add(value1.trim());
                }
            }            
        } catch (Exception e) {
            //System.err.println("");
        }
        // skos:relatedMatch
        try {
            value = record.get("skos:relatedMatch");
            values = value.split("##");
            for (String value1 : values) {
                if(!value1.isEmpty()) {
                    conceptObject.relatedMatchs.add(value1.trim());
                }
            }            
        } catch (Exception e) {
            //System.err.println("");
        }         

        return conceptObject;
    }    
    
    /**
     * permet de charger toutes les relations d'un concept
     * @param conceptObject
     * @param record
     * @return 
     */
    private ConceptObject getRelations(
            ConceptObject conceptObject,
            CSVRecord record) {
        String value = null;
        String values[];
        
        // skos:narrowerId (on vérifie si ce champs est renseigné, on le prend avant skos:narrower pour éviter de découper les identifiants
        // et surtout en cas de fichier avec des uris Ark pour retrouver les bons id internes

        // narrowerId 
        if(record.isMapped("narrowerid")) {
            try {
                value = record.get("narrowerid");
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        conceptObject.narrowers.add(value1.trim());
                    }
                }            
            } catch (Exception e) {
                //System.err.println("");
            }            
        } else {
            // skos:narrower
            try {
                value = record.get("skos:narrower");
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        conceptObject.narrowers.add(getId(value1.trim()));
                    }
                }
            } catch (Exception e) {
                //System.err.println("");
            }            
        }

        // broaderId        
        if(record.isMapped("broaderid")) {
            try {
                value = record.get("broaderid");
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        conceptObject.broaders.add(value1.trim());
                    }
                }            
            } catch (Exception e) {
                //System.err.println("");
            }             
        } else {
            // skos:broader
            try {
                value = record.get("skos:broader");
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        conceptObject.broaders.add(getId(value1.trim()));
                    }
                }
            } catch (Exception e) {
                //System.err.println("");
            }            
        }
        
        if(record.isMapped("relatedid")) {
            // relatedId        
            try {
                value = record.get("relatedid");
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        conceptObject.relateds.add(value1.trim());
                    }
                }            
            } catch (Exception e) {
                //System.err.println("");
            }             
        } else {
            // skos:related
            try {
                value = record.get("skos:related");
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        conceptObject.relateds.add(getId(value1.trim()));
                    }
                }
            } catch (Exception e) {
                //System.err.println("");
            }              
        }
        return conceptObject;
    }
    
    /**
     * permet de charger tous les labels d'un concept dans toutes les langues
     * @param conceptObject
     * @param record
     * @return 
     */
    private ConceptObject getLabels(ConceptObject conceptObject, CSVRecord record) {
        String value;
        Label label;
        String values[];
        
        for (String idLang : langs) {
            // prefLabel
            try {
                value = record.get("skos:prefLabel@" + idLang.trim());
                if(!value.isEmpty()) {
                    label = new Label();
                    label.setLabel(value);
                    label.setLang(idLang);
                    conceptObject.prefLabels.add(label);
                }
            } catch (Exception e) {
                //System.err.println("");
            }

            // altLabel
            try {
                value = record.get("skos:altLabel@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value.isEmpty()) {
                        label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.altLabels.add(label);
                    }
                }
            } catch (Exception e) {
                //System.err.println("");
            }
            
            // hiddenLabel
            try {
                value = record.get("skos:hiddenLabel@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value.isEmpty()) {
                        label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.hiddenLabels.add(label);
                    }
                }
            } catch (Exception e) {
            }             
        }

        return conceptObject;
    }
    
    private ConceptObject getNotes(ConceptObject conceptObject, CSVRecord record){
        
        String value;
        String values[];
        for (String idLang : langs) {
            // note
            try {
                value = record.get("skos:note@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        Label label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.note.add(label);
                    }
                }

            } catch (Exception e) {
                //System.err.println("");
            }            
            // définition
            try {
                value = record.get("skos:definition@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        Label label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.definitions.add(label);
                    }
                }

            } catch (Exception e) {
                //System.err.println("");
            }
            
            // scopeNotes note d'application
            try {
                value = record.get("skos:scopeNote@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        Label label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.scopeNotes.add(label);
                    }
                }

            } catch (Exception e) {
                //System.err.println("");
            }
        
            // example
            try {
                value = record.get("skos:example@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        Label label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.examples.add(label);
                    }
                }

            } catch (Exception e) {
                //System.err.println("");
            } 
            
            // historyNotes
            try {
                value = record.get("skos:historyNote@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        Label label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.historyNotes.add(label);
                    }
                }

            } catch (Exception e) {
                //System.err.println("");
            }
            
            // changeNotes
            try {
                value = record.get("skos:changeNote@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        Label label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.changeNotes.add(label);
                    }
                }

            } catch (Exception e) {
                //System.err.println("");
            }
            
            // editorialNotes
            try {
                value = record.get("skos:editorialNote@" + idLang.trim());
                values = value.split("##");
                for (String value1 : values) {
                    if(!value1.isEmpty()) {
                        Label label = new Label();
                        label.setLabel(value1);
                        label.setLang(idLang);
                        conceptObject.editorialNotes.add(label);
                    }
                }

            } catch (Exception e) {
                //System.err.println("");
            }              
        }        
        return conceptObject; 
    } 
    
    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<ConceptObject> getConceptObjects() {
        return conceptObjects;
    }

    public String getUri() {
        return uri;
    }

    public ArrayList<String> getLangs() {
        return langs;
    }

    public ArrayList<NodeAlignmentImport> getNodeAlignmentImports() {
        return nodeAlignmentImports;
    }

    public void setNodeAlignmentImports(ArrayList<NodeAlignmentImport> nodeAlignmentImports) {
        this.nodeAlignmentImports = nodeAlignmentImports;
    }

    public ArrayList<NodeNote> getNodeNotes() {
        return nodeNotes;
    }

    public void setNodeNotes(ArrayList<NodeNote> nodeNotes) {
        this.nodeNotes = nodeNotes;
    }

    
    
    public class ConceptObject {
        private String idConcept;
        private String uri;
        private String localId; //identifiant local
        private String arkId;
        private String idTerm;
        // rdf:type pour distinguer les concepts des collections, groupes ...
        private String type;
                
        // notation
        private String notation;
        
        // les labels
        private ArrayList<Label> prefLabels;
        private ArrayList<Label> altLabels;
        private ArrayList<Label> hiddenLabels;
        
        // Les notes
        private ArrayList<Label> note;        
        private ArrayList<Label> definitions;
        private ArrayList<Label> scopeNotes;
        private ArrayList<Label> examples;
        private ArrayList<Label> historyNotes;
        private ArrayList<Label> changeNotes;
        private ArrayList<Label> editorialNotes;

        // les relations, broader, narrower, related
        private ArrayList<String> broaders;
        private ArrayList<String> narrowers;
        private ArrayList<String> relateds;        
        
        // les aligenements 
        private ArrayList<String> exactMatchs;
        private ArrayList<String> closeMatchs;
        private ArrayList<String> broadMatchs;
        private ArrayList<String> narrowMatchs;
        private ArrayList<String> relatedMatchs;       
        
        // géolocalisation
        // geo:lat  geo:long
        private String latitude;
        private String longitude;
        
        // skos:member, l'appartenance du concept à un groupe ou collection ...
        private ArrayList<String> members;
        
        // dates 
        //dct:created, dct:modified
        private String created;
        private String modified;        
        
        /// pour récupérer une liste d'alignements (à ajouter ou à supprimer)
        private ArrayList<NodeIdValue> alignments; 

        public ConceptObject() {
            prefLabels = new ArrayList<>();
            altLabels = new ArrayList<>(); 
            hiddenLabels = new ArrayList<>();
            
            note = new ArrayList<>();
            definitions = new ArrayList<>();
            scopeNotes = new ArrayList<>();
            examples = new ArrayList<>();
            historyNotes = new ArrayList<>();
            changeNotes = new ArrayList<>();
            editorialNotes = new ArrayList<>();
            
            broaders = new ArrayList<>();
            narrowers = new ArrayList<>(); 
            relateds = new ArrayList<>();             
            
            exactMatchs = new ArrayList<>();
            closeMatchs = new ArrayList<>();
            broadMatchs = new ArrayList<>();
            narrowMatchs = new ArrayList<>();
            relatedMatchs = new ArrayList<>();
            
            members = new ArrayList<>();      
            alignments = new ArrayList<>();
        }
        public void clear(){
            if(prefLabels != null) prefLabels.clear();
            if(altLabels != null) altLabels.clear();
            if(hiddenLabels != null) hiddenLabels.clear();
            if(note != null) note.clear();
            if(definitions != null) definitions.clear();
            if(scopeNotes != null) scopeNotes.clear();
            if(examples != null) examples.clear();
            if(historyNotes != null) historyNotes.clear();
            if(changeNotes != null) changeNotes.clear();
            if(editorialNotes != null) editorialNotes.clear();
            if(broaders != null) broaders.clear();    
            if(narrowers != null) narrowers.clear();   
            if(relateds != null) relateds.clear();  
            if(exactMatchs != null) exactMatchs.clear();  
            if(closeMatchs != null) closeMatchs.clear();  
            if(broadMatchs != null) broadMatchs.clear();  
            if(narrowMatchs != null) narrowMatchs.clear();  
            if(relatedMatchs != null) relatedMatchs.clear();  
            if(members != null) members.clear();  
            if(alignments != null) alignments.clear();
        }        

        public String getIdConcept() {
            return idConcept;
        }

        public void setIdConcept(String idConcept) {
            this.idConcept = idConcept;
        }

        public String getArkId() {
            return arkId;
        }

        public void setArkId(String arkId) {
            this.arkId = arkId;
        }
        
        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getLocalId() {
            return localId;
        }

        public void setLocalId(String localId) {
            this.localId = localId;
        }

        public String getIdTerm() {
            return idTerm;
        }

        public void setIdTerm(String idTerm) {
            this.idTerm = idTerm;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getNotation() {
            return notation;
        }

        public void setNotation(String notation) {
            this.notation = notation;
        }

        public ArrayList<Label> getPrefLabels() {
            return prefLabels;
        }

        public void setPrefLabels(ArrayList<Label> prefLabels) {
            this.prefLabels = prefLabels;
        }

        public ArrayList<Label> getAltLabels() {
            return altLabels;
        }

        public void setAltLabels(ArrayList<Label> altLabels) {
            this.altLabels = altLabels;
        }

        public ArrayList<Label> getHiddenLabels() {
            return hiddenLabels;
        }

        public void setHiddenLabels(ArrayList<Label> hiddenLabels) {
            this.hiddenLabels = hiddenLabels;
        }

        public ArrayList<Label> getNote() {
            return note;
        }

        public void setNote(ArrayList<Label> note) {
            this.note = note;
        }

        public ArrayList<Label> getDefinitions() {
            return definitions;
        }

        public void setDefinitions(ArrayList<Label> definitions) {
            this.definitions = definitions;
        }

        public ArrayList<Label> getScopeNotes() {
            return scopeNotes;
        }

        public void setScopeNotes(ArrayList<Label> scopeNotes) {
            this.scopeNotes = scopeNotes;
        }

        public ArrayList<Label> getExamples() {
            return examples;
        }

        public void setExamples(ArrayList<Label> examples) {
            this.examples = examples;
        }

        public ArrayList<Label> getHistoryNotes() {
            return historyNotes;
        }

        public void setHistoryNotes(ArrayList<Label> historyNotes) {
            this.historyNotes = historyNotes;
        }

        public ArrayList<Label> getChangeNotes() {
            return changeNotes;
        }

        public void setChangeNotes(ArrayList<Label> changeNotes) {
            this.changeNotes = changeNotes;
        }

        public ArrayList<Label> getEditorialNotes() {
            return editorialNotes;
        }

        public void setEditorialNotes(ArrayList<Label> editorialNotes) {
            this.editorialNotes = editorialNotes;
        }

        public ArrayList<String> getBroaders() {
            return broaders;
        }

        public void setBroaders(ArrayList<String> broaders) {
            this.broaders = broaders;
        }

        public ArrayList<String> getNarrowers() {
            return narrowers;
        }

        public void setNarrowers(ArrayList<String> narrowers) {
            this.narrowers = narrowers;
        }

        public ArrayList<String> getRelateds() {
            return relateds;
        }

        public void setRelateds(ArrayList<String> relateds) {
            this.relateds = relateds;
        }

        public ArrayList<String> getExactMatchs() {
            return exactMatchs;
        }

        public void setExactMatchs(ArrayList<String> exactMatchs) {
            this.exactMatchs = exactMatchs;
        }

        public ArrayList<String> getCloseMatchs() {
            return closeMatchs;
        }

        public void setCloseMatchs(ArrayList<String> closeMatchs) {
            this.closeMatchs = closeMatchs;
        }

        public ArrayList<String> getBroadMatchs() {
            return broadMatchs;
        }

        public void setBroadMatchs(ArrayList<String> broadMatchs) {
            this.broadMatchs = broadMatchs;
        }

        public ArrayList<String> getNarrowMatchs() {
            return narrowMatchs;
        }

        public void setNarrowMatchs(ArrayList<String> narrowMatchs) {
            this.narrowMatchs = narrowMatchs;
        }

        public ArrayList<String> getRelatedMatchs() {
            return relatedMatchs;
        }

        public void setRelatedMatchs(ArrayList<String> relatedMatchs) {
            this.relatedMatchs = relatedMatchs;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public ArrayList<String> getMembers() {
            return members;
        }

        public void setMembers(ArrayList<String> members) {
            this.members = members;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getModified() {
            return modified;
        }

        public void setModified(String modified) {
            this.modified = modified;
        }

        public ArrayList<NodeIdValue> getAlignments() {
            return alignments;
        }

        public void setAlignments(ArrayList<NodeIdValue> alignments) {
            this.alignments = alignments;
        }



    }
    

    public class Label {

        private String label;
        private String lang;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

    }

}
