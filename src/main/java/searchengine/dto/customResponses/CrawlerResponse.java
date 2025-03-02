package searchengine.dto.customResponses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CrawlerResponse {
    private boolean result;
    private String error;

    public CrawlerResponse(boolean result) {
        this.result = result;
    }
    public CrawlerResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
