package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dto.IndexDto;
import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.dto.customResponses.CustomConnectResponse;
import searchengine.services.lemma.LemmaFinder;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class HtmlDataProcessor {

    public PageDto pageBuilder(PageDto pageDto) {

        String url = pageDto.getPath();
        CustomConnectResponse response = fetchHtml(url);
        pageDto.setCode(response.getStatusCode());
        if(String.valueOf(response.getStatusCode()).startsWith("4") ||
                String.valueOf(response.getStatusCode()).startsWith("5") ||
                response.getStatusCode() == 0) {
            pageDto.setContent(String.valueOf(response.getStatusMessage()));
            return pageDto;
        } else {
            pageDto.setContent(response.getDoc().body().html());
            pageDto.setListLinks(listStringParser(response));
        }
        // Если ссылка это домен
        if(pageDto.getSiteDto().getUrl().equals(pageDto.getPath())) {
            pageDto.setPath("/");
        }
        if(pageDto.getPath().startsWith(pageDto.getSiteDto().getUrl()))
            pageDto.setPath(pageDto.getPath().replace(pageDto.getSiteDto().getUrl(),""));
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
                        //TODO Вынести в config
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .execute();
                Document doc = response.parse();
                return new CustomConnectResponse(response.statusCode(), response.statusMessage(),doc);
            } catch (HttpStatusException se) {
                return new CustomConnectResponse(se.getStatusCode(),se.getMessage(),null);
            } catch (UnsupportedMimeTypeException me) {
                System.err.println("Ссылка: " + path + "Неподдерживаемый MIME-тип: " + me.getMimeType());
                return new CustomConnectResponse(0, "Неподдерживаемый MIME-тип:", null);
            } catch (SocketTimeoutException ste) {
                attempts ++;
                if(attempts == replays) {
                    System.err.println("Exception SocketTimeoutException for link : " + path + " message: " + ste.getMessage() + " попыток: " + attempts + " из: " + 3);
                    return new CustomConnectResponse(0, "Таймаут соединения", null);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            } catch (IOException ioe) {
                attempts ++;
                if(attempts == replays) {
                    System.out.println("Exception: " + ioe.getMessage() + " попыток " + attempts  + " из " + 3);
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
        LemmaFinder lemmaFinder = LemmaFinder.getInstance();
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
}
