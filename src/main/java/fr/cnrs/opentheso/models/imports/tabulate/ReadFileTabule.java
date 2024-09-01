package fr.cnrs.opentheso.models.imports.tabulate;

import java.io.InputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.models.exports.tabulate.FieldsSkos;
import fr.cnrs.opentheso.models.exports.tabulate.TabulateDocument;

public class ReadFileTabule {
    
    private ArrayList <TabulateDocument> tabulateDocumentList;
    private ArrayList <String> fieldsList;
    private BufferedReader br;
    private String colonneSeparate;
    private String fieldSeparate;
    private String subfieldSeparate;
    private String formatDate;
    
    public ReadFileTabule() {
    }

    
    /*public void readFile(HikariDataSource ds, InputStream filename, 
            String formatDate, boolean useArk, String adressSite) throws Exception {
*/
    public boolean setReadFile(InputStream filename,
            String colonneSeparate,
            String fieldSeparate,
            String subfieldSeparate,
            String formatDate) {

        this.colonneSeparate = colonneSeparate;
        this.fieldSeparate = fieldSeparate;
        this.subfieldSeparate = subfieldSeparate;
        this.formatDate = formatDate;
        InputStreamReader isr;
        try {
            isr = new InputStreamReader(filename, "UTF-8");
            br = new BufferedReader(isr);
            return true;
        } catch (UnsupportedEncodingException ex1) {
            Logger.getLogger(ReadFileTabule.class.getName()).log(Level.SEVERE, null, ex1);
            return false;
        }
    }

