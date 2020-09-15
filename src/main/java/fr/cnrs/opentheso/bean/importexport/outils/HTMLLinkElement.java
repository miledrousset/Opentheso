package fr.cnrs.opentheso.bean.importexport.outils;


public class HTMLLinkElement {
 
    String linkElement;
    String linkAddress;
 
    public String getLinkAddress() {
        return linkAddress;
    }
 
    public void setLinkAddress(String linkElement) {
        this.linkAddress = replaceInvalidChar(linkElement);
    }
 
    public String getLinkElement() {
        return linkElement;
    }
 
    public void setLinkElement(String linkAddress) {
        this.linkElement = linkAddress;
    }
 
    private String replaceInvalidChar(String linkElement) {
        linkElement = linkElement.replaceAll("'", "");
        linkElement = linkElement.replaceAll("\"", "");
        return linkElement;
    }
 
    @Override
    public String toString() {
 
        return "Link Address : " + this.linkAddress + ". Link Element : "
                + this.linkElement;
 
    }
}