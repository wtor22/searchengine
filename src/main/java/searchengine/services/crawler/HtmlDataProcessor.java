package searchengine.services.crawler;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import searchengine.dto.IndexDto;
import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.dto.customResponses.CustomConnectResponse;
import searchengine.services.crud.SiteEntityCrudService;
import searchengine.services.lemma.LemmaFinder;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HtmlDataProcessor {
    private final SiteEntityCrudService siteEntityCrudService;
    private final DataPageStorage dataPageStorage;
    private final LemmaFinder lemmaFinder;

    @Value("${jsoup-connect.userAgent}")
    String userAgent;
    @Value("${jsoup-connect.referrer}")
    String referrer;

    public PageDto pageBuilder(String url) {
        SiteDto siteDto = getSiteDto(url);
        CustomConnectResponse response = fetchHtml(url);
        PageDto pageDto = new PageDto();
        pageDto.setSiteDto(siteDto);
        if(siteDto.getUrl().equals(url)) {
            pageDto.setPath("/");
        } else {
            pageDto.setPath(url.replace(siteDto.getUrl(),""));
        }
        pageDto.setCode(response.getStatusCode());
        if(String.valueOf(response.getStatusCode()).startsWith("4") ||
                String.valueOf(response.getStatusCode()).startsWith("5") ||
                response.getStatusCode() == 0) {
            pageDto.setContent(String.valueOf(response.getStatusMessage()));
            return pageDto;
        } else {
            pageDto.setContent(response.getDoc().html());
            pageDto.setTitle(response.getDoc().title());
            pageDto.setListLinks(listStringParser(response));
        }
        dataPageStorage.isPageExistsDelete(pageDto.getPath(), siteDto);
        return pageDto;
    }

    private List<String> listStringParser(CustomConnectResponse response) {
        Elements linesParse = response.getDoc().select("body [href]:not(._disabled)");
        return linesParse.stream()
                .map(e -> e.attr("href"))
                .filter(link -> link.startsWith("/") && !link.equals("/") &&
                        !link.toLowerCase().contains(".jpg") && !link.toLowerCase().contains(".jpeg") &&
                        !link.toLowerCase().contains(".png") && !link.toLowerCase().contains(".eps") &&
                        !link.toLowerCase().contains(".svg") && !link.toLowerCase().contains(".pdf") &&
                        !link.toLowerCase().contains(".xlsx") && !link.toLowerCase().contains(".xls") &&
                        !link.toLowerCase().contains(".doc") && !link.toLowerCase().contains(".nc") &&
                        !link.toLowerCase().contains(".zip") && !link.toLowerCase().contains(".dat")
                )
                .map(link -> link.endsWith("/") ?
                        link.substring(0, link.length() - 1) : link)
                .collect(Collectors.toList());
    }

    private  CustomConnectResponse fetchHtml(String path) {
        int attempts = 0;
        int replays = 3;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (attempts < replays) {
            try {
                Connection.Response response = Jsoup.connect(path)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .timeout(10000)
                        .execute();
                Document doc = response.parse();
                return new CustomConnectResponse(response.statusCode(), response.statusMessage(),doc);
            } catch (HttpStatusException se) {
                return new CustomConnectResponse(se.getStatusCode(),se.getMessage(),null);
            } catch (UnsupportedMimeTypeException me) {
                return new CustomConnectResponse(0, "Неподдерживаемый MIME-тип:", null);
            } catch (SocketTimeoutException ste) {
                attempts ++;
                if(attempts == replays) {
                    return new CustomConnectResponse(0, "Таймаут соединения", null);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            } catch (IOException ioe) {
                attempts ++;
                if(attempts == replays) {
                    return new CustomConnectResponse(0,"Не удалось получить ответ сервера", null);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
        return new CustomConnectResponse(0,"Неизвестная ошибка", null);
    }

    public List<IndexDto> listIndexesDtoBuilder(PageDto pageDto) {
        Map<String, Integer> mapLemmas = lemmaFinder.collectLemmas(pageDto.getContent());
        SiteDto siteDto = pageDto.getSiteDto();
        List<IndexDto> indexDtoList = new ArrayList<>();
        for(Map.Entry<String,Integer> entry : mapLemmas.entrySet()) {
            LemmaDto lemmaDto = new LemmaDto(entry.getKey(), 1);
            lemmaDto.setSiteDto(siteDto);
            IndexDto indexDto = new IndexDto();
            indexDto.setPageDto(pageDto);
            indexDto.setLemmaDto(lemmaDto);
            indexDto.setRank(entry.getValue());
            indexDtoList.add(indexDto);
        }
        return indexDtoList;
    }

    SiteDto getSiteDto(String url) {
        if (!url.trim().matches("https?://.*")) return null;
        String[] array = url.trim().split("/");
        String domain = array[0].concat("//").concat(array[2]);
        return siteEntityCrudService.getDtoByUrl(domain);
    }
}
