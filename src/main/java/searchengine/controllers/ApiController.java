package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.customResponses.CrawlerResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;
import searchengine.services.crawler.LinkCollector;
import searchengine.services.crawler.WebCrawlerService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final WebCrawlerService webCrawlerService;

    public ApiController(StatisticsService statisticsService, WebCrawlerService webCrawlerService) {
        this.statisticsService = statisticsService;
        this.webCrawlerService = webCrawlerService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<CrawlerResponse> startIndexing() {
        if (webCrawlerService.updateSiteIndex()) {
            return ResponseEntity.ok().body(new CrawlerResponse(true));
        }
        return ResponseEntity.badRequest().body(new CrawlerResponse(false, "Индексация уже запущена"));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<CrawlerResponse> stopIndexing() {
        if(LinkCollector.setIsStopped()) return ResponseEntity.ok().body(new CrawlerResponse(true));
        return ResponseEntity.badRequest().body(new CrawlerResponse(false, "Индексация не запущена"));
    }
}
