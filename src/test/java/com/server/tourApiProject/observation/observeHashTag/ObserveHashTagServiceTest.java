package com.server.tourApiProject.observation.observeHashTag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObserveHashTagServiceTest {

    @Mock
    private ObserveHashTagRepository observeHashTagRepository;

    @InjectMocks
    private ObserveHashTagService observeHashTagService;

    @Test
    void getObserveHashTag_returnsHashTagNamesInRepositoryOrder() {
        ObserveHashTag first = ObserveHashTag.builder()
                .observationId(5L)
                .hashTagId(1L)
                .hashTagName("가족")
                .build();
        ObserveHashTag second = ObserveHashTag.builder()
                .observationId(5L)
                .hashTagId(2L)
                .hashTagName("야경")
                .build();
        when(observeHashTagRepository.findByObservationId(5L)).thenReturn(List.of(first, second));

        List<String> result = observeHashTagService.getObserveHashTag(5L);

        assertEquals(List.of("가족", "야경"), result);
    }
}
