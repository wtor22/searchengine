package searchengine.services.crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.IndexDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SinglePageCrawler {
    private final SitesList sites;
    private final HtmlDataProcessor htmlDataProcessor;
    private final DataPageStorage dataPageStorage;

    public boolean startPageIndex(String url) {
        String domain = getDomain(url);
        if(!domain.trim().matches("https?://.*")) return false;
        List<Site> siteListApp = sites.getSites();
        Optional<Site> optionalExistsSiteApp = siteListApp.stream().filter(site -> site.getUrl().equals(domain)).findFirst();
        if(optionalExistsSiteApp.isEmpty()) return false;
        Site site = optionalExistsSiteApp.get();
        SiteDto siteDto;
        if(!dataPageStorage.isDomainExists(domain)) {
            siteDto = new SiteDto();
            siteDto.setName(site.getName());
            siteDto.setUrl(site.getUrl());
            siteDto.setStatus(Status.INDEXING);
            siteDto.setStatusTime(LocalDateTime.now());
            dataPageStorage.createSite(siteDto);
        } else {
            siteDto = dataPageStorage.getSiteDtoByUrl(site.getUrl());
        }
        PageDto pageDto = htmlDataProcessor.pageBuilder(url);

        List<IndexDto> indexDtoList = htmlDataProcessor.listIndexesDtoBuilder(pageDto);
        dataPageStorage.storageData(indexDtoList,pageDto);
        dataPageStorage.setStatusIndexed(siteDto);
        return true;
    }

    private String getDomain(String url) {
        String[] array = url.split("/");
        return array[0].concat("//").concat(array[2]);
    }
}
