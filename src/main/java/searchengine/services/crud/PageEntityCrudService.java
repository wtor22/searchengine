package searchengine.services.crud;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.PageDto;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageEntityRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class PageEntityCrudService {

    private PageEntityRepository pageEntityRepository;

    public void create(PageEntity pageEntity) {
        pageEntityRepository.save(pageEntity);
    }

    public List<String> getListExistingPath(List<String> paths, SiteEntity siteEntity) {
        return pageEntityRepository.findByPathAndSiteEntity(paths, siteEntity);
    }

    public void deleteAllByListSiteEntity(List<SiteEntity> siteEntities) {
        pageEntityRepository.deleteAllBySiteEntityIn(siteEntities);
    }

    public static PageEntity mapToEntity(PageDto pageDto) {

        PageEntity pageEntity = new PageEntity();
        pageEntity.setPath(pageDto.getPath());
        pageEntity.setCode(pageDto.getCode());
        pageEntity.setContent(pageDto.getContent());

        return pageEntity;
    }
}
