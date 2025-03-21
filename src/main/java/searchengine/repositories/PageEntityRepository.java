package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface PageEntityRepository extends JpaRepository<PageEntity, Integer> {

    @Query("SELECT p.path FROM PageEntity p WHERE p.path IN :paths AND p.siteEntity = :siteEntity")
    List<String> findByPathAndSiteEntity(List<String> paths, SiteEntity siteEntity);

    int countBySiteEntity(SiteEntity siteEntity);

    PageEntity findByPathAndSiteEntity(String path, SiteEntity siteEntity);
}
