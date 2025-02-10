package searchengine.services.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.crudServices.PageEntityCrudService;
import searchengine.services.crudServices.SiteEntityCrudService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebCrawlerService {

    private final SitesList sites;
    private final SiteEntityCrudService siteEntityCrudService;
    private final PageEntityCrudService pageEntityCrudService;


    public boolean updateSiteIndex() {
        if ( siteEntityCrudService.existsByStatus(Status.INDEXING)) {
            return false;
        }
        LinkCollector.setIsStarted();
        List<Site> siteList = sites.getSites();
        List<String> urls = siteList.stream().map(Site::getUrl).map(this::urlDeleteLastSlash).toList();
        siteEntityCrudService.deleteAllByListUrls(urls);
        for(Site site: siteList) {
            SiteEntity siteEntity = addSite(site);
            LinkCollector linkCollector = new LinkCollector(site.getUrl(),site.getUrl(),
                    pageEntityCrudService, siteEntityCrudService, siteEntity);
            linkCollector.fork();
        }
        return true;
    }

    private SiteEntity addSite(Site site) {
        String url = urlDeleteLastSlash(site.getUrl());
        url = urlDeleteLastSlash(url);
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(url);
        siteEntity.setStatus(Status.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityCrudService.create(siteEntity);
        return siteEntity;
    }

    private String urlDeleteLastSlash(String url) {
        return url.endsWith("/") ?
                url.substring(0, url.length() - 1): url;
    }
}
