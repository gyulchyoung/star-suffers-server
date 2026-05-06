package com.server.tourApiProject.observation.observeImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObserveImageServiceTest {

    @Mock
    private ObserveImageRepository observeImageRepository;

    @InjectMocks
    private ObserveImageService observeImageService;

    @Test
    void getObserveImage_returnsOnlyImagePaths() {
        ObserveImage first = ObserveImage.builder()
                .observationId(1L)
                .image("image-1")
                .imageSource("source-1")
                .build();
        ObserveImage second = ObserveImage.builder()
                .observationId(1L)
                .image("image-2")
                .imageSource("source-2")
                .build();
        when(observeImageRepository.findByObservationId(1L)).thenReturn(List.of(first, second));

        List<String> result = observeImageService.getObserveImage(1L);

        assertEquals(List.of("image-1", "image-2"), result);
    }

    @Test
    void getObserveImageInfo_returnsImageAndSourcePairs() {
        ObserveImage observeImage = ObserveImage.builder()
                .observationId(2L)
                .image("image-path")
                .imageSource("ktoa")
                .build();
        when(observeImageRepository.findByObservationId(2L)).thenReturn(List.of(observeImage));

        List<ObserveImageParams2> result = observeImageService.getObserveImageInfo(2L);

        assertEquals(1, result.size());
        assertEquals("image-path", result.get(0).getImage());
        assertEquals("ktoa", result.get(0).getImageSource());
    }
}
