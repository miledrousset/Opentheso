package fr.cnrs.opentheso.bean.diagram;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections4.CollectionUtils;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.StraightConnector;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;


@Named("conceptsDiagramBean")
@RequestScoped
public class ConceptsDiagramBean {

    @Inject
    private Connect connect;

    @Inject
    private SelectedTheso selectedTheso;

    private ConceptHelper conceptHelper;
    private DefaultDiagramModel model;
    private StraightConnector connector;
    private HashMap<Integer, Integer> positions;

    
    public void init(String conceptId, String idTheso, String idLang) {

        conceptHelper = new ConceptHelper();
        
        model = new DefaultDiagramModel();
        model.setMaxConnections(-1);
        model.setConnectionsDetachable(false);

        NodeConcept nodeConcept = conceptHelper.getConcept(connect.getPoolConnexion(), conceptId, idTheso, idLang);
        
        ArrayList<NodeConceptTree> childs = conceptHelper.getListConcepts(connect.getPoolConnexion(),
                conceptId, idTheso, selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());
        
        int positionX = 3 + (childs.size() * 13 / 2) - 7;
        
        Element root = new Element(nodeConcept.getTerm().getLexical_value(), positionX + "em", "5em");
        root.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
        root.setStyleClass("ui-diagram-element");
        model.addElement(root);

        connector = new StraightConnector();
        connector.setPaintStyle("{strokeStyle:'#F47B2A', lineWidth:2}");
        connector.setHoverPaintStyle("{strokeStyle:'#F47B2A'}");

        positions = new HashMap<>();
        positions.put(1, 1);

        if (!CollectionUtils.isEmpty(childs)) {
            addChilds(root, childs, idTheso, 3, 5, true);
        }
        
        PrimeFaces.current().executeScript("PF('bui').hide();");
    }

    private void addChilds (Element elementParent, ArrayList<NodeConceptTree> childs, String idTheso, int posX, int posY, boolean isTop) {

        int step = 13;
        int positionX = posX == 3 ? 3 : posX;
        int positionY = posY + 10;

        for (int niveau = 0; niveau < childs.size(); niveau++) {

            if (childs.get(niveau).getIdConcept() == null) continue;

            String label = childs.get(niveau).getTitle().isEmpty() ? "(" + childs.get(niveau).getIdConcept() + ")" :
                    childs.get(niveau).getTitle();

            Element child = new Element(label, positionX + "em", positionY + "em");
            child.addEndPoint(createEndPoint(EndPointAnchor.TOP));
            child.setStyleClass("ui-diagram-element");
            model.addElement(child);
            model.connect(new Connection(elementParent.getEndPoints().get(isTop ? 0 : 1), child.getEndPoints().get(0), connector));

            positionX += step;

            ArrayList<NodeConceptTree> childsConcept = conceptHelper.getListConcepts(connect.getPoolConnexion(),
                    childs.get(niveau).getIdConcept(), idTheso, selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());

            if (!CollectionUtils.isEmpty(childsConcept)) {
                child.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
                int childPosX = 3;
                int positionTemp = 0;

                for (int pos = 0; pos < niveau; pos++) {
                    ArrayList<NodeConceptTree> tmp = conceptHelper.getListConcepts(connect.getPoolConnexion(),
                            childs.get(pos).getIdConcept(), idTheso, selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());
                    positionTemp += tmp.size();
                }

                if (positionTemp > 0) {
                    childPosX = positionTemp * step + 3;
                }
                addChilds (child, childsConcept, idTheso, childPosX, positionY, false);
            }
        }
    }
    

    private EndPoint createEndPoint(EndPointAnchor anchor) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setStyle("{fillStyle:'#404a4e'}");
        endPoint.setHoverStyle("{fillStyle:'#20282b'}");

        return endPoint;
    }

    public DiagramModel getModel() {
        return model;
    }

}
