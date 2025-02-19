package searchengine.services.crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.IndexDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.model.*;
import searchengine.services.crud.IndexCrudService;
import searchengine.services.crud.LemmaEntityCrudService;
import searchengine.services.crud.PageEntityCrudService;
import searchengine.services.crud.SiteEntityCrudService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataPageStorage {

    private final SiteEntityCrudService siteEntityCrudService;
    private final PageEntityCrudService pageEntityCrudService;
    private final LemmaEntityCrudService lemmaEntityCrudService;


    @Transactional
    void storageData(List<IndexDto> indexDtoList, PageDto pageDto) {
        SiteEntity siteEntity = siteEntityCrudService.getById(pageDto.getSiteDto().getId()).orElseThrow();
        PageEntity pageEntity = PageEntityCrudService.mapToEntity(pageDto);
        pageEntity.setSiteEntity(siteEntity);

        for(IndexDto indexDto: indexDtoList) {
            IndexEntity indexEntity = IndexCrudService.mapToEntity(indexDto);
            LemmaEntity lemmaEntity = lemmaEntityCrudService.getLemmaEntityBySiteEntityAndLemma(siteEntity, indexDto.getLemmaDto().getLemma());
            if(lemmaEntity == null) {
                LemmaEntity newLemmaEntity = LemmaEntityCrudService.mapToEntity(indexDto.getLemmaDto());
                newLemmaEntity.setSiteEntity(siteEntity);
                LemmaEntity createdLemmaEntity = lemmaEntityCrudService.create(newLemmaEntity);
                indexEntity.setLemmaEntity(createdLemmaEntity);
            } else {
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmaEntityCrudService.create(lemmaEntity);
                indexEntity.setLemmaEntity(lemmaEntity);
            }
            indexEntity.setPageEntity(pageEntity);
            pageEntity.getIndexEntityList().add(indexEntity);
            pageEntity.getIndexEntityList().add(indexEntity);
        }
        pageEntityCrudService.create(pageEntity);
    }

    void setSiteStatusIndexed(PageDto pageDto) {
        SiteEntity siteEntity = siteEntityCrudService.getById(pageDto.getSiteDto().getId()).orElseThrow();
        siteEntity.setStatus(Status.INDEXED);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityCrudService.update(siteEntity);
    }
    public void stopIndex(PageDto pageDto) {
        SiteEntity siteEntity = SiteEntityCrudService.mapTOEntity(pageDto.getSiteDto());
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError("Индексация остановлена пользователем");
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityCrudService.update(siteEntity);
    }

    List<String> checkExistsLink(List<String> listLinks, PageDto pageDto) {
        SiteEntity siteEntity = SiteEntityCrudService.mapTOEntity(pageDto.getSiteDto());
        return pageEntityCrudService.getListExistingPath(listLinks, siteEntity);
    }

    boolean isDomainExists(String url) {
        return siteEntityCrudService.existsByUrl(url);
    }
    void isPageExistsDelete(String url, SiteDto siteDto) {
        SiteEntity siteEntity = siteEntityCrudService.getByUrl(siteDto.getUrl());
        pageEntityCrudService.isPageExistsDelete(url, siteEntity);
    }

}
