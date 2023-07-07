package fr.cnrs.opentheso.bean.notification.dto;

import lombok.Data;


@Data
public class ReleaseDto {

    private String html_url;
    private String tag_name;
    private String published_at;
    private String body;

}
