package com.server.tourApiProject.touristPoint.nearTouristData;

import com.server.tourApiProject.touristPoint.contentType.ContentType;
import com.server.tourApiProject.touristPoint.contentType.ContentTypeRepository;
import com.server.tourApiProject.touristPoint.touristData.TouristData;
import com.server.tourApiProject.touristPoint.touristData.TouristDataRepository;
import com.server.tourApiProject.touristPoint.touristDataHashTag.TouristDataHashTag;
import com.server.tourApiProject.touristPoint.touristDataHashTag.TouristDataHashTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NearTouristDataServiceTest {

    @Mock
    private NearTouristDataRepository nearTouristDataRepository;

    @Mock
    private TouristDataRepository touristDataRepository;

    @Mock
    private TouristDataHashTagRepository touristDataHashTagRepository;

    @Mock
    private ContentTypeRepository contentTypeRepository;

    @InjectMocks
    private NearTouristDataService nearTouristDataService;

    @Test
    void createNearTouristData_returnsWithoutSavingWhenTargetDataIsMissing() {
        when(touristDataRepository.findById(2L)).thenReturn(Optional.empty());

        nearTouristDataService.createNearTouristData(1L, 2L);

        verify(nearTouristDataRepository, never()).save(org.mockito.ArgumentMatchers.any(NearTouristData.class));
    }

    @Test
    void createNearTouristData_mapsOptionalFieldsAndCategoryName() {
        TouristData owner = touristData(1L, "기준 관광지", "서울");
        TouristData near = touristData(2L, "근처 관광지", "부산");
        near.setFirstImage("img.png");
        near.setOverviewSim("요약");
        near.setCat3("CAT1");
        when(touristDataRepository.findById(2L)).thenReturn(Optional.of(near));
        when(touristDataRepository.findByContentId(1L)).thenReturn(owner);
        when(touristDataRepository.findByContentId(2L)).thenReturn(near);
        when(contentTypeRepository.findByCat3Code("CAT1")).thenReturn(contentType("전망대"));

        nearTouristDataService.createNearTouristData(1L, 2L);

        ArgumentCaptor<NearTouristData> captor = ArgumentCaptor.forClass(NearTouristData.class);
        verify(nearTouristDataRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getTouristDataId());
        assertEquals(2L, captor.getValue().getContentId());
        assertEquals("img.png", captor.getValue().getFirstImage());
        assertEquals("전망대", captor.getValue().getCat3Name());
    }

    @Test
    void getNearTouristData_mapsItemsAndLimitsHashTagsToThree() {
        NearTouristData near = NearTouristData.builder()
                .touristDataId(1L)
                .contentId(2L)
                .firstImage("img.png")
                .title("근처 관광지")
                .addr("서울 강남구")
                .cat3Name("전망대")
                .overviewSim("요약")
                .build();
        when(nearTouristDataRepository.findByTouristDataId(1L)).thenReturn(List.of(near));
        when(touristDataHashTagRepository.findByContentId(2L)).thenReturn(List.of(
                TouristDataHashTag.builder().hashTagName("야경").build(),
                TouristDataHashTag.builder().hashTagName("가족").build(),
                TouristDataHashTag.builder().hashTagName("데이트").build(),
                TouristDataHashTag.builder().hashTagName("추가").build()
        ));

        List<NearTouristDataParams> result = nearTouristDataService.getNearTouristData(1L);

        assertEquals(1, result.size());
        assertEquals(List.of("야경", "가족", "데이트"), result.get(0).getHashTagNames());
    }

    @Test
    void deleteNearTouristPoint_deletesAllTouristContentType12EntriesById() {
        when(touristDataRepository.findByContentTypeId(12L)).thenReturn(List.of(
                touristData(10L, "a", "서울"),
                touristData(11L, "b", "부산")
        ));

        nearTouristDataService.deleteNearTouristPoint();

        verify(touristDataRepository).deleteById(10L);
        verify(touristDataRepository).deleteById(11L);
    }

    private TouristData touristData(Long contentId, String title, String addr) {
        TouristData touristData = new TouristData();
        touristData.setContentId(contentId);
        touristData.setTitle(title);
        touristData.setAddr(addr);
        return touristData;
    }

    private ContentType contentType(String cat3Name) {
        ContentType contentType = new ContentType();
        contentType.setCat3Name(cat3Name);
        return contentType;
    }
}
