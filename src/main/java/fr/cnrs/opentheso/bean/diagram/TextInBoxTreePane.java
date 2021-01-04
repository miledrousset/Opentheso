package fr.cnrs.opentheso.bean.diagram;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.abego.treelayout.TreeLayout;


public class TextInBoxTreePane extends JComponent {

    private final static int ARC_SIZE = 10;
    private final TreeLayout<TextInBox> treeLayout;
    

    public TextInBoxTreePane(TreeLayout<TextInBox> treeLayout) {
        this.treeLayout = treeLayout;

        Dimension size = treeLayout.getBounds().getBounds().getSize();
        setPreferredSize(size);
    }
    
    public TreeLayout<TextInBox> getTreeLayout() {
        return treeLayout;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    public List<ElementDiagram> calculePositions() {
        List<ElementDiagram> elements = new ArrayList<>();
        for (TextInBox textInBox : treeLayout.getNodeBounds().keySet()) {
            elements.add(paintBox(textInBox));
        }
        return elements;
    }
    
    public TextInBox findElementInTree(String name) {
        TextInBox elementToFind = new TextInBox();
        for (TextInBox textInBox : treeLayout.getNodeBounds().keySet()) {
            if (name.equals(textInBox.text)) {
                elementToFind = textInBox;
                break;
            }
        }
        return elementToFind;
    }

    private ElementDiagram paintBox(TextInBox textInBox) {
        
        Rectangle2D.Double box = treeLayout.getNodeBounds().get(textInBox);
        
        ElementDiagram element = new ElementDiagram();
        element.name = textInBox.text;
        element.x = (int) box.x + ARC_SIZE / 2;
        element.y = (int) box.y + 12 + 1;
        
        return element;
    }
}
