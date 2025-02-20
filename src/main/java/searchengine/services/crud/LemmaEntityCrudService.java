package searchengine.services.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.LemmaDto;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaEntityRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LemmaEntityCrudService {

    private final LemmaEntityRepository lemmaEntityRepository;

    public LemmaEntity create(LemmaEntity lemma) {
        return lemmaEntityRepository.save(lemma);
    }
    public void deleteAll(List<LemmaEntity> lemmaEntityList) {
        lemmaEntityRepository.deleteAll(lemmaEntityList);
    }
    public void updateAll(List<LemmaEntity> lemmaEntityList) {
        lemmaEntityRepository.saveAll(lemmaEntityList);
    }
    public LemmaEntity getLemmaEntityBySiteEntityAndLemma(SiteEntity siteEntity, String lemma) {
        Optional<LemmaEntity> optionalLemmaEntity = lemmaEntityRepository.findBySiteEntityAndLemma(siteEntity,lemma);
        return optionalLemmaEntity.orElse(null);
    }

    public static LemmaEntity mapToEntity(LemmaDto lemmaDto) {
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setId(lemmaDto.getId());
        lemmaEntity.setLemma(lemmaDto.getLemma());
        lemmaEntity.setFrequency(lemmaDto.getFrequency());
        return lemmaEntity;
    }
}
