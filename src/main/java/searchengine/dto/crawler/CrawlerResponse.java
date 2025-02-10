package searchengine.dto.crawler;

import lombok.Data;

@Data
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
