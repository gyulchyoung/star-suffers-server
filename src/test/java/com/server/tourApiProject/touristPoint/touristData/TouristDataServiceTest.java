package com.server.tourApiProject.touristPoint.touristData;

import com.server.tourApiProject.search.Filter;
import com.server.tourApiProject.search.SearchParams1;
import com.server.tourApiProject.touristPoint.contentType.ContentType;
import com.server.tourApiProject.touristPoint.contentType.ContentTypeRepository;
import com.server.tourApiProject.touristPoint.touristDataHashTag.TouristDataHashTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TouristDataServiceTest {

    @Mock
    private TouristDataRepository touristDataRepository;

    @Mock
    private ContentTypeRepository contentTypeRepository;

    @InjectMocks
    private TouristDataService touristDataService;

    @Test
    void getContentType_returnsContentTypeIdOfTouristData() {
        TouristData touristData = touristData(1L, 12L, "관광지", "서울 강남구");
        when(touristDataRepository.findByContentId(1L)).thenReturn(touristData);

        Long result = touristDataService.getContentType(1L);

        assertEquals(12L, result);
    }

    @Test
    void increaseOverviewSim_updatesOnlyLongOverviews() {
        TouristData longOverview = touristData(1L, 12L, "긴 글", "서울");
        longOverview.setOverview("abcdefghijklmnopqrstuvwxyz");
        TouristData shortOverview = touristData(2L, 12L, "짧은 글", "서울");
        shortOverview.setOverview("short overview");
        TouristData nullOverview = touristData(3L, 12L, "널", "서울");
        nullOverview.setOverview(null);
        when(touristDataRepository.findAll()).thenReturn(List.of(longOverview, shortOverview, nullOverview));

        touristDataService.increaseOverviewSim();

        assertEquals("abcdefghijklmnopqrstuvwxy...", longOverview.getOverviewSim());
        assertNull(shortOverview.getOverviewSim());
        verify(touristDataRepository).save(longOverview);
        verify(touristDataRepository, never()).save(shortOverview);
        verify(touristDataRepository, never()).save(nullOverview);
    }

    @Test
    void getTouristPointData_mapsTouristFields() {
        TouristData touristData = touristData(10L, 12L, "관광지", "서울 종로구");
        touristData.setCat3("CAT1");
        touristData.setOverview("소개");
        touristData.setTel("02-0000-0000");
        touristData.setUseTime("09:00");
        touristData.setRestDate("월요일");
        touristData.setExpGuide("가이드");
        touristData.setParking("가능");
        touristData.setChkPet("Y");
        touristData.setHomePage("https://tour");
        touristData.setMapX(127.1);
        touristData.setMapY(37.5);
        touristData.setOverviewSim("요약");
        when(touristDataRepository.findByContentId(10L)).thenReturn(touristData);
        when(contentTypeRepository.findByCat3Code("CAT1")).thenReturn(contentType("전망대"));

        TouristDataParams result = touristDataService.getTouristPointData(10L);

        assertEquals("관광지", result.getTitle());
        assertEquals("전망대", result.getCat3Name());
        assertEquals("요약", result.getOverviewSim());
        assertEquals(127.1, result.getMapX());
    }

    @Test
    void getFoodData_mapsFoodFields() {
        TouristData touristData = touristData(20L, 39L, "식당", "부산");
        touristData.setCat3("FOOD1");
        touristData.setOpenTimeFood("10:00");
        touristData.setRestDateFood("화요일");
        touristData.setFirstMenu("비빔밥");
        touristData.setTreatMenu("비빔밥, 국밥");
        touristData.setPacking("Y");
        touristData.setParkingFood("주차 가능");
        touristData.setMapX(129.0);
        touristData.setMapY(35.1);
        touristData.setOverviewSim("맛집");
        when(touristDataRepository.findByContentId(20L)).thenReturn(touristData);
        when(contentTypeRepository.findByCat3Code("FOOD1")).thenReturn(contentType("한식"));

        TouristDataParams2 result = touristDataService.getFoodData(20L);

        assertEquals("식당", result.getTitle());
        assertEquals("한식", result.getCat3Name());
        assertEquals("비빔밥", result.getFirstMenu());
        assertEquals("주차 가능", result.getParkingFood());
    }

    @Test
    void getTouristPointId_and_getFoodId_filterByContentTypeId() {
        when(touristDataRepository.findByContentTypeId(12L)).thenReturn(List.of(
                touristData(1L, 12L, "a", "서울"),
                touristData(2L, 12L, "b", "부산")
        ));
        when(touristDataRepository.findByContentTypeId(39L)).thenReturn(List.of(
                touristData(3L, 39L, "c", "서울")
        ));

        assertEquals(List.of(1L, 2L), touristDataService.getTouristPointId());
        assertEquals(List.of(3L), touristDataService.getFoodId());
    }

    @Test
    void isThere_returnsRepositoryPresence() {
        when(touristDataRepository.findById(1L)).thenReturn(Optional.of(touristData(1L, 12L, "a", "서울")));
        when(touristDataRepository.findById(2L)).thenReturn(Optional.empty());

        assertTrue(touristDataService.isThere(1L));
        assertFalse(touristDataService.isThere(2L));
    }

    @Test
    void getTouristPointMap_and_getFoodMap_fillCoordinateArraysInOrder() {
        TouristData tourist = touristData(1L, 12L, "관광지", "서울");
        tourist.setMapX(127.0);
        tourist.setMapY(37.0);
        TouristData food = touristData(2L, 39L, "식당", "서울");
        food.setMapX(128.0);
        food.setMapY(38.0);
        when(touristDataRepository.findByContentTypeId(12L)).thenReturn(List.of(tourist));
        when(touristDataRepository.findByContentTypeId(39L)).thenReturn(List.of(food));

        Double[][] touristMap = touristDataService.getTouristPointMap();
        Double[][] foodMap = touristDataService.getFoodMap();

        assertEquals(127.0, touristMap[0][0]);
        assertEquals(37.0, touristMap[0][1]);
        assertEquals(128.0, foodMap[0][0]);
        assertEquals(38.0, foodMap[0][1]);
    }

    @Test
    void getTouristPointId2_and_getFoodId2_filterByIsJuAndType() {
        TouristData tourist = touristData(1L, 12L, "관광지", "서울");
        TouristData food = touristData(2L, 39L, "식당", "서울");
        TouristData ignored = touristData(3L, 14L, "기타", "서울");
        when(touristDataRepository.findByIsJu(0)).thenReturn(List.of(tourist, food, ignored));

        assertEquals(List.of(1L), touristDataService.getTouristPointId2());
        assertEquals(List.of(2L), touristDataService.getFoodId2());
    }

    @Test
    void getId4Image_returnsIdsWithMissingImages() {
        when(touristDataRepository.findByFirstImage(null)).thenReturn(List.of(
                touristData(10L, 12L, "관광지", "서울"),
                touristData(11L, 39L, "식당", "부산")
        ));

        assertEquals(List.of(10L, 11L), touristDataService.getId4Image());
    }

    @Test
    void getTouristPointWithFilter_onlyHashTagMode_filtersByHashTagAndTruncatesAddress() {
        Filter filter = Filter.builder()
                .areaCodeList(List.of())
                .hashTagIdList(List.of(99L))
                .build();
        TouristData match = touristData(1L, 12L, "별빛 공원", "서울 강남구");
        match.setCat3("CAT1");
        match.setOverviewSim("요약1");
        match.setFirstImage("img1");
        match.setMapX(127.1);
        match.setMapY(37.1);
        match.setTouristDataHashTags(new ArrayList<>(List.of(
                TouristDataHashTag.builder().hashTagId(99L).hashTagName("야경").build(),
                TouristDataHashTag.builder().hashTagId(100L).hashTagName("가족").build(),
                TouristDataHashTag.builder().hashTagId(101L).hashTagName("데이트").build(),
                TouristDataHashTag.builder().hashTagId(102L).hashTagName("추가").build()
        )));
        TouristData noMatch = touristData(2L, 12L, "낮 공원", "부산 해운대구");
        noMatch.setCat3("CAT1");
        noMatch.setTouristDataHashTags(new ArrayList<>(List.of(
                TouristDataHashTag.builder().hashTagId(500L).hashTagName("산책").build()
        )));
        when(contentTypeRepository.findAll()).thenReturn(List.of(contentTypeCode("CAT1", "전망대")));
        when(touristDataRepository.findAllJoinFetch()).thenReturn(List.of(match, noMatch));

        List<SearchParams1> result = touristDataService.getTouristPointWithFilter(filter, null);

        assertEquals(1, result.size());
        assertEquals("서울 강남구", result.get(0).getAddress());
        assertEquals(List.of("야경", "가족", "데이트"), result.get(0).getHashTagNames());
        assertEquals("전망대", result.get(0).getContentType());
    }

    @Test
    void getTouristPointWithFilter_areaAndKeywordMode_skipsItemsWithoutRequestedHashtags() {
        Filter filter = Filter.builder()
                .areaCodeList(List.of(1L))
                .hashTagIdList(List.of(99L))
                .build();
        TouristData match = touristData(3L, 12L, "별빛 언덕", "서울 특별시 종로구");
        match.setCat3("CAT1");
        match.setOverviewSim("요약");
        match.setTouristDataHashTags(new ArrayList<>(List.of(
                TouristDataHashTag.builder().hashTagId(99L).hashTagName("야경").build()
        )));
        TouristData skipped = touristData(4L, 12L, "별빛 정원", "서울 중구");
        skipped.setCat3("CAT1");
        skipped.setTouristDataHashTags(new ArrayList<>());
        when(contentTypeRepository.findAll()).thenReturn(List.of(contentTypeCode("CAT1", "전망대")));
        when(touristDataRepository.findByAreaCodesTitle("별빛", List.of(1L))).thenReturn(List.of(match, skipped));

        List<SearchParams1> result = touristDataService.getTouristPointWithFilter(filter, "별빛");

        assertEquals(1, result.size());
        assertEquals("서울 특별시", result.get(0).getAddress());
        assertEquals(List.of("야경"), result.get(0).getHashTagNames());
    }

    @Test
    void getTouristPointWithFilterForMap_respectsMapLimitAndSameFilteringRules() {
        Filter filter = Filter.builder()
                .areaCodeList(List.of())
                .hashTagIdList(List.of())
                .build();
        TouristData touristData = touristData(5L, 12L, "지도용", "제주 서귀포시");
        touristData.setCat3("CAT2");
        touristData.setOverviewSim("요약");
        touristData.setTouristDataHashTags(new ArrayList<>());
        when(contentTypeRepository.findAll()).thenReturn(List.of(contentTypeCode("CAT2", "자연")));
        when(touristDataRepository.findByTitleContaining("지도")).thenReturn(List.of(touristData));

        List<SearchParams1> result = touristDataService.getTouristPointWithFilterForMap(filter, "지도");

        assertEquals(1, result.size());
        assertEquals("제주 서귀포시", result.get(0).getAddress());
        assertEquals("자연", result.get(0).getContentType());
    }

    private TouristData touristData(Long contentId, Long contentTypeId, String title, String addr) {
        TouristData touristData = new TouristData();
        touristData.setContentId(contentId);
        touristData.setContentTypeId(contentTypeId);
        touristData.setTitle(title);
        touristData.setAddr(addr);
        touristData.setTouristDataHashTags(new ArrayList<>());
        return touristData;
    }

    private ContentType contentType(String cat3Name) {
        ContentType contentType = new ContentType();
        contentType.setCat3Name(cat3Name);
        return contentType;
    }

    private ContentType contentTypeCode(String cat3Code, String cat3Name) {
        ContentType contentType = new ContentType();
        contentType.setCat3Code(cat3Code);
        contentType.setCat3Name(cat3Name);
        return contentType;
    }
}
