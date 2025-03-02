package searchengine.dto.customResponses.searchResponse;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private boolean result;
    private int count;
    private List<SearchData> data;
}
