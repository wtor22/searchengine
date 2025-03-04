package searchengine.services.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.IndexDto;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexCrudService {

    private final IndexRepository indexRepository;
    private final SiteEntityCrudService siteEntityCrudService;

    public void createAll(List<IndexEntity> indexEntityList) {
        indexRepository.saveAll(indexEntityList);
    }
    public void create(IndexEntity indexEntity) {
        indexRepository.save(indexEntity);
    }

    public List<IndexEntity> getAllByLemmaEntity(LemmaEntity lemmaEntity) {
        return indexRepository.findAllByLemmaEntity(lemmaEntity);
    }
    public List<IndexEntity> getAllByLemmaEntityAndSiteUrl(LemmaEntity lemmaEntity, String siteUrl) {
        if(siteUrl == null || siteUrl.isEmpty()) return indexRepository.findAllByLemmaEntity(lemmaEntity);
        SiteEntity siteEntity = siteEntityCrudService.getByUrl(siteUrl);
        return indexRepository.findAllByLemmaEntityAndSiteEntity(lemmaEntity, siteEntity);
    }

    public static IndexEntity mapToEntity(IndexDto indexDto) {

        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setRank(indexDto.getRank());

        return indexEntity;

    }
}
