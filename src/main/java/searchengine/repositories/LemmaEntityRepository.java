package searchengine.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;

@Repository
public interface LemmaEntityRepository extends JpaRepository<LemmaEntity, Integer> {

    @Override
    Optional<LemmaEntity> findById(Integer integer);

    Optional<LemmaEntity> findBySiteEntityAndLemma(SiteEntity siteEntity, String lemma);
    Optional<LemmaEntity> findByLemma(String lemma);
}
