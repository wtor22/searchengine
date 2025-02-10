package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.util.List;

public interface SiteEntityRepository extends JpaRepository<SiteEntity, Integer> {

    SiteEntity findByUrl(String url);

    List<SiteEntity> findByUrlIn(List<String> urls);
    void deleteByUrlIn(List<String> urls);

    boolean existsByStatus(Status status);
}
