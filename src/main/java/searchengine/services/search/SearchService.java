package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.customResponses.searchResponse.SearchData;
import searchengine.dto.customResponses.searchResponse.SearchResponse;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.crud.IndexCrudService;
import searchengine.services.crud.LemmaEntityCrudService;
import searchengine.services.crud.SiteEntityCrudService;
import searchengine.services.lemma.LemmaFinder;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final IndexCrudService indexCrudService;
    private final LemmaEntityCrudService lemmaEntityCrudService;
    private final SiteEntityCrudService siteEntityCrudService;
    private final LemmaFinder lemmaFinder;
    private static final int WINDOW_SIZE = 25; // Размер окна и размер snippet

    public SearchResponse calculatorRelevance(String query, int limit, int offset, String siteUrl) {

        SiteEntity siteEntity = siteEntityCrudService.getByUrl(siteUrl);
        if(siteUrl != null && !siteUrl.isEmpty() && siteEntity == null) {
            SearchResponse errorResponse  = new SearchResponse();
            errorResponse.setResult(false);
            errorResponse.setError("Сайт еще не проиндексирован");
            return errorResponse;
        }
        List<LemmaEntity> lemmasSortedList = getSortedListLemmas(query, siteEntity);

        if (query.isEmpty()) {
            SearchResponse errorResponse  = new SearchResponse();
            errorResponse.setResult(false);
            errorResponse.setError("Задан пустой поисковый запрос");
            return errorResponse;
        }

        if (lemmasSortedList == null || lemmasSortedList.isEmpty()) {
            SearchResponse errorResponse  = new SearchResponse();
            errorResponse.setResult(false);
            errorResponse.setError("Нет результатов по запросу");
            return errorResponse;
        }
        List<PageEntity> pageEntityList = getPageEntityListByExistsAllLemmas(lemmasSortedList, siteUrl);

        Map<PageEntity,Float> mapAbsRelevancePage = getMapAbsRelevancePage(lemmasSortedList, pageEntityList);

        float maxAbsRelevance = mapAbsRelevancePage.values().stream().max(Float::compareTo).orElse(0.0f);

        Map<PageEntity,Float> mapNormalizeRelevancePage = mapAbsRelevancePage
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / maxAbsRelevance));

        return getSearchResponse(mapNormalizeRelevancePage, lemmasSortedList, limit, offset);
    }

    private SearchResponse getSearchResponse(Map<PageEntity,Float> mapNormalizeRelevancePage,
                                             List<LemmaEntity> lemmaSortedList,
                                             int limit,
                                             int offset) {

        List<PageEntity> sortedListPageEntity = mapNormalizeRelevancePage.entrySet()
                .stream()
                .sorted(Map.Entry.<PageEntity, Float>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .skip(offset)
                .limit(limit)
                .toList();

        List<SearchData> searchDataList = new ArrayList<>();
        List<String> lemmaTextList = lemmaSortedList.stream()
                .filter(Objects::nonNull)
                .map(LemmaEntity::getLemma)
                .toList();

        for(PageEntity pageEntity: sortedListPageEntity) {
            SearchData searchData = new SearchData();
            searchData.setSite(pageEntity.getSiteEntity().getUrl());
            searchData.setSiteName(pageEntity.getSiteEntity().getName());
            searchData.setUri(pageEntity.getPath());
            String snippet = generateSnippet(pageEntity.getContent(),lemmaTextList);
            searchData.setSnippet(snippet);
            searchData.setRelevance(mapNormalizeRelevancePage.get(pageEntity));
            searchData.setTitle(pageEntity.getTitle());
            searchDataList.add(searchData);
        }
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setCount(mapNormalizeRelevancePage.size());
        searchResponse.setResult(true);
        searchResponse.setData(searchDataList);
        return searchResponse;
    }

    private Map<PageEntity, Float> getMapAbsRelevancePage(List<LemmaEntity> lemmasSortedList,
                                                          List<PageEntity> sortedByRankPageEntityList) {
        Map<PageEntity,Float> mapAbsRelevancePage = new HashMap<>();
        for(LemmaEntity lemmaEntity: lemmasSortedList) {
            for(PageEntity pageEntity: sortedByRankPageEntityList) {
                float absRelevancePage = 0;
                List<IndexEntity> indexEntities = pageEntity.getIndexEntityList();
                IndexEntity indexEntity = indexEntities
                        .stream()
                        .filter(i -> i.getLemmaEntity().equals(lemmaEntity))
                        .findFirst()
                        .orElse(null);
                if(indexEntity != null) {
                    absRelevancePage = absRelevancePage + indexEntity.getRank();
                }
                mapAbsRelevancePage.merge(pageEntity,absRelevancePage,Float::sum);
            }
        }
        return mapAbsRelevancePage;
    }

    private List<PageEntity> getPageEntityListByExistsAllLemmas(List<LemmaEntity> lemmasSortedList, String siteUrl) {
        List<IndexEntity> indexEntityList = new ArrayList<>();
        for (LemmaEntity lemmaEntity : lemmasSortedList) {
            List<IndexEntity> indexesList = indexCrudService.getAllByLemmaEntityAndSiteUrl(lemmaEntity, siteUrl);
            indexEntityList.addAll(indexesList);

        }
        CopyOnWriteArrayList<PageEntity> pageEntityList = indexEntityList.stream()
                .map(IndexEntity::getPageEntity)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        for(PageEntity pageEntity : pageEntityList) {
            List<LemmaEntity> lemmaEntityList = pageEntity.getIndexEntityList()
                    .stream()
                    .map(IndexEntity::getLemmaEntity)
                    .toList();
            for(int i = 1; i < lemmasSortedList.size(); i++) {
                if(!lemmaEntityList.contains(lemmaEntityList.get(i))) {
                    pageEntityList.remove(pageEntity);
                }
            }
        }
        return pageEntityList;
    }

    private List<LemmaEntity> getSortedListLemmas(String query, SiteEntity siteEntity) {
        Map<String, Integer> mapLemmas = lemmaFinder.collectLemmas(query);
        if (mapLemmas.isEmpty()) return null;

        Set<LemmaEntity> lemmaEntityHashSet = new HashSet<>();
        if(siteEntity == null) {
            for (Map.Entry<String, Integer> entry: mapLemmas.entrySet()) {
                List<LemmaEntity> lemmaEntityList = lemmaEntityCrudService.getLemmaEntityByLemma(entry.getKey());
                lemmaEntityHashSet.addAll(lemmaEntityList);
            }
        } else {
            for (Map.Entry<String, Integer> entry: mapLemmas.entrySet()) {
                LemmaEntity lemmaEntity = lemmaEntityCrudService.getLemmaEntityByLemmaAndSiteUrl(entry.getKey(),siteEntity);
                if(lemmaEntity == null) continue;
                lemmaEntityHashSet.add(lemmaEntity);
            }
        }
        if(lemmaEntityHashSet.isEmpty()) return null;
        return lemmaEntityHashSet.stream()
                .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                .toList();
    }

    public String generateSnippet(String content, List<String> lemmas) {
        String[] words = lemmaFinder.arrayContainsRussianWords(content);
        int maxMatches = 0;
        int maxUniqueLemmas = 0;
        String bestSnippet = "";
        for (int i = 0; i <= words.length - WINDOW_SIZE; i++) {
            int matches = 0;
            String[] window = Arrays.copyOfRange(words, i, i + WINDOW_SIZE);
            List<String> snippetWords = new ArrayList<>();
            Set<String> foundNormalForms = new HashSet<>();
            for(String word: window) {
                String lemmaWord = lemmaFinder.getNormalForm(word);
                if(lemmas.contains(lemmaWord)) {
                    snippetWords.add("<b>" + word + "</b>");
                    foundNormalForms.add(lemmaWord);
                    matches ++;
                } else {
                    snippetWords.add(word);
                }
            }
            if (foundNormalForms.size() > maxUniqueLemmas || foundNormalForms.size() == maxUniqueLemmas && matches > maxMatches) {
                maxMatches = matches;
                maxUniqueLemmas = foundNormalForms.size();
                bestSnippet = String.join(" ", "..." + snippetWords) + "...";
            }
        }
        return bestSnippet;
    }
}

