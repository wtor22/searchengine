package searchengine.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.test.web.servlet.MockMvc;
import searchengine.dto.customResponses.searchResponse.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;
import searchengine.services.crawler.RecursiveCrawler;
import searchengine.services.crawler.SinglePageCrawler;
import searchengine.services.crawler.StarterRecursiveCrawler;
import searchengine.services.search.SearchService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(ApiController.class)
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private StarterRecursiveCrawler starterRecursiveCrawler;
    @MockBean
    private StatisticsService statisticsService;
    @MockBean
    private SinglePageCrawler singlePageCrawler;
    @MockBean
    private SearchService searchService;
    @MockBean
    private RecursiveCrawler recursiveCrawler;


    @Test
    @DisplayName("Test response ok when start indexing")
    void shouldReturnOkWhenIndexingStarted() throws Exception {
        Mockito.when(starterRecursiveCrawler.startSiteIndex()).thenReturn(true);

        mockMvc.perform(get("/api/startIndexing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @DisplayName("Test response bad request when indexing already started")
    void shouldReturnBadRequestWhenIndexingAlreadyStarted() throws Exception {
        Mockito.when(starterRecursiveCrawler.startSiteIndex()).thenReturn(false);

        mockMvc.perform(get("/api/startIndexing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.error").value("Индексация уже запущена"));
    }

    @Test
    @DisplayName("Test response bad request when indexing already stopped")
    void shouldReturnBadRequestWhenIndexingStopped() throws Exception {
        Mockito.when(starterRecursiveCrawler.stopSiteIndexing()).thenReturn(false);

        mockMvc.perform(get("/api/stopIndexing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.error").value("Индексация не запущена"));
    }
    @Test
    @DisplayName("Test response ok when indexing stopped")
    void shouldReturnOkWhenIndexingStopped() throws Exception {
        Mockito.when(starterRecursiveCrawler.stopSiteIndexing()).thenReturn(true);

        mockMvc.perform(get("/api/stopIndexing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));

    }

    @Test
    @DisplayName("Test response ok when indexed single page")
    void shouldReturnOkWhenIndexedSinglePage() throws Exception {
        Mockito.when(singlePageCrawler.startPageIndex(Mockito.anyString())).thenReturn(true);

        mockMvc.perform(post("/api/indexPage")
                        .param("url","http//..."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @DisplayName("Test response bad request when don't indexed single page")
    void shouldReturnBadRequestWhenIndexedSinglePage() throws Exception {
        Mockito.when(singlePageCrawler.startPageIndex(Mockito.anyString())).thenReturn(false);

        mockMvc.perform(post("/api/indexPage")
                        .param("url","http//..."))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.error").value("Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле"));
    }

    @Test
    @DisplayName("Test response not found when search query did not return any results")
    void shouldReturnNotFoundWhenNotResultBySearch() throws Exception {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(false);

        Mockito.when(searchService.calculatorRelevance(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any()))
                .thenReturn(searchResponse);

        mockMvc.perform(get("/api/search")
                        .param("query","nonexistent")
                        .param("offset","0")
                        .param("limit", "20")
                        .param("site", "example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    @DisplayName("Test response ok when search query return results")
    void shouldReturnOkWhenExistsResultBySearch() throws Exception {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(true);

        Mockito.when(searchService.calculatorRelevance(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any()))
                .thenReturn(searchResponse);

        mockMvc.perform(get("/api/search")
                        .param("query","nonexistent")
                        .param("offset","0")
                        .param("limit", "20")
                        .param("site", "example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @DisplayName("Test response OK when return successfully statistics")
    void shouldReturnStatisticsSuccessfully() throws Exception {
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        statisticsResponse.setResult(true);

        Mockito.when(statisticsService.getStatistics()).thenReturn(statisticsResponse);

        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @DisplayName("Test response http status 500 when error database")
    void shouldReturn500WhenDatabaseError() throws Exception {
        Mockito.when(statisticsService.getStatistics())
                .thenThrow(new DataAccessException("Ошибка базы данных") {});

        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.error").value("Ошибка доступа к базе данных"));
    }

    @Test
    @DisplayName("Test response http status 500 when error unknown")
    void shouldReturn500WhenUnknownError() throws Exception {
        Mockito.when(statisticsService.getStatistics())
                .thenThrow(new RuntimeException("Какая-то стрёмная ошибка"));

        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isInternalServerError())  // Ожидаем 500
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.error").value("Непредвиденная ошибка"));
    }
}
