package searchengine.services.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.SiteDto;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteEntityRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SiteEntityCrudService {

    private final SiteEntityRepository siteEntityRepository;
    private final PageEntityCrudService pageEntityCrudService;


    public void create(SiteDto siteDto) {
        SiteEntity existSiteEntity = siteEntityRepository.findByUrl(siteDto.getUrl());
        if(existSiteEntity == null) {
            SiteEntity siteEntity = siteEntityRepository.save(mapTOEntity(siteDto));
            siteDto.setId(siteEntity.getId());
        }
    }
    public List<SiteDto> getAllDto(){
        List<SiteEntity> siteEntityList = siteEntityRepository.findAll();
        return siteEntityList.stream().map(SiteEntityCrudService::mapToDto).toList();
    }
    public SiteDto getDtoByUrl(String url) {
        SiteEntity siteEntity = siteEntityRepository.findByUrl(url);
        if(siteEntity == null) return null;
        return mapToDto(siteEntity);
    }
    public SiteEntity getByUrl(String url) {
        return siteEntityRepository.findByUrl(url);
    }
    public Optional<SiteEntity> getById(int id) {
        return siteEntityRepository.findById(id);
    }

    public boolean existsByUrl(String url) {
        return siteEntityRepository.existsByUrl(url);
    }
    public boolean existsByStatus(Status status) {
        return siteEntityRepository.existsByStatus(status);
    }

    public void update(SiteEntity siteEntity) {
        SiteEntity oldSiteEntity = siteEntityRepository.findByUrl(siteEntity.getUrl());
        siteEntity.setId(oldSiteEntity.getId());
        siteEntityRepository.save(siteEntity);
    }
    public int getCountPages(SiteDto siteDto) {
        return pageEntityCrudService.getCountPages(mapTOEntity(siteDto));
    }

    @Transactional
    public void deleteAllByListUrls(List<String> listUrls) {
        //List<SiteEntity> siteEntities = siteEntityRepository.findByUrlIn(listUrls);
        //pageEntityCrudService.deleteAllByListSiteEntity(siteEntities);
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

    public static SiteDto mapToDto(SiteEntity siteEntity) {
        SiteDto siteDto = new SiteDto();
        siteDto.setId(siteEntity.getId());
        siteDto.setName(siteEntity.getName());
        siteDto.setUrl(siteEntity.getUrl());
        siteDto.setLastError(siteEntity.getLastError());
        siteDto.setStatus(siteEntity.getStatus());
        siteDto.setStatusTime(siteEntity.getStatusTime());
        return siteDto;
    }
}
