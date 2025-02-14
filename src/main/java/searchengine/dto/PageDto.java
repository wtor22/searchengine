package searchengine.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDto {

    private int id;
    private SiteDto siteDto;
    private String path;
    private Integer code;
    private String content;
    private List<String> listLinks;
}
