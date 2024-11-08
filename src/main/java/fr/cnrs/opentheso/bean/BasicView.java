
package fr.cnrs.opentheso.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;


@Named("basicView")
@RequestScoped
public class BasicView {

    private DefaultDiagramModel model;

    @PostConstruct
    public void init() {
        model = new DefaultDiagramModel();
        model.setMaxConnections(-1);
        model.setConnectionsDetachable(false);
        
        Element _4 = new Element("top", "21em", "1em");
        _4.addEndPoint(new DotEndPoint(EndPointAnchor.BOTTOM));
        model.addElement(_4);
        
        Element _5 = new Element("n1", "21em", "11em");  
        _5.addEndPoint(new DotEndPoint(EndPointAnchor.TOP));         
        _5.addEndPoint(new DotEndPoint(EndPointAnchor.BOTTOM));
        model.addElement(_5);
        model.connect(new Connection(_4.getEndPoints().get(0), _5.getEndPoints().get(0), null));          
        
        
        Element _7 = new Element("n2", "1em", "21em"); 
        _7.addEndPoint(new DotEndPoint(EndPointAnchor.TOP));         
        _7.addEndPoint(new DotEndPoint(EndPointAnchor.BOTTOM));   
        _7.addEndPoint(new DotEndPoint(EndPointAnchor.LEFT));  
        _7.addEndPoint(new DotEndPoint(EndPointAnchor.RIGHT));            
        
        Element _8 = new Element("n3", "21em", "21em");   
        _8.addEndPoint(new DotEndPoint(EndPointAnchor.TOP));         
        _8.addEndPoint(new DotEndPoint(EndPointAnchor.BOTTOM));   
        _8.addEndPoint(new DotEndPoint(EndPointAnchor.LEFT));  
        _8.addEndPoint(new DotEndPoint(EndPointAnchor.RIGHT));          
        
        Element _19 = new Element("n21", "41em", "21em");
        _19.addEndPoint(new DotEndPoint(EndPointAnchor.TOP));         
        _19.addEndPoint(new DotEndPoint(EndPointAnchor.BOTTOM));   
        _19.addEndPoint(new DotEndPoint(EndPointAnchor.LEFT));  
        _19.addEndPoint(new DotEndPoint(EndPointAnchor.RIGHT));  
        
        Element _9 = new Element("n4", "21em", "31em");  
        _9.addEndPoint(new DotEndPoint(EndPointAnchor.TOP));         
        _9.addEndPoint(new DotEndPoint(EndPointAnchor.BOTTOM));   
        _9.addEndPoint(new DotEndPoint(EndPointAnchor.LEFT));  
        _9.addEndPoint(new DotEndPoint(EndPointAnchor.RIGHT));

        model.addElement(_7);
        model.addElement(_8);
        model.addElement(_19);
        model.addElement(_9);

        model.connect(new Connection(_5.getEndPoints().get(1), _7.getEndPoints().get(0), null));   
        model.connect(new Connection(_5.getEndPoints().get(1), _8.getEndPoints().get(0), null));         
        model.connect(new Connection(_5.getEndPoints().get(1), _19.getEndPoints().get(0), null));         
        
        model.connect(new Connection(_7.getEndPoints().get(3), _8.getEndPoints().get(2), null)); 
        
        model.connect(new Connection(_8.getEndPoints().get(1), _9.getEndPoints().get(0), null));
    }

    public DiagramModel getModel() {
        return model;
    }
}
