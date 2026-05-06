package com.server.tourApiProject.observation;

import com.server.tourApiProject.hashTag.HashTag;
import com.server.tourApiProject.hashTag.HashTagRepository;
import com.server.tourApiProject.observation.model.ObservationParams;
import com.server.tourApiProject.observation.model.ObservationSimpleParams;
import com.server.tourApiProject.observation.observeFee.ObserveFee;
import com.server.tourApiProject.observation.observeFee.ObserveFeeRepository;
import com.server.tourApiProject.observation.observeHashTag.ObserveHashTag;
import com.server.tourApiProject.observation.observeHashTag.ObserveHashTagParams;
import com.server.tourApiProject.observation.observeHashTag.ObserveHashTagRepository;
import com.server.tourApiProject.observation.observeImage.ObserveImage;
import com.server.tourApiProject.observation.observeImage.ObserveImageRepository;
import com.server.tourApiProject.search.Filter;
import com.server.tourApiProject.search.SearchParams1;
import com.server.tourApiProject.weather.area.WeatherArea;
import com.server.tourApiProject.weather.area.WeatherAreaRepository;
import com.server.tourApiProject.weather.observationalFit.ObservationalFitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservationServiceImplTest {

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private ObserveHashTagRepository observeHashTagRepository;

    @Mock
    private HashTagRepository hashTagRepository;

    @Mock
    private ObserveFeeRepository observeFeeRepository;

    @Mock
    private ObserveImageRepository observeImageRepository;

    @Mock
    private ObservationalFitRepository observationalFitRepository;

    @Mock
    private WeatherAreaRepository weatherAreaRepository;

    @Spy
    @InjectMocks
    private ObservationServiceImpl observationService;

    @Test
    void createObservation_mapsInputFieldsAndInitialSavedCount() {
        ObservationParams params = ObservationParams.builder()
                .observationName("별빛공원")
                .link("https://obs")
                .latitude(37.5)
                .longitude(127.0)
                .address("서울 중구")
                .phoneNumber("02-123-4567")
                .operatingHour("09:00-18:00")
                .parking("Y")
                .observeType("천문대")
                .outline("야간 관측")
                .build();

        observationService.createObservation(params);

        ArgumentCaptor<Observation> captor = ArgumentCaptor.forClass(Observation.class);
        verify(observationRepository).save(captor.capture());
        assertEquals("별빛공원", captor.getValue().getObservationName());
        assertEquals("서울 중구", captor.getValue().getAddress());
        assertEquals(0L, captor.getValue().getSaved());
    }

    @Test
    void createObserveHashTags_createsEntriesUsingResolvedHashTagIds() {
        Observation observation = observation(1L, "서울 강남구");
        HashTag night = HashTag.builder().hashTagId(10L).hashTagName("야경").build();
        HashTag family = HashTag.builder().hashTagId(11L).hashTagName("가족").build();
        when(observationRepository.findById(1L)).thenReturn(Optional.of(observation));
        when(hashTagRepository.findByHashTagName("야경")).thenReturn(night);
        when(hashTagRepository.findByHashTagName("가족")).thenReturn(family);

        observationService.createObserveHashTags(1L, List.of(
                new ObserveHashTagParams("야경"),
                new ObserveHashTagParams("가족")
        ));

        ArgumentCaptor<ObserveHashTag> captor = ArgumentCaptor.forClass(ObserveHashTag.class);
        verify(observeHashTagRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(10L, captor.getAllValues().get(0).getHashTagId());
        assertEquals("가족", captor.getAllValues().get(1).getHashTagName());
        assertEquals(1L, captor.getAllValues().get(1).getObservationId());
    }

    @Test
    void createObserveFees_addsAllFeesToObservation() {
        Observation observation = observation(2L, "서울 강남구");
        ObserveFee adult = ObserveFee.builder().feeName("성인").build();
        ObserveFee child = ObserveFee.builder().feeName("아동").build();
        when(observationRepository.findById(2L)).thenReturn(Optional.of(observation));

        observationService.createObserveFees(2L, List.of(adult, child));

        assertEquals(List.of(adult, child), observation.getObserveFees());
    }

    @Test
    void getObservation_returnsEntityFromRepository() {
        Observation observation = observation(3L, "서울 강남구");
        when(observationRepository.findById(3L)).thenReturn(Optional.of(observation));

        Observation result = observationService.getObservation(3L);

        assertSame(observation, result);
    }

    @Test
    void getObservationWithFilter_mapsThumbnailAddressAndUpToThreeHashtags() {
        Pageable pageable = PageRequest.of(0, 10);
        Filter filter = Filter.builder()
                .areaCodeList(List.of(1L))
                .hashTagIdList(List.of(2L))
                .build();

        Observation skipObservation = observation(999L, "제외 대상");
        Observation richObservation = observation(100L, "서울 특별시 강남구");
        richObservation.setObservationName("강남 관측지");
        richObservation.setObserveType("천문대");
        richObservation.setIntro("소개");
        richObservation.setLatitude(37.1);
        richObservation.setLongitude(127.1);
        richObservation.setLight(2.5);
        richObservation.setSaved(5L);
        richObservation.setObserveImages(new ArrayList<>(List.of(
                ObserveImage.builder().image("thumb.png").build()
        )));
        richObservation.setObserveHashTags(new ArrayList<>(List.of(
                ObserveHashTag.builder().hashTagName("야경").build(),
                ObserveHashTag.builder().hashTagName("가족").build(),
                ObserveHashTag.builder().hashTagName("드라이브").build(),
                ObserveHashTag.builder().hashTagName("추가태그").build()
        )));

        Observation noImageObservation = observation(101L, "부산");
        noImageObservation.setObservationName("부산 관측지");
        noImageObservation.setObserveType("공원");
        noImageObservation.setObserveImages(new ArrayList<>());
        noImageObservation.setObserveHashTags(new ArrayList<>(List.of(
                ObserveHashTag.builder().hashTagName("한적").build()
        )));

        when(observationRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(skipObservation, richObservation, noImageObservation)));

        List<SearchParams1> result = observationService.getObservationWithFilter(filter, "sky", pageable);

        assertEquals(2, result.size());
        assertEquals("서울 특별시", result.get(0).getAddress());
        assertEquals("thumb.png", result.get(0).getThumbnail());
        assertEquals(List.of("야경", "가족", "드라이브"), result.get(0).getHashTagNames());
        assertEquals("부산", result.get(1).getAddress());
        assertNull(result.get(1).getThumbnail());
        assertEquals(List.of("한적"), result.get(1).getHashTagNames());
    }

    @Test
    void getCountWithFilter_delegatesToRepositoryCount() {
        Filter filter = Filter.builder()
                .areaCodeList(List.of(1L))
                .hashTagIdList(List.of(2L))
                .build();
        when(observationRepository.count(any(Specification.class))).thenReturn(12L);

        Long result = observationService.getCountWithFilter(filter, "night");

        assertEquals(12L, result);
    }

    @Test
    void getObservationSimpleList_usesComputedDate() {
        String expectedDate = expectedFitDate();
        List<ObservationSimpleParams> expected = List.of(
                new ObservationSimpleParams(1L, null, "관측지", "서울", "소개", 127.0, 37.0, 1.0, 90.0, 3L)
        );
        when(observationRepository.findObservationSimpleByIdList(List.of(1L), expectedDate)).thenReturn(expected);

        List<ObservationSimpleParams> result = observationService.getObservationSimpleList(List.of(1L));

        assertSame(expected, result);
    }

    @Test
    void getBestFitObservationList_looksUpIdsThenLoadsSimpleParams() {
        String expectedDate = expectedFitDate();
        List<Long> ids = List.of(3L, 4L);
        List<ObservationSimpleParams> expected = List.of(
                new ObservationSimpleParams(3L, null, "A", "서울", null, 0.0, 0.0, 0.0, 80.0, 1L)
        );
        when(observationalFitRepository.getObservationIdsByBestFit(expectedDate)).thenReturn(ids);
        when(observationRepository.findObservationSimpleByIdList(ids, expectedDate)).thenReturn(expected);

        List<ObservationSimpleParams> result = observationService.getBestFitObservationList();

        assertSame(expected, result);
    }

    @Test
    void getNearObservationIds_usesWeatherAreaCoordinatesAndComputedDate() {
        String expectedDate = expectedFitDate();
        WeatherArea weatherArea = WeatherArea.builder()
                .areaId(5L)
                .latitude(37.55)
                .longitude(126.99)
                .lightPollution(1.0)
                .SD("서울")
                .build();
        List<Long> ids = List.of(8L, 9L);
        List<ObservationSimpleParams> expected = List.of(
                new ObservationSimpleParams(8L, null, "가까운 관측지", "서울", null, 0.0, 0.0, 0.0, 70.0, 0L)
        );
        when(weatherAreaRepository.findById(5L)).thenReturn(Optional.of(weatherArea));
        when(observationRepository.findNearObservationIds(37.55, 126.99, 2)).thenReturn(ids);
        when(observationRepository.findObservationSimpleByIdList(ids, expectedDate)).thenReturn(expected);

        List<ObservationSimpleParams> result = observationService.getNearObservationIds(5L, 2);

        assertSame(expected, result);
    }

    private String expectedFitDate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime latest = now.getHour() < 8 ? now.minusDays(1) : now;
        return latest.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private Observation observation(Long id, String address) {
        Observation observation = new Observation();
        observation.setObservationId(id);
        observation.setObservationName("관측지-" + id);
        observation.setAddress(address);
        observation.setLatitude(37.0);
        observation.setLongitude(127.0);
        observation.setObserveImages(new ArrayList<>());
        observation.setObserveHashTags(new ArrayList<>());
        observation.setObserveFees(new ArrayList<>());
        return observation;
    }
}
