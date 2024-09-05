package fr.cnrs.opentheso.models.releases;

import lombok.Data;


@Data
public class TagDto {

    private String name;
    private String zipball_url;
    private String tarball_url;
    private String node_id;

}
