package searchengine.services.crudServices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageEntityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PageEntityCrudService {

    private final PageEntityRepository pageEntityRepository;

    public void create(PageEntity pageEntity) {
        pageEntityRepository.save(pageEntity);
    }

    public List<String> getListExistingPath(List<String> paths, SiteEntity siteEntity) {
        return pageEntityRepository.findByPathAndSiteEntity(paths, siteEntity);
    }

    public void deleteAllByListSiteEntity(List<SiteEntity> siteEntities) {
        pageEntityRepository.deleteAllBySiteEntityIn(siteEntities);
    }
}
