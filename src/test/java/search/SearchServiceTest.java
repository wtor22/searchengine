package search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import searchengine.dto.customResponses.searchResponse.SearchResponse;
import searchengine.model.SiteEntity;
import searchengine.services.crud.IndexCrudService;
import searchengine.services.crud.LemmaEntityCrudService;
import searchengine.services.crud.SiteEntityCrudService;
import searchengine.services.lemma.LemmaFinder;
import searchengine.services.search.SearchService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    private final String testSiteUrl = "https://testsiteurl.com";

    private SearchService searchService;


    @Mock
    private IndexCrudService indexCrudService;
    @Mock
    private LemmaEntityCrudService lemmaEntityCrudService;
    @Mock
    private SiteEntityCrudService siteEntityCrudService;
    @Mock
    private LemmaFinder lemmaFinder;

    @BeforeEach
    public void setUp() {
        searchService = new SearchService(indexCrudService, lemmaEntityCrudService, siteEntityCrudService, lemmaFinder);
    }

    @Test
    @DisplayName("Test error when site is not indexed")
    void shouldReturnErrorWhenSiteIsNotIndexed() {
        when(siteEntityCrudService.getByUrl(testSiteUrl)).thenReturn(null);
        SearchResponse response = searchService.calculatorRelevance("query", 10, 0, testSiteUrl);
        assertFalse(response.isResult());
        assertEquals("Сайт еще не проиндексирован", response.getError());
    }

    @Test
    @DisplayName("Test error when query is empty")
    void shouldReturnErrorWhenQueryIsEmpty() {
        when(siteEntityCrudService.getByUrl(testSiteUrl)).thenReturn(new SiteEntity());
        SearchResponse response = searchService.calculatorRelevance("  ", 10, 0, testSiteUrl);
        assertFalse(response.isResult());
        assertEquals("Задан пустой поисковый запрос", response.getError());
    }

    @Test
    @DisplayName("Test error when list lemmas is empty")
    void shouldReturnErrorWhenNotFoundLemmas() {
        when(siteEntityCrudService.getByUrl(testSiteUrl)).thenReturn(new SiteEntity());
        SearchResponse response = searchService.calculatorRelevance("query", 10, 0, testSiteUrl);
        assertFalse(response.isResult());
        assertEquals("Нет результатов по запросу", response.getError());
    }
}
