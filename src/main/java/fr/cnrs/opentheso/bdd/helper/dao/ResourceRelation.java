package fr.cnrs.opentheso.bdd.helper.dao;

import lombok.Data;
/**
 *
 * @author miledrousset
 */

@Data
public class ResourceRelation {
    private String identifier;
    private String uri;
    private int property;
}
