package searchengine.services.crawler;

import org.springframework.transaction.annotation.Transactional;
import searchengine.config.JsoupConnect;
import searchengine.dto.IndexDto;
import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.model.*;
import searchengine.services.HtmlDataProcessor;
import searchengine.services.crud.IndexCrudService;
import searchengine.services.crud.LemmaEntityCrudService;
import searchengine.services.crud.PageEntityCrudService;
import searchengine.services.crud.SiteEntityCrudService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;


public class LinkCollector extends RecursiveAction {

    private final SiteEntityCrudService siteEntityCrudService;
    private final PageEntityCrudService pageEntityCrudService;
    private final LemmaEntityCrudService lemmaEntityCrudService;
    private final IndexCrudService indexCrudService;
    private final PageDto pageDto;
    private final JsoupConnect jsoupConnect;
    private static volatile boolean isStopped = false;


    public LinkCollector(SiteEntityCrudService siteEntityCrudService, PageEntityCrudService pageEntityCrudService, LemmaEntityCrudService lemmaEntityCrudService, IndexCrudService indexCrudService, PageDto pageDto, JsoupConnect jsoupConnect) {
        this.siteEntityCrudService = siteEntityCrudService;
        this.pageEntityCrudService = pageEntityCrudService;
        this.lemmaEntityCrudService = lemmaEntityCrudService;
        this.indexCrudService = indexCrudService;
        this.pageDto = pageDto;
        this.jsoupConnect = jsoupConnect;
    }

    @Override
    public void compute() {
        HtmlDataProcessor htmlDataProcessor = new HtmlDataProcessor(jsoupConnect);
        PageDto processedPageDto = htmlDataProcessor.pageBuilder(pageDto);

        List<IndexDto> indexDtoList = htmlDataProcessor.listIndexesDtoBuilder(processedPageDto);
        createAndSavePageData(indexDtoList,processedPageDto);

        List<String> listLinksOnPage = processedPageDto.getListLinks();
        CopyOnWriteArrayList<String> checkedList = new CopyOnWriteArrayList<>(listLinksOnPage);
        SiteDto siteDto = processedPageDto.getSiteDto();
        for(String link : checkedList) {
            if(isStopped) {
                stopIndex(pageDto);
                return;
            }
            checkedList.removeAll(checkExistsLink(checkedList,processedPageDto));
            if(checkedList.isEmpty() || !checkedList.contains(link)) continue;
            link = !link.startsWith(siteDto.getUrl()) ? siteDto.getUrl().concat(link) : link;
            PageDto pageDtoToRecursive = new PageDto();
            pageDtoToRecursive.setPath(link);
            pageDtoToRecursive.setSiteDto(siteDto);
            LinkCollector linkCollector = new LinkCollector(siteEntityCrudService,pageEntityCrudService,
                    lemmaEntityCrudService, indexCrudService, pageDtoToRecursive, jsoupConnect);
            linkCollector.invoke();
        }
        if(processedPageDto.getPath().equals("/")) {
            setSiteStatusIndexed(processedPageDto);
        }
    }
    private List<String> checkExistsLink(List<String> listLinks, PageDto pageDto) {
        SiteEntity siteEntity = SiteEntityCrudService.mapTOEntity(pageDto.getSiteDto());
        return pageEntityCrudService.getListExistingPath(listLinks, siteEntity);
    }
    private void createAndSavePageData(List<IndexDto> indexDtoList, PageDto pageDto) {
        PageEntity pageEntity = PageEntityCrudService.mapToEntity(pageDto);
        SiteEntity siteEntity = SiteEntityCrudService.mapTOEntity(pageDto.getSiteDto());
        pageEntity.setSiteEntity(siteEntity);

        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        List<IndexEntity> indexEntityList = new ArrayList<>();


        for (IndexDto indexDto: indexDtoList) {
            LemmaDto lemmaDto = indexDto.getLemmaDto();
            LemmaEntity lemmaEntity = lemmaEntityCrudService.getLemmaEntityBySiteEntityAndLemma(siteEntity, lemmaDto.getLemma());
            if(lemmaEntity == null) {
                lemmaEntityList.add(new LemmaEntity(lemmaDto.getLemma(),lemmaDto.getFrequency(),siteEntity));
            } else {
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmaEntityList.add(lemmaEntity);
            }
            IndexEntity indexEntity = IndexCrudService.mapToEntity(indexDto);
            indexEntity.setLemmaEntity(lemmaEntity);
            indexEntity.setPageEntity(pageEntity);
            indexEntityList.add(indexEntity);
        }
        saveData(pageEntity,indexEntityList,lemmaEntityList);
    }
    @Transactional
    private void saveData(PageEntity pageEntity, List<IndexEntity> indexEntityList, List<LemmaEntity> lemmaEntityList) {

        SiteEntity siteEntity = pageEntity.getSiteEntity();
        siteEntity.setStatus(Status.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityCrudService.update(siteEntity);
        pageEntityCrudService.create(pageEntity);
        lemmaEntityCrudService.createOrUpdateAll(lemmaEntityList);
        indexCrudService.createAll(indexEntityList);

    }
    private void setSiteStatusIndexed(PageDto pageDto) {
        SiteEntity siteEntity = SiteEntityCrudService.mapTOEntity(pageDto.getSiteDto());
        siteEntity.setStatus(Status.INDEXED);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityCrudService.update(siteEntity);
    }
    public void stopIndex(PageDto pageDto) {
        SiteEntity siteEntity = SiteEntityCrudService.mapTOEntity(pageDto.getSiteDto());
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError("Индексация остановлена пользователем");
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityCrudService.update(siteEntity);
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
