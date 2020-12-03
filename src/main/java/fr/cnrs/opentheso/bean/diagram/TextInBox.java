package fr.cnrs.opentheso.bean.diagram;

public class TextInBox {

    public final String text;
    public final int height;
    public final int width;
    
    public TextInBox() {
        this.text = "";
        this.width = 0;
        this.height = 0;
    } 

    public TextInBox(String text, int width, int height) {
        this.text = text;
        this.width = width;
        this.height = height;
    }
}
