package fr.cnrs.opentheso.bean.diagram;

import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.concept.NodeConceptTree;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.utils.MessageUtils;

import lombok.RequiredArgsConstructor;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.primefaces.PrimeFaces;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.overlay.ArrowOverlay;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


@SessionScoped
@RequiredArgsConstructor
@Named("conceptsDiagramBean")
public class ConceptsDiagramBean implements Serializable {

    private final TermService termService;
    private final SelectedTheso selectedTheso;
    private final ConceptService conceptService;

    private final int HEIGHT_ELEMENT = 15;
    private final int WIDTH_ELEMENT = 40;

    private String elementSelected;
    private DefaultDiagramModel model;
    private NodeConcept nodeConceptSelected;
    private List<ElementDiagram> elements;
    private Map<TextInBox, List> elementsTreeMap;
    private DefaultTreeForTreeLayout<TextInBox> defaultTreeForTreeLayout;


    public void clear(){
        if(nodeConceptSelected != null){
            nodeConceptSelected.clear();
            nodeConceptSelected = null;
        }
        if(elements != null){
            elements.clear();
            elements = null;
        }      
        if(elementsTreeMap != null){
            elementsTreeMap.clear();
            elementsTreeMap = null;
        }        
        elementSelected = null;
        model = null;
        defaultTreeForTreeLayout = null;
    }

    public void init(String conceptId, String idThesaurus, String idLang) {

        nodeConceptSelected = conceptService.getConceptOldVersion(conceptId, idThesaurus, idLang, -1, -1);
        elementSelected = nodeConceptSelected.getTerm().getLexicalValue();
        var root = new TextInBox(nodeConceptSelected.getTerm().getLexicalValue(), WIDTH_ELEMENT, HEIGHT_ELEMENT);
        elementsTreeMap = new HashMap<>();
        defaultTreeForTreeLayout = new DefaultTreeForTreeLayout<>(root);
        drowDiagram();
    }

    /**
     * Déssiner le diagram
     * Appeler lors de chaque modification de la structure du diagram
     */
    private void drowDiagram() {

        model = new DefaultDiagramModel();
        model.clearElements();
        model.clear();
        model.setMaxConnections(-1);
        model.setConnectionsDetachable(false);

        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#C7B097',lineWidth:1}");
        model.setDefaultConnector(connector);

        DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<>(0, 0);
        var nodeExtentProvider = new TextInBoxNodeExtentProvider();
        TreeLayout<TextInBox> treeLayout = new TreeLayout<>(defaultTreeForTreeLayout, nodeExtentProvider, configuration);

        TextInBoxTreePane panel = new TextInBoxTreePane(treeLayout);

        elements = panel.calculePositions();

        float GAP_BETWEEN_NODES = 3.1f;
        float GAP_BETWEEN_LEVELS = 4;

        for (int i = 0; i < elements.size(); i++) {
            Element root = new Element(elements.get(i).name, (elements.get(i).x * GAP_BETWEEN_NODES) + "px", ((elements.get(i).y - 10) * GAP_BETWEEN_LEVELS) + "px");
            root.setId(i+"");

            if (isRoot(panel, elements.get(i).name)) {
                root.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
            } else {
                root.addEndPoint(createEndPoint(EndPointAnchor.TOP));
                String idConcept = termService.getConceptIdFromPrefLabel(elements.get(i).name,
                        selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang());
                var childs = conceptService.getListConcepts(idConcept, selectedTheso.getSelectedIdTheso(),
                        selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());
                if (!CollectionUtils.isEmpty(childs)) {
                    root.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
                }
            }
            model.addElement(root);
        }


        elements.forEach(element -> {
            if (!isRoot(panel, element.name)) {
                Element elementParent = findElement(getParentElement(element.name).text);
                Element elementDiagram = findElement(element.name);
                model.connect(createConnection(elementParent.getEndPoints().get(
                        elementParent.getEndPoints().size() > 1 ? 1 : 0), elementDiagram.getEndPoints().get(0)));
            }
        });

        Element elementToCenter = null;
        if (!StringUtils.isEmpty(elementSelected)) {
            Element elementParent = findElement(elementSelected);
            if (elementParent != null) {
                elementToCenter = elementParent;
                elementParent.setStyleClass("ui-diagram-element-selected");
            }
        }

        PrimeFaces.current().ajax().update("diagram");
        PrimeFaces.current().ajax().update("dialogDiagram");

        assert elementToCenter != null;
        var posX = Float.parseFloat(elementToCenter.getX().replace("px", ""));
        var posY = Float.parseFloat(elementToCenter.getY().replace("px", ""));

        PrimeFaces.current().executeScript("setScrollPosition("+posX+", "+posY+");");
    }

