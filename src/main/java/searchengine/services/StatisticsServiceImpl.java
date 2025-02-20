package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.SiteDto;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.services.crawler.RecursiveCrawler;
import searchengine.services.crud.SiteEntityCrudService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteEntityCrudService siteEntityCrudService;
    private final SitesList sites;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(RecursiveCrawler.isStopped);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<SiteDto> listSiteDto = siteEntityCrudService.getAllDto();
        for (SiteDto siteDto: listSiteDto) {
            Optional<SiteEntity> optionalSiteEntity = siteEntityCrudService.getById(siteDto.getId());
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(siteDto.getName());
            item.setStatus(siteDto.getStatus().toString());
            item.setUrl(siteDto.getUrl());
            item.setError(siteDto.getLastError() != null ? siteDto.getLastError() : "");
            LocalDateTime lastStatusTime = siteDto.getStatusTime();
            ZonedDateTime zone = lastStatusTime.atZone(ZoneOffset.UTC);
            long time = zone.toInstant().toEpochMilli();
            item.setStatusTime(time);
            int pages = 0, lemmas = 0;
            if(optionalSiteEntity.isPresent()) {
                pages = optionalSiteEntity.get().getPageEntityList().size();
                lemmas = optionalSiteEntity.get().getLemmaEntityList().size();
            }
            item.setPages(pages);
            item.setLemmas(lemmas);
            detailed.add(item);
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
