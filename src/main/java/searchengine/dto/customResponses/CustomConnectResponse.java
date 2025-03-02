package searchengine.dto.customResponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;

@Setter
@Getter
@AllArgsConstructor
public class CustomConnectResponse {
    private int statusCode;
    private String statusMessage;
    private Document doc;
}

