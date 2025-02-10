package searchengine.services.crawler;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dto.customResponses.CustomConnectResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.crudServices.PageEntityCrudService;
import searchengine.services.crudServices.SiteEntityCrudService;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;


@Getter
public class LinkCollector extends RecursiveAction {

    private static volatile boolean isStopped = false;

    private final String domain;
    private String url;
    private final PageEntityCrudService pageEntityCrudService;
    private final SiteEntityCrudService siteEntityCrudService;
    private final SiteEntity siteEntity;


    LinkCollector(String url, String domain, PageEntityCrudService pageEntityCrudService,
                  SiteEntityCrudService siteEntityCrudService, SiteEntity siteEntity) {
        this.url = url;
        this.domain = domain;
        this.pageEntityCrudService = pageEntityCrudService;
        this.siteEntity = siteEntity;
        this.siteEntityCrudService = siteEntityCrudService;
    }

    @Override
    public void compute() {
        String page = domain;
        if(url.startsWith(domain) && !url.equals(domain)) url = url.replace(domain,"");
        if(!url.startsWith(domain)) page = page.concat(url);
        CustomConnectResponse response = htmlGetResponse(page);
        PageEntity pageEntity = new PageEntity();
        pageEntity.setPath(url.equals(domain) ?  "/" : url );
        pageEntity.setCode(response.getStatusCode());
        pageEntity.setSiteEntity(siteEntity);

        //TODO Реализовать все в методе получения response
        if(String.valueOf(response.getStatusCode()).startsWith("4") ||
                String.valueOf(response.getStatusCode()).startsWith("5")) {
            pageEntity.setContent(String.valueOf(response.getStatusCode()));
            pageEntityCrudService.create(pageEntity);
            return;
        } else if (response.getStatusCode() == 0){
            pageEntity.setContent(String.valueOf(response.getStatusMessage()));
            pageEntityCrudService.create(pageEntity);
            return;
        } else {
            pageEntity.setContent(response.getDoc().body().html());
        }

        pageEntityCrudService.create(pageEntity);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityCrudService.update(siteEntity);
        Elements linesParse = response.getDoc().select("body [href]:not(._disabled)");
        List<String> listLinksOnPage = linesParse.stream()
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

        if (listLinksOnPage.isEmpty()) return;

        List<String> existingInDataBaseLinks = pageEntityCrudService.getListExistingPath(listLinksOnPage, siteEntity);
        listLinksOnPage.removeAll(existingInDataBaseLinks);

        List<String> listLinksOnPageO = new CopyOnWriteArrayList<>(listLinksOnPage);
        for (String link : listLinksOnPageO) {

            if(isStopped) {
                siteEntity.setStatus(Status.FAILED);
                siteEntity.setLastError("Индексация остановлена пользователем");
                siteEntity.setStatusTime(LocalDateTime.now());
                siteEntityCrudService.update(siteEntity);
                return;
            }
            List<String> existingInDataBaseLinksO = pageEntityCrudService.getListExistingPath(listLinksOnPageO, siteEntity);
            listLinksOnPageO.removeAll(existingInDataBaseLinksO);
            if(listLinksOnPageO.isEmpty())  break;

            if(!listLinksOnPageO.contains(link)) continue;

            LinkCollector linkCollector = new LinkCollector(link,domain,pageEntityCrudService,
                    siteEntityCrudService,siteEntity);
            linkCollector.invoke();
        }
        if (pageEntity.getPath().equals("/")) {
            siteEntity.setStatus(Status.INDEXED);
            siteEntityCrudService.update(siteEntity);
        }
    }

    private  CustomConnectResponse htmlGetResponse(String link) {
        int attempts = 0;
        int reties = 3;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (attempts < reties) {
            try {
                Connection.Response response = Jsoup.connect(link)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .execute();
                Document doc = response.parse();
                return new CustomConnectResponse(response.statusCode(), response.statusMessage(),doc);
            } catch (HttpStatusException se) {
                return new CustomConnectResponse(se.getStatusCode(),se.getMessage(),null);
            } catch (UnsupportedMimeTypeException me) {
                System.err.println("Ссылка: " + link + "Неподдерживаемый MIME-тип: " + me.getMimeType());
                return new CustomConnectResponse(0, "Неподдерживаемый MIME-тип:", null);
            } catch (SocketTimeoutException ste) {
                attempts ++;
                if(attempts == reties) {
                    System.err.println("Exception SocketTimeoutException for link : " + link + " message: " + ste.getMessage() + " попыток: " + attempts + " из: " + 3);
                    return new CustomConnectResponse(0, "Таймаут соединения", null);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            } catch (IOException ioe) {
                attempts ++;
                if(attempts == reties) {
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
    public static boolean setIsStopped() {
        if(isStopped) return false;
        isStopped = true;
        return true;
    }
    public static void setIsStarted() {
        isStopped = false;
    }
}
