package searchengine.services.crawler;

import lombok.RequiredArgsConstructor;
import searchengine.dto.IndexDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class RecursiveCrawler extends RecursiveAction {

    private final String url;
    private final HtmlDataProcessor htmlDataProcessor;
    private final DataPageStorage dataPageStorage;
    public static volatile boolean isStopped = false;

    @Override
    public void compute() {
        System.out.println("START COMPUTE THREAD " + Thread.currentThread().getId());
        PageDto pageDto = htmlDataProcessor.pageBuilder(url);
        if (pageDto.getPath().equals("/") && !pageDto.getCode().toString().startsWith("2")) {
            dataPageStorage.setSiteErrorHomePage(pageDto);
            return;
        }
        if (!pageDto.getCode().equals(200)) {
            System.out.println(LocalDateTime.now() + " PAGE DTO " +  pageDto.getSiteDto().getName() + " link " +
                    pageDto.getPath() + " code " + pageDto.getCode() + " THREAD -" + Thread.currentThread().getId());
        }
        List<IndexDto> indexDtoList = htmlDataProcessor.listIndexesDtoBuilder(pageDto);
        if (!pageDto.getCode().equals(200)) {
            System.out.println("INDEX DTO LIST SIZE  " + indexDtoList.size());
        }
        dataPageStorage.storageData(indexDtoList,pageDto);
        if(!pageDto.getCode().equals(200)) return;

        List<String> listLinksOnPage = pageDto.getListLinks();
        CopyOnWriteArrayList<String> checkedList = new CopyOnWriteArrayList<>(listLinksOnPage);
        SiteDto siteDto = pageDto.getSiteDto();
        for(String link : checkedList) {
            if(isStopped) {
                dataPageStorage.stopIndex(pageDto);
                return;
            }
            checkedList.removeAll(dataPageStorage.checkExistsLink(checkedList,pageDto));
            if(checkedList.isEmpty() || !checkedList.contains(link)) continue;
            link = !link.startsWith(siteDto.getUrl()) ? siteDto.getUrl().concat(link) : link;
            RecursiveCrawler linkCollector = new RecursiveCrawler(
                     link, htmlDataProcessor, dataPageStorage);
            linkCollector.invoke();
        }
        if(pageDto.getPath().equals("/")) {
            dataPageStorage.setStatusIndexed(pageDto.getSiteDto());
        }
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
