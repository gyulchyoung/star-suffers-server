package com.server.tourApiProject.observation.observeFee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObserveFeeServiceTest {

    @Mock
    private ObserveFeeRepository observeFeeRepository;

    @InjectMocks
    private ObserveFeeService observeFeeService;

    @Test
    void getObserveFees_returnsRepositoryResultWithoutModification() {
        List<ObserveFee> observeFees = List.of(
                ObserveFee.builder()
                        .observeFeeListId(1L)
                        .observationId(8L)
                        .feeName("입장료")
                        .entranceFee("1000")
                        .build()
        );
        when(observeFeeRepository.findByObservationId(8L)).thenReturn(observeFees);

        List<ObserveFee> result = observeFeeService.getObserveFees(8L);

        assertSame(observeFees, result);
    }
}
