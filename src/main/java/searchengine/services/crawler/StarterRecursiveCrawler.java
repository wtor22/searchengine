package searchengine.services.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.SiteDto;
import searchengine.model.Status;
import searchengine.services.crud.SiteEntityCrudService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StarterRecursiveCrawler {

    private final SitesList sites;
    private final SiteEntityCrudService siteEntityCrudService;
    private final DataPageStorage dataPageStorage;
    private final HtmlDataProcessor htmlDataProcessor;

    public boolean startSiteIndex() {

        if ( siteEntityCrudService.existsByStatus(Status.INDEXING))return false;
        List<Site> siteList = sites.getSites();
        List<SiteDto> siteDtoList = siteList.stream()
                .map(site -> new SiteDto(urlDeleteLastSlash(site.getUrl()),site.getName()))
                .toList();
        List<String> urls = siteDtoList.stream().map(SiteDto::getUrl).toList();
        siteEntityCrudService.deleteAllByListUrls(urls);
        RecursiveCrawler.setIsStarted();
        for(SiteDto siteDto: siteDtoList) {
            siteDto.setStatus(Status.INDEXING);
            siteDto.setStatusTime(LocalDateTime.now());
            siteEntityCrudService.create(siteDto);
            RecursiveCrawler linkCollector = new RecursiveCrawler(
                    siteDto.getUrl(), htmlDataProcessor, dataPageStorage);
            linkCollector.fork();
        }
        return true;
    }

    private String urlDeleteLastSlash(String url) {
        return url.endsWith("/") ?
                url.substring(0, url.length() - 1): url;
    }
}
