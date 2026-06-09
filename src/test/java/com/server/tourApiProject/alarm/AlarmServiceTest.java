package com.server.tourApiProject.alarm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @InjectMocks
    private AlarmService alarmService;

    @Test
    void getAllAlarm_returnsGlobalAndUserSpecificAlarmsOnly() {
        Alarm global = Alarm.builder().alarmId(1L).alarmTitle("전체").userId(null).build();
        Alarm mine = Alarm.builder().alarmId(2L).alarmTitle("내 알림").userId(7L).build();
        Alarm others = Alarm.builder().alarmId(3L).alarmTitle("다른 사람").userId(8L).build();
        when(alarmRepository.findAll()).thenReturn(List.of(global, mine, others));

        List<Alarm> result = alarmService.getAllAlarm(7L);

        assertEquals(List.of(global, mine), result);
    }

    @Test
    void createAlarm_savesAlarm() {
        Alarm alarm = Alarm.builder().alarmTitle("공지").build();

        alarmService.createAlarm(alarm);

        verify(alarmRepository).save(alarm);
    }
}
