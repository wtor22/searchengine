package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.customResponses.CrawlerResponse;
import searchengine.dto.customResponses.searchResponse.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;
import searchengine.services.crawler.SinglePageCrawler;
import searchengine.services.crawler.StarterRecursiveCrawler;
import searchengine.services.search.SearchService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final StarterRecursiveCrawler starterRecursiveCrawler;
    private final SinglePageCrawler singlePageCrawler;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<?> statistics() {
        try {
            StatisticsResponse response = statisticsService.getStatistics();
            return ResponseEntity.ok(response);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", false,"error", "Ошибка доступа к базе данных"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", false,"error", "Непредвиденная ошибка"));
        }
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
        if(starterRecursiveCrawler.stopSiteIndexing()) return ResponseEntity.ok().body(new CrawlerResponse(true));
        return ResponseEntity.badRequest().body(new CrawlerResponse(false, "Индексация не запущена"));
    }

    @PostMapping("/indexPage")
    public ResponseEntity<CrawlerResponse> indexPage(@RequestParam String url) {
        if(singlePageCrawler.startPageIndex(url)) {
            return ResponseEntity.ok().body(new CrawlerResponse(true));
        }
        return ResponseEntity.badRequest().body(new CrawlerResponse(false, "Данная страница находится за пределами сайтов, " +
                "указанных в конфигурационном файле"));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> indexAge(@RequestParam String query,
                                                   @RequestParam int offset,
                                                   @RequestParam(defaultValue = "20") int limit,
                                                   @RequestParam(required = false) String site) {

        SearchResponse searchResponse = searchService.calculatorRelevance(query, limit, offset, site);
        if(!searchResponse.isResult()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(searchResponse);
        }
        return ResponseEntity.ok().body(searchService.calculatorRelevance(query, limit, offset, site));
    }

}
