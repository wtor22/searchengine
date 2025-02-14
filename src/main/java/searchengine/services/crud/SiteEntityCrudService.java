package searchengine.services.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.SiteDto;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteEntityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteEntityCrudService {

    private final SiteEntityRepository siteEntityRepository;
    private final PageEntityCrudService pageEntityCrudService;


    public SiteDto create(SiteDto siteDto) {
        SiteEntity existSiteEntity = siteEntityRepository.findByUrl(siteDto.getUrl());
        if(existSiteEntity == null) {
            SiteEntity siteEntity = siteEntityRepository.save(mapTOEntity(siteDto));
            siteDto.setId(siteEntity.getId());
            return siteDto;
        }
        return null;
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

    public static SiteEntity mapTOEntity(SiteDto siteDto) {

        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setId(siteDto.getId());
        siteEntity.setName(siteDto.getName());
        siteEntity.setUrl(siteDto.getUrl());
        siteEntity.setLastError(siteDto.getLastError());
        siteEntity.setStatus(siteDto.getStatus());
        siteEntity.setStatusTime(siteDto.getStatusTime());
        return siteEntity;
    }


}
