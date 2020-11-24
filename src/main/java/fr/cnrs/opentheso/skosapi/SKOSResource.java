package fr.cnrs.opentheso.skosapi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import fr.cnrs.opentheso.bdd.tools.StringPlus;

/**
 *
 * @author Miled Rousset
 *
 */
public class SKOSResource {

    private String uri;
    private String identifier;
    private SKOSdc sdc;
    private int property;
    private SKOSStatus skosStatus;
    private ArrayList<SKOSDiscussion> messages;
    private ArrayList<SKOSVote> votes;
    private ArrayList<SKOSLabel> labelsList;
    private ArrayList<SKOSRelation> relationsList;
    private ArrayList<SKOSDocumentation> documentationsList;
    private ArrayList<SKOSDate> dateList;
    private ArrayList<SKOSCreator> creatorList;
    private SKOSGPSCoordinates GPSCoordinates;
    private ArrayList<SKOSNotation> notationList;
    private ArrayList<SKOSMatch> matchList;
    
    // Autopostage the complete path to the concept
    private ArrayList<String> paths;
    
    // images
    private ArrayList<String> imageUris;

    /**
     *
     */
    public SKOSResource() {
        skosStatus = null;
        labelsList = new ArrayList<>();
        relationsList = new ArrayList<>();
        documentationsList = new ArrayList<>();
        dateList = new ArrayList<>();
        creatorList = new ArrayList<>();
        GPSCoordinates = new SKOSGPSCoordinates();
        notationList = new ArrayList<>();
        matchList = new ArrayList<>();
        imageUris = new ArrayList<>();
        messages = new ArrayList<>();
        votes = new ArrayList<>();
    }

    public SKOSResource(String uri, int property) {

        skosStatus = null;
        labelsList = new ArrayList<>();
        relationsList = new ArrayList<>();
        documentationsList = new ArrayList<>();
        dateList = new ArrayList<>();
        creatorList = new ArrayList<>();
        GPSCoordinates = new SKOSGPSCoordinates();
        notationList = new ArrayList<>();
        matchList = new ArrayList<>();
        imageUris = new ArrayList<>();
        messages = new ArrayList<>();
        votes = new ArrayList<>();

        this.property = property;
        this.uri = uri;
    }

    public int getProperty() {
        return property;
    }

    /**
     *
     * @return l'Identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     *
     * @param identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public ArrayList<SKOSMatch> getMatchList() {
        return matchList;
    }

    public ArrayList<SKOSCreator> getCreatorList() {
        return creatorList;
    }

    public SKOSGPSCoordinates getGPSCoordinates() {
        return GPSCoordinates;
    }

    public ArrayList<SKOSNotation> getNotationList() {
        return notationList;
    }

    /**
     *
     * @return un String URI de la ressource
     */
    public String getUri() {
        return this.uri;
    }

    /**
     *
     * @return la liste des labels
     */
    public ArrayList<SKOSLabel> getLabelsList() {
        return labelsList;
    }

    /**
     *
     * @return la liste des relations
     */
    public ArrayList<SKOSRelation> getRelationsList() {
        return relationsList;
    }

    /**
     *
     * @return la liste des documenntations
     */
    public ArrayList<SKOSDocumentation> getDocumentationsList() {
        return documentationsList;
    }

    /**
     *
     * @return la liste des dates
     */
    public ArrayList<SKOSDate> getDateList() {
        return dateList;
    }

    /**
     *
     * @param v la valeur du match
     * @param prop le type de match
     */
    public void addMatch(String v, int prop) {
        matchList.add(new SKOSMatch(v, prop));
    }

    /**
     *
     * @param creator le nom
     * @param prop le type
     */
    public void addCreator(String creator, int prop) {
        creatorList.add(new SKOSCreator(creator, prop));
    }

    /**
     *
     * @param notation valeur
     */
    public void addNotation(String notation) {
        notationList.add(new SKOSNotation(notation));
    }

