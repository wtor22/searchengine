package searchengine.dto;

import lombok.Data;

@Data
public class LemmaDto {
    private int id;
    private String lemma;
    private int frequency;
    private SiteDto siteDto;

    public LemmaDto(String lemma, int frequency) {
        this.lemma = lemma;
        this.frequency = frequency;
    }
}
