package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "`index`")
public class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "`rank`")
    private float rank;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private PageEntity pageEntity;

    @OneToOne
    @JoinColumn(name = "lemma_id")
    private LemmaEntity lemmaEntity;

}
