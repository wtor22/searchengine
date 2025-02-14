package searchengine.services.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.IndexDto;
import searchengine.model.IndexEntity;
import searchengine.repositories.IndexRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexCrudService {

    private final IndexRepository indexRepository;

    public void createAll(List<IndexEntity> indexEntityList) {
        indexRepository.saveAll(indexEntityList);
    }

    public static IndexEntity mapToEntity(IndexDto indexDto) {

        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setRank(indexDto.getRank());

        return indexEntity;

    }
}
