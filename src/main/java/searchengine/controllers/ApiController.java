package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.customResponses.CrawlerResponse;
import searchengine.dto.customResponses.searchResponse.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;
import searchengine.services.crawler.RecursiveCrawler;
import searchengine.services.crawler.SinglePageCrawler;
import searchengine.services.crawler.StarterRecursiveCrawler;
import searchengine.services.search.SearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final StarterRecursiveCrawler starterRecursiveCrawler;
    private final SinglePageCrawler singlePageCrawler;
    private final SearchService searchRelevanceCalculator;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<CrawlerResponse> startIndexing() {
        if (starterRecursiveCrawler.startSiteIndex()) {
            return ResponseEntity.ok().body(new CrawlerResponse(true));
        }
        return ResponseEntity.badRequest().body(new CrawlerResponse(false, "Индексация уже запущена"));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<CrawlerResponse> stopIndexing() {
        if(RecursiveCrawler.setIsStopped()) return ResponseEntity.ok().body(new CrawlerResponse(true));
        return ResponseEntity.badRequest().body(new CrawlerResponse(false, "Индексация не запущена"));
    }

    @PostMapping("/indexPage")
    public ResponseEntity<CrawlerResponse> indexPage(@RequestParam String url) {
        if(singlePageCrawler.startPageIndex(url)) {
            return ResponseEntity.ok().body(new CrawlerResponse(true));
        }
        return ResponseEntity.badRequest().body(new CrawlerResponse(false, "Данная страница находится за пределами сайтов, \n" +
                "указанных в конфигурационном файле\n"));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> indexAge(@RequestParam String query,
                                                   @RequestParam int offset,
                                                   @RequestParam(defaultValue = "20") int limit,
                                                   @RequestParam(required = false) String site) {

        return ResponseEntity.ok().body(searchRelevanceCalculator.calculatorRelevance(query, limit, offset, site));
    }
}