    /**
     * Méthode d'ajout des labels à la ressource, dans une ArrayList
     *
     * @param lab un String label
     * @param lang un String langue conforme xml (fr, eng...)
     * @param prop un int SKOSProperty
     */
    public void addLabel(String lab, String lang, int prop) {
        try {
            SKOSLabel label = new SKOSLabel(lab, lang, prop);
            labelsList.add(label);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Méthode d'ajout des relations à la ressource, dans une ArrayList
     *
     * @param uri un String URI
     * @param prop un int SKOSProperty
     */
    public void addRelation(String uri, int prop) {
        try {
            SKOSRelation relation = new SKOSRelation(uri, prop);
            relationsList.add(relation);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Méthode d'ajout des documentations à la ressource, dans une ArrayList
     *
     * @param text un String texte de la documentation
     * @param lang un String langue conforme xml (fr, en...)
     * @param prop un int SKOSProperty
     */
    public void addDocumentation(String text, String lang, int prop) {
        try {
            SKOSDocumentation documentation = new SKOSDocumentation(text, lang, prop);
            documentationsList.add(documentation);
        } catch (Exception e) {
            e.getMessage();
            System.out.println(e.getMessage());
        }
    }

    public void setProperty(int property) {
        this.property = property;
    }

    public void setLabelsList(ArrayList<SKOSLabel> labelsList) {
        this.labelsList = labelsList;
    }

    public void setRelationsList(ArrayList<SKOSRelation> relationsList) {
        this.relationsList = relationsList;
    }

    public void setDocumentationsList(ArrayList<SKOSDocumentation> documentationsList) {
        this.documentationsList = documentationsList;
    }

    public void setDateList(ArrayList<SKOSDate> dateList) {
        this.dateList = dateList;
    }

    public void setCreatorList(ArrayList<SKOSCreator> creatorList) {
        this.creatorList = creatorList;
    }

    public void setNotationList(ArrayList<SKOSNotation> notationList) {
        this.notationList = notationList;
    }

    public void setMatchList(ArrayList<SKOSMatch> matchList) {
        this.matchList = matchList;
    }

    /**
     * Méthode d'ajout des dates de création et de modification à la ressource,
     * dans une ArrayList
     *
     * @param sDate
     * @param prop un int SKOSProperty
     */
    public void addDate(String sDate, int prop) {
        try {
            SKOSDate date = new SKOSDate(sDate, prop);
            dateList.add(date);
        } catch (Exception e) {
            e.getMessage();
        }
    }
    
    /**
     * Méthode d'ajout des Idetifier type DC
     * @param identifier
     * @param prop un int SKOSProperty
     */
    public void addIdentifier(String identifier, int prop){
        try{
            SKOSdc dc = new SKOSdc(identifier,prop);
            this.sdc = dc;
        }
        catch(Exception e){
            e.getMessage();
        }
    }    

    public void setGPSCoordinates(SKOSGPSCoordinates GPSCoordinates) {
        this.GPSCoordinates = GPSCoordinates;
    }

    public ArrayList<String> getImageUris() {
        return imageUris;
    }

    public void setImageUris(ArrayList<String> imageUris) {
        this.imageUris = imageUris;
    }
    
    public void addImageUri(String uri) {
        this.imageUris.add(uri);
    }


    /**
     *
     * @param uri String URI de la ressource
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public SKOSdc getSdc() {
        return sdc;
    }

    public ArrayList<String> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<String> paths) {
        this.paths = paths;
    }
   
    
    /**
     * Surcharge de la méthode toString() afin de mettre au format xml la
     * ressource
     * @return 
     */
    public String toString() {
        String xmlRessource;
        Iterator<SKOSLabel> itLab = labelsList.iterator();
        Iterator<SKOSRelation> itRel = relationsList.iterator();
        Iterator<SKOSDocumentation> itDoc = documentationsList.iterator();
        Iterator<SKOSDate> itDate = dateList.iterator();

        xmlRessource = "<skos:Concept rdf:about=\"" + uri + "\">\n";

        while (itLab.hasNext()) {
            xmlRessource += "        " + itLab.next().toString();
        }

        while (itRel.hasNext()) {
            xmlRessource += "        " + itRel.next().toString();
        }

        while (itDoc.hasNext()) {
            xmlRessource += "        " + itDoc.next().toString();
        }
        while (itDate.hasNext()) {
            xmlRessource += "        " + itDate.next().toString();
        }
        if(sdc != null) {
            if(sdc.getIdentifier() != null){
                if(!sdc.getIdentifier().isEmpty()){
                    xmlRessource += "        "+sdc.toString();
                }
            }
        }
       

        xmlRessource += "    </skos:Concept>\n";

        return xmlRessource;
    }

    /**
     * sort par ordre alphabetique par raport a une langue donnée et assosi les
     * noms au ID ne change pas l'ordre si isTrad est a true
     *
     * @param isTrad
     * @param langCode
     * @param langCode2
     * @param idToNameHashMap
     * @param idToIsTrad
     * @param resourceChecked
     * @return
     */
    public static Comparator<SKOSResource> sortAlphabeticInLang(boolean isTrad, String langCode, String langCode2, HashMap<String, String> idToNameHashMap, HashMap<String, ArrayList<Integer>> idToIsTrad, ArrayList<String> resourceChecked) {
        return new AlphabeticComparator(isTrad, langCode, langCode2, idToNameHashMap, idToIsTrad, resourceChecked);
    }

    /**
     * sort par ordre alphabetique par raport a une langue donnée et assosi les
     * noms au ID et rempli les assosiation des IDs toutes les information du
     * term sont stocké dans les hashmaps ne change pas l'ordre si isTrad est a
     * true
     *
     * @param isTrad
     * @param langCode
     * @param langCode2
     * @param idToNameHashMap
     * @param idToChildId
     * @param idToDocumentation
     * @param idToMatch
     * @param idToGPS
     * @param resourceChecked
     * @param idToIsTradDiff
     * @return
     */
    public static Comparator<SKOSResource> sortForHiera(boolean isTrad, String langCode, String langCode2, HashMap<String, String> idToNameHashMap, HashMap<String, ArrayList<String>> idToChildId,
            HashMap<String, ArrayList<String>> idToDocumentation, HashMap<String, ArrayList<String>> idToMatch, HashMap<String, String> idToGPS, ArrayList<String> resourceChecked, HashMap<String, ArrayList<Integer>> idToIsTradDiff) {
        return new HieraComparator(isTrad, langCode, langCode2, idToNameHashMap, idToChildId, idToDocumentation, idToMatch, idToGPS, resourceChecked, idToIsTradDiff);
    }

    private static class HieraComparator implements Comparator<SKOSResource> {

        String langCode;
        String langCode2;
        HashMap<String, String> idToNameHashMap;
        HashMap<String, ArrayList<String>> idToChildId;
        HashMap<String, ArrayList<String>> idToDocumentation;
        HashMap<String, ArrayList<String>> idToMatch;
        HashMap<String, String> idToGPS;
        boolean isTrad;
        ArrayList<String> resourceChecked;
        HashMap<String, ArrayList<Integer>> idToIsTradDiff;

        public HieraComparator(boolean isTrad, String langCode, String langCode2, HashMap<String, String> idToNameHashMap, HashMap<String, ArrayList<String>> idToChildId, HashMap<String, ArrayList<String>> idToDocumentation, HashMap<String, ArrayList<String>> idToMatch, HashMap<String, String> idToGPS, ArrayList<String> resourceChecked, HashMap<String, ArrayList<Integer>> idToIsTradDiff) {
            this.langCode = langCode;
            this.langCode2 = langCode2;
            this.idToNameHashMap = idToNameHashMap;
            this.idToChildId = idToChildId;
            this.idToDocumentation = idToDocumentation;
            this.idToMatch = idToMatch;
            this.idToGPS = idToGPS;
            this.isTrad = isTrad;
            this.resourceChecked = resourceChecked;
            this.idToIsTradDiff = idToIsTradDiff;
            
            this.idToNameHashMap.clear();

        }

        @Override
        public int compare(SKOSResource r1, SKOSResource r2) {
            String r1_name = null;
            String r2_name = null;

            String id1 = getIdFromUri(r1.getUri());
            String id2 = getIdFromUri(r2.getUri());

            if (!resourceChecked.contains(id1)) {

                writeIdToChild(r1);
                writeIdToMatch(r1);
                writeIdToGPS(r1);
                checkTrad(r1);
                resourceChecked.add(id1);
            }
            writeIdToDocumentation(r1);

            if (!resourceChecked.contains(id2)) {

                writeIdToChild(r2);
                writeIdToMatch(r2);
                writeIdToGPS(r2);
                checkTrad(r2);
                resourceChecked.add(id2);
            }
            writeIdToDocumentation(r2);

            for (SKOSLabel label : r1.getLabelsList()) {
                if (label.getProperty() == SKOSProperty.prefLabel && label.getLanguage().equals(langCode)) {

                    r1_name = label.getLabel();
                    idToNameHashMap.put(id1, r1_name);

                }
            }

            for (SKOSLabel label2 : r2.getLabelsList()) {
                if (label2.getProperty() == SKOSProperty.prefLabel && label2.getLanguage().equals(langCode)) {
                    r2_name = label2.getLabel();

                    idToNameHashMap.put(id2, r2_name);

                }
            }

            if (isTrad) {
                return 0;
            }

            if (r1_name == null) {
                r1_name = "";
            }
            if (r2_name == null) {
                r2_name = "";
            }

            if (r1_name.length() == 0) {
                if (r2_name.length() == 0) {
                    return 0;               // Both empty - so indicate
                }
                return 1;                   // empty string sorts last
            }
            if (r2_name.length() == 0) {
                return -1;                  // empty string sorts last                  
            }

            return r1_name.compareTo(r2_name);

        }

        private void writeIdToGPS(SKOSResource resource) {
            String key = getIdFromUri(resource.getUri());
            SKOSGPSCoordinates gps = resource.getGPSCoordinates();
            String lat = gps.getLat();
            String lon = gps.getLon();

            if (lat != null && lon != null) {
                idToGPS.put(key, "gps : lat =" + lat + " long =" + lon);
            }
        }

        private void writeIdToMatch(SKOSResource resource) {
            for (SKOSMatch match : resource.getMatchList()) {
                String key = getIdFromUri(resource.getUri());
                String matchTypeName = null;
                int prop = match.getProperty();
                switch (prop) {
                    case SKOSProperty.exactMatch:
                        matchTypeName = "exactMatch";
                        break;
                    case SKOSProperty.closeMatch:
                        matchTypeName = "closeMatch";
                        break;
                    case SKOSProperty.broadMatch:
                        matchTypeName = "broadMatch";
                        break;
                    case SKOSProperty.relatedMatch:
                        matchTypeName = "relatedMatch";
                        break;
                    case SKOSProperty.narrowMatch:
                        matchTypeName = "narrowMatch";
                        break;
                }

                ArrayList<String> mat = idToMatch.get(key);

                if (mat == null) {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(matchTypeName + ": " + match.getValue());
                    idToMatch.put(key, temp);
                } else {
                    String matToAdd = matchTypeName + ": " + match.getValue();
                    if (!mat.contains(matToAdd)) {
                        mat.add(matToAdd);
                    }
                }

            }

        }

        private void writeIdToDocumentation(SKOSResource resource) {
            for (SKOSDocumentation documentation : resource.getDocumentationsList()) {
                String documentationText = null;

                if (documentation.getLanguage().equals(langCode)) {
                    documentationText = documentation.getText();
                } else {
                    continue;
                }

                String key = getIdFromUri(resource.getUri());
                String docTypeName = null;
                int prop = documentation.getProperty();

                switch (prop) {
                    case SKOSProperty.definition:
                        docTypeName = "definition";
                        break;
                    case SKOSProperty.scopeNote:
                        docTypeName = "scopeNote";
                        break;
                    case SKOSProperty.example:
                        docTypeName = "example";
                        break;
                    case SKOSProperty.historyNote:
                        docTypeName = "historyNote";
                        break;
                    case SKOSProperty.editorialNote:
                        docTypeName = "editorialNote";
                        break;
                    case SKOSProperty.changeNote:
                        docTypeName = "changeNote";
                        break;
                    case SKOSProperty.note:
                        docTypeName = "note";
                        break;
                    default:
                        docTypeName = "note";
                        break;
                }
                ArrayList<String> doc = idToDocumentation.get(key);

                if (doc == null) {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(docTypeName + ": " + documentationText);
                    idToDocumentation.put(key, temp);
                } else {
                    String docToAdd = docTypeName + ": " + documentationText;
                    if (!doc.contains(docToAdd)) {
                        doc.add(docToAdd);
                    }
                }
            }
        }

        private void writeIdToChild(SKOSResource resource) {
            for (SKOSRelation relation : resource.getRelationsList()) {
                String key;
                if (relation.getProperty() == SKOSProperty.narrower 
                        || relation.getProperty() == SKOSProperty.narrowerGeneric
                        || relation.getProperty() == SKOSProperty.narrowerInstantial
                        || relation.getProperty() == SKOSProperty.narrowerPartitive) {
                    key = getIdFromUri(resource.getUri());
                    ArrayList<String> child = idToChildId.get(key);
                    if (child == null) {
                        ArrayList<String> temp = new ArrayList<>();
                        temp.add(getIdFromUri(relation.getTargetUri()));
                        idToChildId.put(key, temp);
                    } else {
                        String childId = getIdFromUri(relation.getTargetUri());
                        if (!child.contains(childId)) {
                            child.add(childId);
                        }
                    }

                }
            }
        }

        private void checkTrad(SKOSResource resource) {

            String key = getIdFromUri(resource.getUri());

            int lang1Doc = 0;
            int lang2Doc = 0;

            for (SKOSDocumentation doc : resource.getDocumentationsList()) {

                if (doc.getLanguage().equals(langCode)) {
                    lang1Doc++;
                }
                if (doc.getLanguage().equals(langCode2)) {
                    lang2Doc++;
                }

                int diff = Math.abs(lang1Doc - lang2Doc);

                ArrayList<Integer> newIsTrad = new ArrayList<>();

                for (int i = 0; i < diff; i++) {
                    newIsTrad.add(SKOSProperty.note);
                }
                idToIsTradDiff.put(key, newIsTrad);
            }
        }
    }

    private static class AlphabeticComparator implements Comparator<SKOSResource> {

        String langCode;
        String langCode2;
        HashMap<String, String> idToNameHashMap;
        boolean isTrad;
        HashMap<String, ArrayList<Integer>> idToIsTrad;
        ArrayList<String> resourceChecked;

        public AlphabeticComparator(boolean isTrad, String langCode, String langCode2, HashMap<String, String> idToNameHashMap, HashMap<String, ArrayList<Integer>> idToIsTrad, ArrayList<String> resourceChecked) {
            this.langCode = langCode;
            this.idToNameHashMap = idToNameHashMap;
            this.isTrad = isTrad;
            this.langCode2 = langCode2;
            this.idToIsTrad = idToIsTrad;
            this.resourceChecked = resourceChecked;
            
            this.idToNameHashMap.clear();
        }

        @Override
        public int compare(SKOSResource r1, SKOSResource r2) {

            String r1_name = null;
            String r2_name = null;

            String id1 = getIdFromUri(r1.getUri());
            String id2 = getIdFromUri(r2.getUri());

            if (!resourceChecked.contains(id1)) {
                checkTrad(r1);
                resourceChecked.add(id1);
            }
            if (!resourceChecked.contains(id2)) {
                checkTrad(r2);
                resourceChecked.add(id2);

            }

            for (SKOSLabel label : r1.getLabelsList()) {
                if (label.getProperty() == SKOSProperty.prefLabel && label.getLanguage().equals(langCode)) { // to test
                    r1_name = label.getLabel();
                    idToNameHashMap.put(id1, r1_name);

                }
            }
            for (SKOSLabel label2 : r2.getLabelsList()) {
                if (label2.getProperty() == SKOSProperty.prefLabel && label2.getLanguage().equals(langCode)) {
                    r2_name = label2.getLabel();
                    idToNameHashMap.put(id2, r2_name);

                }

            }
            if (isTrad) {
                return 0;
            }

            if (r1_name == null) {
                r1_name = "";
            }
            if (r2_name == null) {
                r2_name = "";
            }

            if (r1_name.length() == 0) {
                if (r2_name.length() == 0) {
                    return 0;               // Both empty - so indicate
                }
                return 1;                   // empty string sorts last
            }
            if (r2_name.length() == 0) {
                return -1;                  // empty string sorts last                  
            }

            return r1_name.compareTo(r2_name);

        }

        private void checkTrad(SKOSResource resource) {

            int lang1Pref = 0;
            int lang2Pref = 0;

            int lang1Alt = 0;
            int lang2Alt = 0;

            int lang1Doc = 0;
            int lang2Doc = 0;

            //prefLabel
            for (SKOSLabel label : resource.getLabelsList()) {
                if (label.getLanguage().equals(langCode) && label.getProperty() == SKOSProperty.prefLabel) {
                    lang1Pref++;
                }
                if (label.getLanguage().equals(langCode2) && label.getProperty() == SKOSProperty.prefLabel) {
                    lang2Pref++;
                }

                if (lang1Pref > 0 && lang2Pref > 0) {
                    lang1Pref--;
                    lang2Pref--;

                    String key = getIdFromUri(resource.getUri());
                    ArrayList<Integer> trad = idToIsTrad.get(key);
                    if (trad == null) {
                        ArrayList<Integer> newIsTrad = new ArrayList<>();
                        newIsTrad.add(SKOSProperty.prefLabel);
                        idToIsTrad.put(key, newIsTrad);
                    } else {
                        trad.add(SKOSProperty.prefLabel);
                    }
                }
            }
            //altLabel
            for (SKOSLabel label : resource.getLabelsList()) {

                if (label.getLanguage().equals(langCode) && label.getProperty() == SKOSProperty.altLabel) {
                    lang1Alt++;
                }
                if (label.getLanguage().equals(langCode2) && label.getProperty() == SKOSProperty.altLabel) {
                    lang2Alt++;
                }

                if (lang1Alt > 0 && lang2Alt > 0) {
                    lang1Alt--;
                    lang2Alt--;

                    String key = getIdFromUri(resource.getUri());
                    ArrayList<Integer> trad = idToIsTrad.get(key);
                    if (trad == null) {
                        ArrayList<Integer> newIsTrad = new ArrayList<>();
                        newIsTrad.add(SKOSProperty.altLabel);
                        idToIsTrad.put(key, newIsTrad);
                    } else {
                        trad.add(SKOSProperty.altLabel);
                    }
                }

            }

            for (SKOSDocumentation doc : resource.getDocumentationsList()) {

                if (doc.getLanguage().equals(langCode)) {
                    lang1Doc++;
                }
                if (doc.getLanguage().equals(langCode2)) {
                    lang2Doc++;
                }

                if (lang1Doc > 0 && lang2Doc > 0) {
                    lang1Doc--;
                    lang2Doc--;

                    String key = getIdFromUri(resource.getUri());
                    ArrayList<Integer> trad = idToIsTrad.get(key);
                    if (trad == null) {
                        ArrayList<Integer> newIsTrad = new ArrayList<>();
                        newIsTrad.add(SKOSProperty.note);
                        idToIsTrad.put(key, newIsTrad);
                    } else {
                        trad.add(SKOSProperty.note);
                    }
                }

            }
        }
    }

    public static String getIdFromUri(String uri) {
        if (uri.contains("idg=")) {
            if (uri.contains("&")) {
                uri = uri.substring(uri.indexOf("idg=") + 4, uri.indexOf("&"));
            } else {
                uri = uri.substring(uri.indexOf("idg=") + 4, uri.length());
            }
        } else {
            if (uri.contains("idc=")) {
                if (uri.contains("&")) {
                    uri = uri.substring(uri.indexOf("idc=") + 4, uri.indexOf("&"));
                } else {
                    uri = uri.substring(uri.indexOf("idc=") + 4, uri.length());
                }
            } else {
                if (uri.contains("#")) {
                    uri = uri.substring(uri.indexOf("#") + 1, uri.length());
                } else {
                    uri = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
                }
            }
        }

        StringPlus stringPlus = new StringPlus();
        uri = stringPlus.normalizeStringForIdentifier(uri);
        return uri;
    }

    public SKOSStatus getSkosStatus() {
        return skosStatus;
    }

    public void setSkosStatus(String[] statusDatas) {
        skosStatus = new SKOSStatus();
        skosStatus.setIdStatus(statusDatas[0]);
        skosStatus.setMessage(statusDatas[1]);
        skosStatus.setIdUser(statusDatas[2]);
        skosStatus.setDate(statusDatas[3]);
        skosStatus.setIdConcept(identifier);
        skosStatus.setIdThesaurus(uri.substring(uri.indexOf("idt=")+4, uri.length()));
    }

    public void setSkosStatus(SKOSStatus skosStatus) {
        this.skosStatus = skosStatus;
    }

    public ArrayList<SKOSDiscussion> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<SKOSDiscussion> messages) {
        this.messages = messages;
    }

    public void addMessage(String[] voteDatas) {
        SKOSDiscussion skosDiscussion = new SKOSDiscussion();
        skosDiscussion.setDate(voteDatas[0]);
        skosDiscussion.setIdUser(voteDatas[1] != null ? Integer.parseInt(voteDatas[1]) : null);
        skosDiscussion.setMsg(voteDatas[2]);
        messages.add(skosDiscussion);
    }

    public void addMessage(SKOSDiscussion skosDiscussion) {
        messages.add(skosDiscussion);
    }

    public ArrayList<SKOSVote> getVotes() {
        return votes;
    }

    public void addVote(String[] voteDatas) {
        SKOSVote skosVote = new SKOSVote();
        skosVote.setIdThesaurus(voteDatas[0]);
        skosVote.setIdConcept(voteDatas[1]);
        skosVote.setIdNote(voteDatas[2]);
        skosVote.setIdUser(voteDatas[3] != null ? Integer.parseInt(voteDatas[3]) : null);
        skosVote.setTypeVote(voteDatas[4]);
        votes.add(skosVote);
    }

    public void addVote(SKOSVote skosVote) {
        votes.add(skosVote);
    }
}
