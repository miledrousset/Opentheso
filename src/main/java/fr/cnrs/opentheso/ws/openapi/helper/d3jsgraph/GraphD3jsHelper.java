package fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph;

import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.models.thesaurus.Thesaurus;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.models.concept.ConceptIdLabel;
import fr.cnrs.opentheso.models.concept.ConceptLabel;
import fr.cnrs.opentheso.models.concept.ConceptRelation;
import fr.cnrs.opentheso.models.concept.NodeFullConcept;
import fr.cnrs.opentheso.models.concept.NodeUri;
import fr.cnrs.opentheso.models.group.NodeGroupLabel;
import fr.cnrs.opentheso.models.group.NodeGroupTraductions;
import fr.cnrs.opentheso.models.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.models.exports.UriHelper;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;

import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.ThesaurusService;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GraphD3jsHelper {

    private final ConceptHelper conceptHelper;
    private final UriHelper uriHelper;
    private final PreferenceService preferenceService;
    private final ThesaurusService thesaurusService;
    private final GroupService groupService;
    private final ConceptService conceptService;

    private NodeGraphD3js nodeGraphD3js;
    private Preferences nodePreference;

    public void initGraph(){
        nodeGraphD3js = new NodeGraphD3js();
        nodeGraphD3js.setNodes(new ArrayList<>());
        nodeGraphD3js.setRelationships(new ArrayList<>()); 
    }
    
    public void getGraphByTheso(String idTheso, String idLang, boolean limit){

        nodePreference = preferenceService.getThesaurusPreferences(idTheso);

        uriHelper.setIdTheso(idTheso);
        uriHelper.setNodePreference(nodePreference);
        
        // récupérer les conceptScheme
        NodeThesaurus nodeThesaurus = thesaurusService.getNodeThesaurus(idTheso);
        if(nodeThesaurus == null){
            return;
        }
        nodeGraphD3js.addNewNode(getDatasOfThesaurus(nodeThesaurus));
        
        ArrayList<NodeUri> nodeTTs = conceptHelper.getAllTopConcepts(idTheso);
        nodeGraphD3js.getRelationships().addAll(getRelationshipOfTheso(nodeTTs, idTheso));        
        
        /// récupérer les concepts
        List<String> listIdConcept = conceptHelper.getAllIdConceptOfThesaurus(idTheso);
        if (listIdConcept.size() > 2000) {
            listIdConcept = listIdConcept.subList(0, 2000);
        }
        
        for (String idC : listIdConcept) {
            NodeFullConcept nodeFullConcept = conceptHelper.getConcept2(idC, idTheso, idLang, -1, -1); 
            nodeGraphD3js.addNewNode(getDatasOfNode(nodeFullConcept));
            nodeGraphD3js.getRelationships().addAll(getRelationship(nodeFullConcept, idTheso, idLang));
        }
    }

    public void getGraphByConcept(String idTheso, String idConcept, String idLang){
        
        nodePreference = preferenceService.getThesaurusPreferences(idTheso);

        uriHelper.setIdTheso(idTheso);
        uriHelper.setNodePreference(nodePreference);

        NodeThesaurus nodeThesaurus = thesaurusService.getNodeThesaurus(idTheso);
        if(nodeThesaurus == null){
            return;
        }        
        nodeGraphD3js.addNewNode(getDatasOfThesaurus(nodeThesaurus));
        
        if(!conceptService.isIdExiste(idConcept)){
            return;
        }
        
        /// récupérer les concepts
        List<String> listIdConcept = conceptService.getIdsOfBranch2(idTheso, idConcept);
        if (listIdConcept.size() > 2000) {
            listIdConcept = listIdConcept.subList(0, 2000);
        }        
        for (String idC : listIdConcept) {
            NodeFullConcept nodeFullConcept = conceptHelper.getConcept2(idC, idTheso, idLang, -1, -1 ); 
            nodeGraphD3js.addNewNode(getDatasOfNode(nodeFullConcept));
            nodeGraphD3js.getRelationships().addAll(getRelationship(nodeFullConcept, idTheso, idLang));
        }
    }    
    
    
    
    private Node getDatasOfThesaurus(NodeThesaurus nodeThesaurus){
        Node node = new Node();
        
        node.setId(uriHelper.getUriForTheso(nodeThesaurus.getIdThesaurus(), nodeThesaurus.getIdArk(), ""));
        List<String> labels = new ArrayList<>();
        labels.add("Resource");
        labels.add("skos__ConceptScheme");
        node.setLabels(labels);
        
        Properties properties = new Properties();
        properties.setUri(uriHelper.getUriForTheso(nodeThesaurus.getIdThesaurus(), "", ""));
        properties.setPropertiesLabel("skos__prefLabel");

        List<String> prefLabels = new ArrayList<>();
        for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {
            prefLabels.add(thesaurus.getTitle() + "@" + thesaurus.getLanguage());
        }
        properties.setPrefLabels(prefLabels);          
        node.setProperties(properties);
        return node;
    }    
    private List<Relationship> getRelationshipOfTheso(ArrayList<NodeUri> nodeUri, String idTheso){
        List<Relationship> relationships = new ArrayList<>();
        for (NodeUri nodeUri1 : nodeUri) {
            Relationship relationship = new Relationship();

            relationship.setStart(uriHelper.getUriForTheso(idTheso, "", ""));
            relationship.setEnd(uriHelper.getUriForConcept(nodeUri1.getIdConcept(), nodeUri1.getIdArk(), null));
            relationship.setRelation("skos__hasTopConcept");
            relationships.add(relationship);            
        }
        return relationships;
    }    
    

    
    private Node getDatasOfCollection(ConceptIdLabel conceptIdLabel, String idTheso){
        Node node = new Node();
        NodeGroupLabel nodeGroupLabel = groupService.getNodeGroupLabel(conceptIdLabel.getIdentifier(), idTheso);
 
        node.setId(conceptIdLabel.getUri());
        List<String> labels = new ArrayList<>();
        labels.add("Resource");
        labels.add("skos__Collection");
        node.setLabels(labels);
        
        Properties properties = new Properties();
        properties.setUri(conceptIdLabel.getUri());
        properties.setPropertiesLabel("skos__prefLabel");

        
        List<String> prefLabels = new ArrayList<>();
        
        for (NodeGroupTraductions nodeGroupTraductionse : nodeGroupLabel.getNodeGroupTraductionses()) {
            prefLabels.add(nodeGroupTraductionse.getTitle() + "@" + nodeGroupTraductionse.getIdLang());
        }
        properties.setPrefLabels(prefLabels);       
        
        node.setProperties(properties);
        return node;
    }
    
    /**
     * Datas for external links
     */
    private Node getDatasOfExternalLink(String id){
        Node node = new Node();
        
        node.setId(id);
        node.setLabels(getNodeLabel(null));
        node.setProperties(getPropertiesOfExternalLink(id));
        return node;
    }
    
    private Properties getPropertiesOfExternalLink(String id){
        Properties properties = new Properties();
        properties.setUri(id);
        return properties;
    } 
    
    private Node getDatasOfNode(NodeFullConcept nodeFullConcept){
        Node node = new Node();
        node.setId(nodeFullConcept.getUri());//idTheso + "." + nodeFullConcept.getIdentifier());
        node.setLabels(getNodeLabel(nodeFullConcept));
        node.setProperties(getPropertiesOfNode(nodeFullConcept));
        return node;
    }
    
    private Properties getPropertiesOfNode(NodeFullConcept nodeFullConcept){
        Properties properties = new Properties();
        properties.setUri(nodeFullConcept.getUri());
        properties.setPropertiesLabel("skos__prefLabel");

        List<String> prefLabels = new ArrayList<>();
    //    prefLabels.add(nodeFullConcept.getPrefLabel().getLabel() + "@" + nodeFullConcept.getPrefLabel().getIdLang());
        prefLabels.add((nodeFullConcept != null && nodeFullConcept.getPrefLabel() != null) ?
                (nodeFullConcept.getPrefLabel().getLabel() + "@" + nodeFullConcept.getPrefLabel().getIdLang()) :
                "");
        if(nodeFullConcept.getPrefLabelsTraduction() != null){
            for (ConceptLabel conceptLabel : nodeFullConcept.getPrefLabelsTraduction()) {
                //prefLabels.add(conceptLabel.getLabel() + "@" + conceptLabel.getIdLang());
                prefLabels.add(conceptLabel != null ? conceptLabel.getLabel() + "@" + conceptLabel.getIdLang() : "");
            }
        }
        properties.setPrefLabels(prefLabels);
        return properties;
    }  
    
    private List<String> getNodeLabel(NodeFullConcept nodeFullConcept){
        List<String> labels = new ArrayList<>();
        labels.add("Resource");
        if(nodeFullConcept != null){
            switch (nodeFullConcept.getResourceType()) {
                case SKOSProperty.CONCEPT:
                    labels.add("skos__Concept");
                    break;
                case SKOSProperty.COLLECTION:
                    labels.add("skos__Collection");
                    break;        
                case SKOSProperty.CONCEPT_SCHEME:
                    labels.add("skos__ConceptScheme");
                    break;           
                default:
                    break;
            }
        }
        return labels;
    }    
    
    
    
    
    
    private List<Relationship> getRelationship(NodeFullConcept nodeFullConcept, String idTheso, String idLang){
        List<Relationship> relationships = new ArrayList<>();
        if(nodeFullConcept.getNarrowers() != null){
            for (ConceptRelation narrower : nodeFullConcept.getNarrowers()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(narrower.getUri());
                relationship.setRelation("skos__narrower");
                relationships.add(relationship);
            }
        }
        if(nodeFullConcept.getBroaders() != null){
            for (ConceptRelation broader : nodeFullConcept.getBroaders()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(broader.getUri());
                relationship.setRelation("skos__broader");
                relationships.add(relationship);
            }
        }
        
        if(nodeFullConcept.getRelateds()!= null){
            for (ConceptRelation related : nodeFullConcept.getRelateds()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(related.getUri());
                relationship.setRelation("skos__related");
                relationships.add(relationship);
            }
        }
        if(nodeFullConcept.getReplacedBy() != null){
            for (ConceptIdLabel conceptIdLabel : nodeFullConcept.getReplacedBy()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(conceptIdLabel.getUri());
                relationship.setRelation("ns0__isReplacedBy");
                relationships.add(relationship);
            }
        }     
        if(nodeFullConcept.getReplaces()!= null){
            for (ConceptIdLabel conceptIdLabel : nodeFullConcept.getReplaces()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(conceptIdLabel.getUri());
                relationship.setRelation("ns0__replace");
                relationships.add(relationship);
            }
        }
        if(nodeFullConcept.getMembres()!= null){
            for (ConceptIdLabel conceptIdLabel : nodeFullConcept.getMembres()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(conceptIdLabel.getUri());
                relationship.setRelation("ns2__memberOf");
                relationships.add(relationship);
                Node node = getDatasOfCollection(conceptIdLabel, idTheso);
                nodeGraphD3js.addNewNode(node);
            }
        }           

        if(nodeFullConcept.getExactMatchs()!= null){
            for (ConceptIdLabel exactMatch : nodeFullConcept.getExactMatchs()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(exactMatch.getUri());
                relationship.setRelation("skos__exactMatch");
                relationships.add(relationship);
                nodeGraphD3js.addNewNode(getDatasOfExternalLink(exactMatch.getUri()));
            }
        }  
        
        Relationship relationship = new Relationship();

        relationship.setStart(nodeFullConcept.getUri());
        relationship.setEnd(uriHelper.getUriForTheso(idTheso, "", ""));
        relationship.setRelation("skos__inScheme");
        relationships.add(relationship);

        return relationships;
    }

    public String getJsonFromNodeGraphD3js(){
        if(nodeGraphD3js == null) return null;
        
        JsonObjectBuilder nodeRoot = Json.createObjectBuilder();
        
        JsonArrayBuilder jsonArrayNodes = Json.createArrayBuilder();
        
        
        for (Node node : nodeGraphD3js.getNodes()) {
            JsonObjectBuilder nodeDatas = Json.createObjectBuilder();
            
            // id
            nodeDatas.add("id", node.getId());
            
            // label of node
            JsonArrayBuilder nodeLables = Json.createArrayBuilder();
            for (String label : node.getLabels()) {
                nodeLables.add(label);
            }
            nodeDatas.add("labels", nodeLables.build());  
            
            // properties // prefLabel
            JsonObjectBuilder nodeProperties = Json.createObjectBuilder();
            JsonArrayBuilder jsonArrayPrefLabels = Json.createArrayBuilder();
            
            if(node.getProperties().getPrefLabels() != null){
                for (String prefLabel : node.getProperties().getPrefLabels()) {
                    jsonArrayPrefLabels.add(prefLabel);
                }
                nodeProperties.add(node.getProperties().getPropertiesLabel(), jsonArrayPrefLabels.build());
            }
            nodeProperties.add("uri", node.getProperties().getUri());
            
            nodeDatas.add("properties", nodeProperties.build());

            jsonArrayNodes.add(nodeDatas.build());
        }
    
        // add nodes
        nodeRoot.add("nodes", jsonArrayNodes.build());        
        
        
        // add relationships
        JsonArrayBuilder jsonArrayRelationships = Json.createArrayBuilder();
        
        for (Relationship relationship : nodeGraphD3js.getRelationships()) {
             JsonObjectBuilder nodeRelation = Json.createObjectBuilder();
             nodeRelation.add("start", relationship.getStart());
             nodeRelation.add("end", relationship.getEnd());
             nodeRelation.add("label", relationship.getRelation());
             jsonArrayRelationships.add(nodeRelation.build());
        }
        nodeRoot.add("relationships", jsonArrayRelationships.build());        
        return nodeRoot.build().toString();
    }             
    
}
