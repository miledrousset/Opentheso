package fr.cnrs.opentheso.bean.importexport.outils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlLinkExtraction {
 
    private Matcher mTag, mLink;
    private Pattern pTag, pLink;
 
    private static final String HTML_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
    private static final String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
 
    public HtmlLinkExtraction() {
        pTag = Pattern.compile(HTML_TAG_PATTERN);
        pLink = Pattern.compile(HTML_HREF_TAG_PATTERN);
    }
 
    public ArrayList<HTMLLinkElement> extractHTMLLinks(final String sourceHtml) {
 
        ArrayList<HTMLLinkElement> elements = new ArrayList<HTMLLinkElement>();
 
        mTag = pTag.matcher(sourceHtml);
 
        while (mTag.find()) {
 
            String href = mTag.group(1);     // get the values of href
            String linkElem = mTag.group(2); // get the text of link Html Element
 
            mLink = pLink.matcher(href);
 
            while (mLink.find()) {
 
                String link = mLink.group(1);
                HTMLLinkElement htmlLinkElement = new HTMLLinkElement();
                htmlLinkElement.setLinkAddress(link);
                htmlLinkElement.setLinkElement(linkElem);
 
                elements.add(htmlLinkElement);
            }
 
        }
 
        return elements;
 
    }
}
