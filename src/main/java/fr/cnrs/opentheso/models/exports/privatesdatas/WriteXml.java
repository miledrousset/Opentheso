package fr.cnrs.opentheso.models.exports.privatesdatas;

/**
 *
 * @author antonio.perez
 */

import java.util.ArrayList;

import fr.cnrs.opentheso.models.exports.privatesdatas.tables.Table;
import fr.cnrs.opentheso.utils.StringUtils;

public class WriteXml {

    private StringBuilder xml = new StringBuilder();
    
    
    public WriteXml() {
    }
    
    public void WriteIntoXML(ArrayList<Table> DataTable, String Tablename) {
        startTable(Tablename);
        for (Table user : DataTable) {
            startLine();
            for (LineOfData lineOfData : user.getLineOfDatas()) {
                writeLine(lineOfData.getColomne(), lineOfData.getValue());
            }
            endLine();
        }
        endTable();
    }
    
    private void startTable(String tableName) {
        xml.append("\n");
        xml.append( "    ");
        xml.append("<table nom =\"" + tableName + "\">");
    }
    
    private StringBuilder endTable() {
        xml.append("\n");
        xml.append("    ");
        xml.append("</table>");
        return xml;
    }    
    
    private void startLine() {
        xml.append("\n");
        xml.append("        ");
        xml.append("<ligne>");
    }
    
    private void endLine() {
        xml.append("\n");
        xml.append("        ");
        xml.append("</ligne>");
    }    
    
    private void writeLine(String colomne, String value) {
        value = StringUtils.normalizeStringForXml(value);
        xml.append("\n");
        xml.append("            ");
        xml.append("<" + colomne + ">");
        xml.append(value);
        xml.append("</" + colomne + ">");
    }
    
    public void writeHead(){
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("\n");
    }
    
    public void start(){
        xml.append("<tables>");
    }
    
    public void end(){
        xml.append("\n");
        xml.append("</tables>");
    }    

    public StringBuilder getXml() {
        return xml;
    }

    public void setXml(StringBuilder xml) {
        this.xml = xml;
    }



    
}
