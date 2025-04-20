package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.PropositionModificationDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PropositionModificationDetailRepository extends JpaRepository<PropositionModificationDetail, Integer> {

    List<PropositionModificationDetail> findAllByIdProposition(Integer propositionId);

}