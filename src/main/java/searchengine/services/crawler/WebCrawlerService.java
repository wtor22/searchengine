package searchengine.services.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConnect;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.model.Status;
import searchengine.services.crud.IndexCrudService;
import searchengine.services.crud.LemmaEntityCrudService;
import searchengine.services.crud.PageEntityCrudService;
import searchengine.services.crud.SiteEntityCrudService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebCrawlerService {

    private final SitesList sites;
    private final SiteEntityCrudService siteEntityCrudService;
    private final PageEntityCrudService pageEntityCrudService;
    private final LemmaEntityCrudService lemmaEntityCrudService;
    private final IndexCrudService indexCrudService;
    private final JsoupConnect jsoupConnect;



    public boolean updateSiteIndex() {
        if ( siteEntityCrudService.existsByStatus(Status.INDEXING)) {
            return false;
        }
        LinkCollector.setIsStarted();
        List<Site> siteList = sites.getSites();
        List<SiteDto> siteDtoList = siteList.stream()
                .map(site -> new SiteDto( urlDeleteLastSlash(site.getUrl()),site.getName()))
                .toList();
        List<String> urls = siteList.stream().map(Site::getUrl).toList();
        siteEntityCrudService.deleteAllByListUrls(urls);
        for(SiteDto siteDto: siteDtoList) {
            siteDto.setStatus(Status.INDEXING);
            siteDto.setStatusTime(LocalDateTime.now());
            SiteDto createdSiteDto = siteEntityCrudService.create(siteDto);

            PageDto pageDto = new PageDto();
            pageDto.setSiteDto(createdSiteDto);
            pageDto.setPath(siteDto.getUrl());
            LinkCollector linkCollector = new LinkCollector(siteEntityCrudService,pageEntityCrudService,lemmaEntityCrudService,
                    indexCrudService,pageDto, jsoupConnect);
            linkCollector.fork();
        }
        return true;
    }


    private String urlDeleteLastSlash(String url) {
        return url.endsWith("/") ?
                url.substring(0, url.length() - 1): url;
    }
}