    public Element findElement(String name) {
        Element elementSearch = null;

        if (!CollectionUtils.isEmpty(model.getElements())) {
            for(int i = 0; i < model.getElements().size(); ++i) {
                Element el = this.model.getElements().get(i);
                if (el.getData().equals(name)) {
                    elementSearch = el;
                    break;
                }
            }
        }
        return elementSearch;
    }

    private Connection createConnection(EndPoint from, EndPoint to) {
        Connection conn = new Connection(from, to);
        conn.getOverlays().add(new ArrowOverlay(8, 8, 1, 1));
        return conn;
    }

    private TextInBox getParentElement(String nameChild) {

        if (elementsTreeMap.isEmpty()) {
            return defaultTreeForTreeLayout.getRoot();
        }

        TextInBox parentElement = null;
        for (Map.Entry mapEntry : elementsTreeMap.entrySet()) {
            var tmp = (List<TextInBox>) elementsTreeMap.get(mapEntry.getKey());
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

    private EndPoint createEndPoint(EndPointAnchor anchor) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setRadius(4);
        endPoint.setStyle("{fillStyle:'#404a4e'}");
        endPoint.setHoverStyle("{fillStyle:'#20282b'}");
        return endPoint;
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

    /**
     * Evenement lors de la selection d'un élément dans le diagram
     */
    public void onElementClicked() {
        String str = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("elementId");
        elementSelected = str.substring(8);
        elementSelected = elements.get(Integer.parseInt(elementSelected)).name;

        TextInBox parentElement = getElementFromTree(elementSelected);

        if (parentElement != null) {
            
            String idConcept = termService.getConceptIdFromPrefLabel(elementSelected, selectedTheso.getSelectedIdTheso(),
                    selectedTheso.getCurrentLang());
            
            nodeConceptSelected = conceptService.getConceptOldVersion(idConcept, selectedTheso.getSelectedIdTheso(),
                    selectedTheso.getCurrentLang(), -1, -1);

            List<TextInBox> temp = defaultTreeForTreeLayout.getChildrenList(parentElement);
            if (!CollectionUtils.isEmpty(temp)) {
                drowDiagram();
                return;
            }
            
            var childs = conceptService.getListConcepts(idConcept, selectedTheso.getSelectedIdTheso(),
                    selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());

            if (CollectionUtils.isEmpty(childs)) {
                MessageUtils.showInformationMessage("Le concept '" + elementSelected + "' n'a pas d'enfant !");
                drowDiagram();
                return;
            }

            List<TextInBox> textInBoxes = new ArrayList<>();

            for (NodeConceptTree child : childs) {
                TextInBox childElement = new TextInBox(child.getTitle(), WIDTH_ELEMENT, HEIGHT_ELEMENT);
                textInBoxes.add(childElement);
                defaultTreeForTreeLayout.addChild(parentElement, childElement);
            }

            elementsTreeMap.put(parentElement, textInBoxes);

            drowDiagram();
        }
    }

    public void closeNoeud() {

        TextInBox elementToDelete = getElementFromTree(elementSelected);
        if (elementToDelete != null) {

            PrimeFaces.current().executeScript("initScrollPosition();");

            Map<TextInBox, List> elementsTreeMapTemp = new HashMap<>();
            DefaultTreeForTreeLayout<TextInBox> treeTemp = new DefaultTreeForTreeLayout<>(defaultTreeForTreeLayout.getRoot());

            if (elementToDelete.text.equalsIgnoreCase(defaultTreeForTreeLayout.getRoot().text)) {
                defaultTreeForTreeLayout = treeTemp;
                elementsTreeMap = elementsTreeMapTemp;
            }

            var childs = defaultTreeForTreeLayout.getChildrenList(defaultTreeForTreeLayout.getRoot());
            deleteConstractTree(treeTemp, elementsTreeMapTemp, childs, defaultTreeForTreeLayout.getRoot(), elementToDelete);

            defaultTreeForTreeLayout = treeTemp;
            elementsTreeMap = elementsTreeMapTemp;
            drowDiagram();
        }
    }

    private void deleteConstractTree(DefaultTreeForTreeLayout<TextInBox> treeTemp, Map<TextInBox, List> elementsTreeMapTemp,
                                     List<TextInBox> childs, TextInBox elementParent, TextInBox elementToDelete) {

        childs.forEach(child -> {
            treeTemp.addChild(elementParent, child);
            if (!elementToDelete.text.equalsIgnoreCase(child.text)) {
                List<TextInBox> childsTmp = defaultTreeForTreeLayout.getChildrenList(child);
                if (!CollectionUtils.isEmpty(childsTmp)) {
                    deleteConstractTree(treeTemp, elementsTreeMapTemp, childsTmp, child, elementToDelete);
                }
            }
        });

        elementsTreeMapTemp.put(elementParent, childs);
    }
}
