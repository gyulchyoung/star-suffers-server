package com.server.tourApiProject.searchFirst;

import com.server.tourApiProject.observation.observeImage.ObserveImage;
import com.server.tourApiProject.observation.observeImage.ObserveImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchFirstServiceTest {

    @Mock
    private SearchFirstRepository searchFirstRepository;

    @Mock
    private ObserveImageRepository observeImageRepository;

    @InjectMocks
    private SearchFirstService searchFirstService;

    @Test
    void getSearchFirst_setsObservationImageWhenImageExists() {
        SearchFirst searchFirst = SearchFirst.builder()
                .observationId(100L)
                .observationName("별 관측지")
                .build();
        ObserveImage observeImage = ObserveImage.builder()
                .observationId(100L)
                .image("https://image.test/obs-100.png")
                .build();

        when(searchFirstRepository.findByTypeName("recommend")).thenReturn(List.of(searchFirst));
        when(observeImageRepository.findByObservationId(100L)).thenReturn(List.of(observeImage));

        List<SearchFirstParams> result = searchFirstService.getSearchFirst("recommend");

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getObservationId());
        assertEquals("별 관측지", result.get(0).getObservationName());
        assertEquals("https://image.test/obs-100.png", result.get(0).getObservationImage());
        verify(observeImageRepository, times(2)).findByObservationId(100L);
    }

    @Test
    void getSearchFirst_leavesObservationImageNullWhenNoImageExists() {
        SearchFirst searchFirst = SearchFirst.builder()
                .observationId(101L)
                .observationName("은하수 명소")
                .build();

        when(searchFirstRepository.findByTypeName("recent")).thenReturn(List.of(searchFirst));
        when(observeImageRepository.findByObservationId(101L)).thenReturn(Collections.emptyList());

        List<SearchFirstParams> result = searchFirstService.getSearchFirst("recent");

        assertEquals(1, result.size());
        assertNull(result.get(0).getObservationImage());
        verify(observeImageRepository).findByObservationId(101L);
    }
}
