package searchengine.dto.customResponses.searchResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchResponse {
    private boolean result;
    private int count;
    private List<SearchData> data;
    private String error;
}
