package searchengine.services.crudServices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteEntityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteEntityCrudService {

    private final SiteEntityRepository siteEntityRepository;
    private final PageEntityCrudService pageEntityCrudService;

    public void create(SiteEntity siteEntity) {
        siteEntityRepository.save(siteEntity);
    }
    public boolean existsByStatus(Status status) {
        return siteEntityRepository.existsByStatus(status);
    }

    public void update(SiteEntity siteEntity) {
        SiteEntity oldSiteEntity = siteEntityRepository.findByUrl(siteEntity.getUrl());
        siteEntity.setId(oldSiteEntity.getId());
        siteEntityRepository.save(siteEntity);
    }

    @Transactional
    public void deleteAllByListUrls(List<String> listUrls) {
        List<SiteEntity> siteEntities = siteEntityRepository.findByUrlIn(listUrls);
        pageEntityCrudService.deleteAllByListSiteEntity(siteEntities);
        siteEntityRepository.deleteByUrlIn(listUrls);
    }
}
