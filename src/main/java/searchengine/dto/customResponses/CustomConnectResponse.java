package searchengine.dto.customResponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.nodes.Document;

@Data
@AllArgsConstructor
public class CustomConnectResponse {
    private int statusCode;
    private String statusMessage;
    private Document doc;


}

