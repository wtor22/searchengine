package searchengine.services.crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.IndexDto;
import searchengine.dto.PageDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SinglePageCrawler {
    private final HtmlDataProcessor htmlDataProcessor;
    private final DataPageStorage dataPageStorage;

    public boolean startPageIndex(String url) {
        String domain = getDomain(url);
        if(!url.trim().matches("https?://.*") || !dataPageStorage.isDomainExists(domain)) return false;
        PageDto processedPageDto = htmlDataProcessor.pageBuilder(url);
        List<IndexDto> indexDtoList = htmlDataProcessor.listIndexesDtoBuilder(processedPageDto);
        dataPageStorage.storageData(indexDtoList,processedPageDto);
        return true;
    }

    private String getDomain(String url) {
        String[] array = url.split("/");
        return array[0].concat("//").concat(array[2]);
    }
}
