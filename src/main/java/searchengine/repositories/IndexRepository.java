package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {
    public List<IndexEntity> findAllByLemmaEntity(LemmaEntity lemmaEntity);

    @Query("SELECT i From IndexEntity i JOIN i.pageEntity p WHERE p.siteEntity = :siteEntity AND i.lemmaEntity = :lemmaEntity")
    public List<IndexEntity> findAllByLemmaEntityAndSiteEntity(LemmaEntity lemmaEntity, SiteEntity siteEntity);
}
