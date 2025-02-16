package searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "jsoup-connect")
public class JsoupConnect {
    String userAgent;
    String referrer;
}
