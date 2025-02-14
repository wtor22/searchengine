package searchengine.dto;

import lombok.Data;
import searchengine.model.Status;

import java.time.LocalDateTime;

@Data
public class SiteDto {

    private int id;
    private Status status;
    private LocalDateTime statusTime;
    private String lastError;
    private String url;
    private String name;

    public SiteDto() {}
    public SiteDto(String url,String name) {
        this.url = url;
        this.name = name;
    }
}