    public boolean setFields(){
    
        // separate of colonne ";"
        // separate multivalues "$"

        fieldsList = new ArrayList<>();
        String ligne;

        try {
            while ((ligne = br.readLine()) != null)
            {
                // Retourner la ligne dans un tableau
                String[] data = ligne.trim().split(colonneSeparate);
                if(data.length == 0) continue;

                // Afficher le contenu du tableau
                for (String val : data)
                {
                    // Fields
                    fieldsList.add(val.trim());
                }
                if(!isFieldsValid(fieldsList)) {
                    br.close();
                    return false;
                }
                return true;

            }
        } catch (IOException ex) {
            Logger.getLogger(ReadFileTabule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * cette fonction permet d'ajouter la valeur à un champ suivant son index 
     * @return  
     */
    public boolean setDatas(){
    
        // separate of colonne ";"
        // separate multivalues "$"

        tabulateDocumentList = new ArrayList<>();
        
        String ligne;
        try {
            while ((ligne = br.readLine()) != null)
            {
                // Retourner la ligne dans un tableau
                String[] data = ligne.trim().split(colonneSeparate);
                if(data.length == 0) continue;

                // Afficher le contenu du tableau
                if(!addDatas(data)) return false;

            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ReadFileTabule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * Permet d'ajouter les données d'une ligne à la classe TabulateDocument.
     * @param valeurs
     * @return 
     */
    private boolean addDatas(String[] valeurs) {
        TabulateDocument tabulateDocument = new TabulateDocument();
        SimpleDateFormat formatDate1 = new SimpleDateFormat(formatDate);
        
        for (int i=0; i< valeurs.length; i++) {
            switch (i) {
                case FieldsSkos.id_:
                    tabulateDocument.setId(valeurs[i].trim());
                    break;
                case FieldsSkos.IdArk_:
                    tabulateDocument.setIdArk(valeurs[i].trim());
                    break;
                case FieldsSkos.type_:
                    tabulateDocument.setType(valeurs[i].trim());
                    break;
                case FieldsSkos.prefLabel_:
                    
                    // cas de mulivalues
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            if(!value.contains(subfieldSeparate)) return false;
                            String[] label = value.split(subfieldSeparate);
                            tabulateDocument.addPrefLabel(label[0].trim(), label[1].trim());
                        }
                    }
                    else {
                        if(!valeurs[i].contains(subfieldSeparate)) return false;
                            String[] label = valeurs[i].split(subfieldSeparate);
                            tabulateDocument.addPrefLabel(label[0].trim(), label[1].trim());
                    }
                    break;
                case FieldsSkos.altLabel_:
                    // cas de mulivalues
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            if(!value.contains(subfieldSeparate)) return false;
                            String[] label = value.split(subfieldSeparate);
                            tabulateDocument.addAltLabel(label[0].trim(), label[1].trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){
                            if(!valeurs[i].contains(subfieldSeparate)) return false;
                            String[] label = valeurs[i].split(subfieldSeparate);
                            tabulateDocument.addAltLabel(label[0].trim(), label[1].trim());
                        }
                    }
                    break;
                case FieldsSkos.inScheme_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            tabulateDocument.addInScheme(value.trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){
                            tabulateDocument.addInScheme(valeurs[i].trim());
                        }
                    }
                    break; 
                case FieldsSkos.broader_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            tabulateDocument.addBroader(value.trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){
                            tabulateDocument.addBroader(valeurs[i].trim());
                        }
                    }
                    break;
                case FieldsSkos.narrower_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            tabulateDocument.addNarrower(value.trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){
                            tabulateDocument.addNarrower(valeurs[i].trim());
                        }
                    }
                    break;
                case FieldsSkos.related_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            tabulateDocument.addRelated(value.trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){
                            tabulateDocument.addRelated(valeurs[i].trim());
                        }
                    }
                    break;
                case FieldsSkos.alignment_:
                    // cas de mulivalues
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            if(!value.contains(subfieldSeparate)) return false;
                            String[] label = value.split(subfieldSeparate);
                            tabulateDocument.addAlignments(label[0].trim(), label[1].trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){
                            if(!valeurs[i].contains(subfieldSeparate)) return false;
                                String[] label = valeurs[i].split(subfieldSeparate);
                                tabulateDocument.addAlignments(label[0].trim(), label[1].trim());
                        }
                    }
                    break;
                case FieldsSkos.definition_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            if(!value.contains(subfieldSeparate)) return false;
                            String[] label = value.split(subfieldSeparate);
                            tabulateDocument.addDefinition(label[0].trim(), label[1].trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){                        
                            if(!valeurs[i].contains(subfieldSeparate)) return false;
                                String[] label = valeurs[i].split(subfieldSeparate);
                                tabulateDocument.addDefinition(label[0].trim(), label[1].trim());
                        }
                    }
                    break;                     
                case FieldsSkos.note_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            if(!value.contains(subfieldSeparate)) return false;
                            String[] label = value.split(subfieldSeparate);
                            tabulateDocument.addNote(label[0].trim(), label[1].trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){                        
                            if(!valeurs[i].contains(subfieldSeparate)) return false;
                                String[] label = valeurs[i].split(subfieldSeparate);
                                tabulateDocument.addDefinition(label[0].trim(), label[1].trim());
                        }
                    }
                    break; 
                case FieldsSkos.scopeNote_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            if(!value.contains(subfieldSeparate)) return false;
                            String[] label = value.split(subfieldSeparate);
                            tabulateDocument.addScopeNote(label[0].trim(), label[1].trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){                        
                            if(!valeurs[i].contains(subfieldSeparate)) return false;
                                String[] label = valeurs[i].split(subfieldSeparate);
                                tabulateDocument.addScopeNote(label[0].trim(), label[1].trim());
                        }
                    }
                    break;
                case FieldsSkos.historyNote_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            if(!value.contains(subfieldSeparate)) return false;
                            String[] label = value.split(subfieldSeparate);
                            tabulateDocument.addHistoryNote(label[0].trim(), label[1].trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){                        
                            if(!valeurs[i].contains(subfieldSeparate)) return false;
                                String[] label = valeurs[i].split(subfieldSeparate);
                                tabulateDocument.addHistoryNote(label[0].trim(), label[1].trim());
                        }
                    }
                    break;
                case FieldsSkos.editorialNote_:
                    if(valeurs[i].contains(fieldSeparate)) {
                        String[] values = valeurs[i].split(fieldSeparate);
                        for (String value : values) {
                            if(!value.contains(subfieldSeparate)) return false;
                            String[] label = value.split(subfieldSeparate);
                            tabulateDocument.addEditorialNote(label[0].trim(), label[1].trim());
                        }
                    }
                    else {
                        if(!valeurs[i].trim().isEmpty()){                        
                            if(!valeurs[i].contains(subfieldSeparate)) return false;
                                String[] label = valeurs[i].split(subfieldSeparate);
                                tabulateDocument.addEditorialNote(label[0].trim(), label[1].trim());
                        }
                    }
                    break;
                case FieldsSkos.createdDate_:
                    if(!valeurs[i].trim().isEmpty()){
                        try {
                            tabulateDocument.setCreated(formatDate1.parse(valeurs[i].trim()));
                        } catch (ParseException ex) {
                            System.err.println(ex.toString());
                            return false;
                        }
                    }
                    break; 
                case FieldsSkos.modifiedDate_:
                    if(!valeurs[i].trim().isEmpty()){
                        try {
                            tabulateDocument.setModified(formatDate1.parse(valeurs[i].trim()));
                        } catch (ParseException ex) {
                            System.err.println(ex.toString());
                            return false;
                        }
                    }
                    break;
            }
        }
        tabulateDocumentList.add(tabulateDocument);
        return true;
    }
    
    /**
     * Permet de comparer les champs du fichier tabulé avec les champs acceptés.
     * @param fields
     * @return true or false
     */
    private boolean isFieldsValid(ArrayList<String> fieldsOfFile) {
        
        if(fieldsOfFile.isEmpty())
            return false;
        
        if(!fieldsOfFile.contains(FieldsSkos.IdArk)) {
            return false;
        }

        if(!fieldsOfFile.contains(FieldsSkos.alignment)) {
            return false;
        }       

        if(!fieldsOfFile.contains(FieldsSkos.altLabel)) {
            return false;
        }

        if(!fieldsOfFile.contains(FieldsSkos.broader)) {
            return false;
        }

        if(!fieldsOfFile.contains(FieldsSkos.createdDate)) {
            return false;
        }

        if(!fieldsOfFile.contains(FieldsSkos.definition)) {
            return false;
        }            

        if(!fieldsOfFile.contains(FieldsSkos.editorialNote)) {
            return false;
        }

        if(!fieldsOfFile.contains(FieldsSkos.historyNote)) {
            return false;
        }      

        if(!fieldsOfFile.contains(FieldsSkos.id)) {
            return false;
        }      

        if(!fieldsOfFile.contains(FieldsSkos.inScheme)) {
            return false;
        }       

        if(!fieldsOfFile.contains(FieldsSkos.modifiedDate)) {
            return false;
        }      

        if(!fieldsOfFile.contains(FieldsSkos.narrower)) {
            return false;
        }

        if(!fieldsOfFile.contains(FieldsSkos.prefLabel)) {
            return false;
        }      

        if(!fieldsOfFile.contains(FieldsSkos.related)) {
            return false;
        }            

        if(!fieldsOfFile.contains(FieldsSkos.scopeNote)) {
            return false;
        }

        if(!fieldsOfFile.contains(FieldsSkos.type)) {
            return false;
        }
        return true;
    }

    public ArrayList<TabulateDocument> getTabulateDocumentList() {
        return tabulateDocumentList;
    }

    public ArrayList<String> getFieldsList() {
        return fieldsList;
    }

}
