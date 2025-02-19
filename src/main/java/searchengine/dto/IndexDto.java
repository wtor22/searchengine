package searchengine.dto;

import lombok.Data;


@Data
public class IndexDto {
    private int id;
    private float rank;
    private PageDto pageDto;
    private LemmaDto lemmaDto;

    @Override
    public String toString() {
        return "IndexDto{" +
                "id=" + id +
                ", rank=" + rank +
                ", pageDto=  RFR"  +
                ", lemmaDto=" + lemmaDto +
                '}';
    }
}
