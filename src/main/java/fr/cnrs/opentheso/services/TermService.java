package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.repositories.TermRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;



@Data
@Service
@AllArgsConstructor
public class TermService {

    private final TermRepository termRepository;

}
