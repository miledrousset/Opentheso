package fr.cnrs.opentheso.bean.diagram;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import java.io.Serializable;
import java.util.*;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;

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
@SessionScoped
public class ConceptsDiagramBean implements Serializable {
    
    private final int GAP_BETWEEN_LEVELS = 4;
    private final int GAP_BETWEEN_NODES = 1;
    private final int HEIGHT_ELEMENT = 20;
    private final int WIDTH_ELEMENT = 40;

    @Inject
    private Connect connect;

    @Inject
    private SelectedTheso selectedTheso;

    private String elementIdSelected;
    private ConceptHelper conceptHelper;
    private DefaultDiagramModel model;
    private StraightConnector connector;

    private List<Element> treeElements; 
    private Map<TextInBox, List> elementsTreeMap;
    private DefaultTreeForTreeLayout<TextInBox> defaultTreeForTreeLayout;
    

    public void init(String conceptId, String idTheso, String idLang) {

        conceptHelper = new ConceptHelper();

        NodeConcept nodeConcept = conceptHelper.getConcept(connect.getPoolConnexion(), conceptId, idTheso, idLang);

        connector = new StraightConnector();
        connector.setPaintStyle("{strokeStyle:'#F47B2A', lineWidth:2}");
        connector.setHoverPaintStyle("{strokeStyle:'#F47B2A'}");
        
        TextInBox root = new TextInBox(nodeConcept.getTerm().getLexical_value(), 
                WIDTH_ELEMENT, HEIGHT_ELEMENT);
        elementsTreeMap = new HashMap<>();
        defaultTreeForTreeLayout = new DefaultTreeForTreeLayout<>(root);
        
        drowDiagram();
    }

    private void drowDiagram() {

        model = new DefaultDiagramModel();
        model.setMaxConnections(-1);
        model.setConnectionsDetachable(false);
        
        DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<>(
                GAP_BETWEEN_LEVELS, GAP_BETWEEN_NODES);

        TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();

        TreeLayout<TextInBox> treeLayout = new TreeLayout<>(defaultTreeForTreeLayout,
                nodeExtentProvider, configuration);

        TextInBoxTreePane panel = new TextInBoxTreePane(treeLayout);
        
        List<ElementDiagram> elements = panel.calculePositions();
        
        
        model = new DefaultDiagramModel();
        model.setMaxConnections(-1);
        model.setConnectionsDetachable(false);
        
        for (ElementDiagram element : elements) {
            Element root = new Element(element.name, element.x + "em", element.y + "em");
            root.setId(element.name);
            
            if (isRoot(panel, element.name)) {
                root.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
            } else {
                root.addEndPoint(createEndPoint(EndPointAnchor.TOP));
                if (hasChilds(panel, element.name)) {
                    root.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
                }
            }
            model.addElement(root);
        }

        for (ElementDiagram element : elements) {
            if (!isRoot(panel, element.name)) {
                Element elementParent = model.findElement(getParentElement(element.name).text);
                Element elementDiagram = model.findElement(element.name);
                model.connect(new Connection(elementParent.getEndPoints().get(
                        elementParent.getEndPoints().size() > 1 ? 1 : 0), elementDiagram.getEndPoints().get(0), connector));
            }
        }

    }

    private TextInBox getParentElement(String nameChild) {

        if (elementsTreeMap.isEmpty()) {
            return defaultTreeForTreeLayout.getRoot();
        }

        TextInBox parentElement = null;
        for (Map.Entry mapEntry : elementsTreeMap.entrySet()) {
            List<TextInBox> tmp = (List<TextInBox>) elementsTreeMap.get(mapEntry.getKey());
            for (TextInBox element : tmp) {
                if (element.text.equalsIgnoreCase(nameChild)) {
                    parentElement = (TextInBox) mapEntry.getKey();
                    break;
                }
            }
        }
        return parentElement;
    }
    
    private boolean isRoot(TextInBoxTreePane panel, String elementDiagramName) {
        
        if (panel.getTreeLayout().getTree().getRoot() == null) {
            return false;
        }
        
        return panel.getTreeLayout().getTree().getRoot().text.equals(elementDiagramName);
    }
    
    private boolean hasChilds(TextInBoxTreePane panel, String elementName) {
        TextInBox textInBox = panel.findElementInTree(elementName);
        
        if (textInBox == null) return false;
        
        Iterable<TextInBox> iterable = panel.getTreeLayout().getTree().getChildren(textInBox);
        
        if (iterable instanceof Collection<?>) {
            return !((Collection<?>)iterable).isEmpty();
        } else {
            return false;
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

    public void onElementClicked() {
        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("elementId");
        elementIdSelected = id.substring(8, id.length());

        Element elementParent = model.findElement(elementIdSelected);
        elementParent.setStyleClass("ui-diagram-element-selected");

        PrimeFaces.current().ajax().update("diagram");
    }

    public void chargerNoeud() {

        String idConcept = conceptHelper.getConceptIdFromPrefLabel(connect.getPoolConnexion(), elementIdSelected,
                selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang());

        ArrayList<NodeConceptTree> childs = conceptHelper.getListConcepts(connect.getPoolConnexion(),
                idConcept, selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());
        
        if (CollectionUtils.isEmpty(childs)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "", "Le concept '" + elementIdSelected + "' n'a pas d'enfant !"));
            return;
        }

        List<TextInBox> textInBoxes = new ArrayList<>();

        TextInBox parentElement = getElementFromTree(elementIdSelected);

        for (NodeConceptTree child : childs) {
            TextInBox childElement = new TextInBox(child.getTitle(), WIDTH_ELEMENT, HEIGHT_ELEMENT);
            textInBoxes.add(childElement);
            defaultTreeForTreeLayout.addChild(parentElement, childElement);
        }

        elementsTreeMap.put(parentElement, textInBoxes);

        drowDiagram();
    }

    private TextInBox getElementFromTree(String elementName) {

        if (elementsTreeMap.isEmpty()) {
            return defaultTreeForTreeLayout.getRoot();
        }

        TextInBox parentElement = null;

        for (Map.Entry mapEntry : elementsTreeMap.entrySet()) {

            if (((TextInBox) mapEntry.getKey()).text.equalsIgnoreCase(elementName)) {
                parentElement = (TextInBox) mapEntry.getKey();
                break;
            }

            List<TextInBox> tmp = (List<TextInBox>) elementsTreeMap.get(mapEntry.getKey());
            for (TextInBox element : tmp) {
                if (element.text.equalsIgnoreCase(elementName)) {
                    parentElement = element;
                    break;
                }
            }
        }
        return parentElement;
    }

    public void closeNoeud() {

        String idConcept = conceptHelper.getConceptIdFromPrefLabel(connect.getPoolConnexion(), elementIdSelected,
                selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang());

        ArrayList<NodeConceptTree> childs = conceptHelper.getListConcepts(connect.getPoolConnexion(),
                idConcept, selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());

        for (NodeConceptTree nodeConceptTree : childs) {
            model.removeElement(model.findElement(nodeConceptTree.getTitle()));
        }
    }

}
