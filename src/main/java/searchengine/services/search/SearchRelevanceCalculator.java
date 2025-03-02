package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.springframework.stereotype.Service;
import searchengine.dto.customResponses.searchResponse.SearchData;
import searchengine.dto.customResponses.searchResponse.SearchResponse;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.services.crud.IndexCrudService;
import searchengine.services.crud.LemmaEntityCrudService;
import searchengine.services.lemma.LemmaFinder;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchRelevanceCalculator {
    private final IndexCrudService indexCrudService;
    private final LemmaEntityCrudService lemmaEntityCrudService;
    private final LemmaFinder lemmaFinder;
    private static final int WINDOW_SIZE = 25; // Размер окна

    public SearchResponse calculatorRelevance(String query) {
        List<LemmaEntity> lemmasSortedList = getSortedListLemmas(query);

        List<PageEntity> pageEntityList = getPageEntityListByExistsAllLemmas(lemmasSortedList);

        Map<PageEntity,Float> mapAbsRelevancePage = getMapAbsRelevancePage(lemmasSortedList, pageEntityList);

        float maxAbsRelevance = mapAbsRelevancePage.values().stream().max(Float::compareTo).orElse(0.0f);

        Map<PageEntity,Float> mapNormalizeRelevancePage = mapAbsRelevancePage
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / maxAbsRelevance));
        List<PageEntity> sortedListPageEntity = mapNormalizeRelevancePage.entrySet()
                .stream()
                .sorted(Map.Entry.<PageEntity, Float>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
//        List<String> lemmaTextList = lemmasSortedList.stream().map(LemmaEntity::getLemma).toList();
//        return generateSnippet(sortedListPageEntity.get(0).getContent(),lemmaTextList);
        return getSearchResponse(sortedListPageEntity, lemmasSortedList);
    }

    private SearchResponse getSearchResponse(List<PageEntity> pageEntityList, List<LemmaEntity> lemmaEntityList) {
        System.out.println("START METHOD GET RESPONSE");
        List<SearchData> searchDataList = new ArrayList<>();
        List<String> lemmaTextList = lemmaEntityList.stream().map(LemmaEntity::getLemma).toList();

        for(PageEntity pageEntity: pageEntityList) {
            SearchData searchData = new SearchData();
            searchData.setSite(pageEntity.getSiteEntity().getUrl());
            searchData.setSiteName(pageEntity.getSiteEntity().getName());
            searchData.setUri(pageEntity.getPath());
            String snippet = generateSnippet(pageEntity.getContent(),lemmaTextList);
            searchData.setSnippet(snippet);
            searchData.setRelevance(0.9f);

            searchDataList.add(searchData);
        }

        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setCount(pageEntityList.size());
        searchResponse.setResult(true);
        searchResponse.setData(searchDataList);
        return searchResponse;
    }

    private Map<PageEntity, Float> getMapAbsRelevancePage(List<LemmaEntity> lemmasSortedList, List<PageEntity> sortedByRankPageEntityList) {
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

    private List<PageEntity> getPageEntityListByExistsAllLemmas(List<LemmaEntity> lemmasSortedList) {
        List<IndexEntity> indexEntityList = indexCrudService.getAllByLemmaEntity(lemmasSortedList.get(0));
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
                    continue;
                }
            }
        }
        return pageEntityList;
    }

    private List<LemmaEntity> getSortedListLemmas(String query) {
        Map<String, Integer> mapLemmas = lemmaFinder.collectLemmas(query);
        return mapLemmas
                .keySet()
                .stream()
                .map(lemmaEntityCrudService::getLemmaEntityByLemma)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                .toList();
    }

    public String generateSnippet(String content, List<String> lemmas) {
        String[] words = lemmaFinder.arrayContainsRussianWords(content);
        int maxMatches = 0;
        int maxUniqueLemmas = 0;
        String bestSnippet = "Фрагмент текста не найден";
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
                bestSnippet = String.join(" ", snippetWords) + "...";
            }
        }
        return bestSnippet;
    }
}

