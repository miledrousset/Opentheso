package fr.cnrs.opentheso.services.graphs;

import fr.cnrs.opentheso.entites.GraphView;
import fr.cnrs.opentheso.entites.GraphViewExportedConceptBranch;
import fr.cnrs.opentheso.models.graphs.GraphObject;
import fr.cnrs.opentheso.repositories.GraphViewExportedConceptBranchRepository;
import fr.cnrs.opentheso.repositories.GraphViewRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;



@Slf4j
@Service
@AllArgsConstructor
public class GraphService implements Serializable {

    private final GraphViewRepository graphViewRepository;
    private final GraphViewExportedConceptBranchRepository graphViewExportedConceptBranchRepository;


    public Map<Integer, GraphObject> getViews(int idUser) {

        log.info("Rechercher les informations complètes pour toutes les vues de l'utilisateur {}", idUser);
        var graphViewsList = graphViewRepository.findAllByIdUser(idUser);
        if (graphViewsList.isEmpty()) {
            log.error("Aucun graphView n'est trouvé pour l'id user {}", idUser);
            return new HashMap<>();
        }

        Map<Integer, GraphObject> graphViews = new HashMap<>();
        for (GraphView graphView : graphViewsList) {
            graphViews.put(graphView.getId(),
                    new GraphObject(graphView.getId(), graphView.getName(), graphView.getDescription()));

            var graphObject = graphViews.get(graphView.getId());

            var result = graphViewExportedConceptBranchRepository.findAllByGraphViewId(graphView.getId());
            for (GraphViewExportedConceptBranch element : result) {
                if (element.getTopConceptThesaurusId() != null) {
                    graphObject.getExportedData().add(new ImmutablePair<>(element.getTopConceptThesaurusId(), element.getTopConceptId()));
                }
            }
        }
        return graphViews;
    }

    public GraphObject getView(String id) {
        List<Object[]> rows = graphViewRepository.findViewWithExportedConcepts(Integer.parseInt(id));

        GraphObject view = null;

        for (Object[] row : rows) {
            if (view == null) {
                Integer viewId = (Integer) row[0];
                String name = (String) row[1];
                String description = (String) row[2];
                view = new GraphObject(viewId, name, description);
            }
            String thesaurusId = (String) row[3];
            String conceptId = (String) row[4];
            if (thesaurusId != null) {
                view.getExportedData().add(new ImmutablePair<>(thesaurusId, conceptId));
            }
        }

        return view;
    }

    public void deleteView(String idView) {

        log.info("Suppression de la view id {}", idView);
        graphViewRepository.deleteById(Integer.parseInt(idView));
        graphViewExportedConceptBranchRepository.deleteAllByGraphViewId(Integer.parseInt(idView));
    }

    public void saveView(GraphObject view) {

        var graphView = graphViewRepository.getById(view.getId());
        graphView.setName(view.getName());
        graphView.setDescription(view.getDescription());
        graphViewRepository.save(graphView);
    }

    public int createView(GraphObject view, int idUser) {

        log.info("Création d'une view par l'utilisateur {}", idUser);
        var graphView = graphViewRepository.save(GraphView.builder()
                .name(view.getName())
                .description(view.getDescription())
                .idUser(idUser)
                .build());
        return graphView.getId();
    }

    public void addDataToView(int idView, ImmutablePair<String, String> tuple) {

        log.info("Ajout des données à la view id {}", idView);
        var concept = tuple.right == null ? null : tuple.right;
        graphViewExportedConceptBranchRepository.save(GraphViewExportedConceptBranch.builder()
                        .graphViewId(idView)
                        .topConceptId(concept)
                        .topConceptThesaurusId(tuple.left)
                .build());
    }

    public boolean isExistDatas(int idView, String idThesaurus, String idConcept) {
        log.info("Vérification si la combinaison {} de vue existe", idView);
        if (StringUtils.isEmpty(idConcept)) {
            var graphView = graphViewExportedConceptBranchRepository
                    .findByGraphViewIdAndTopConceptIdNullAndTopConceptThesaurusId(idView, idThesaurus);
            return graphView.isPresent();
        } else {
            var graphView = graphViewExportedConceptBranchRepository
                    .findByGraphViewIdAndTopConceptIdAndTopConceptThesaurusId(idView, idConcept, idThesaurus);
            return graphView.isPresent();
        }
    }

    public void removeDataFromView(int selectedViewId, ImmutablePair<String, String> tuple) {
        log.info("Suppression des données depuis la vie {}", selectedViewId);
        if (tuple.right == null) {
            graphViewExportedConceptBranchRepository.deleteAllByGraphViewIdAndTopConceptThesaurusId(selectedViewId, tuple.left);
        } else {
            graphViewExportedConceptBranchRepository.deleteAllByGraphViewIdAndTopConceptIdAndTopConceptThesaurusId(selectedViewId, tuple.right, tuple.left);
        }
    }
}
