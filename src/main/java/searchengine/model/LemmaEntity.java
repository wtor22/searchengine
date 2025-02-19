package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "lemma")
public class LemmaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private SiteEntity siteEntity;

    @Column(columnDefinition = "VARCHAR(255)")
    private String lemma;

    private int frequency;

    public LemmaEntity() {
    }
    public LemmaEntity(String lemma, int frequency, SiteEntity siteEntity) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteEntity = siteEntity;
    }
}
