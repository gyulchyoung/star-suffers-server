package com.server.tourApiProject.notice;

import com.server.tourApiProject.alarm.Alarm;
import com.server.tourApiProject.alarm.AlarmService;
import com.server.tourApiProject.fcm.FcmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private FcmService fcmService;

    @Mock
    private AlarmService alarmService;

    @InjectMocks
    private NoticeService noticeService;

    @Test
    void getAllNotice_returnsRepositoryResult() {
        List<Notice> notices = List.of(
                Notice.builder().noticeId(1L).noticeTitle("공지").noticeContent("내용").noticeDate("2026-04-21").build()
        );
        when(noticeRepository.findAll()).thenReturn(notices);

        List<Notice> result = noticeService.getAllNotice();

        assertSame(notices, result);
    }

    @Test
    void getNotice_returnsStoredNotice() {
        Notice notice = Notice.builder().noticeId(1L).noticeTitle("공지").noticeContent("내용").noticeDate("2026-04-21").build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        Notice result = noticeService.getNotice(1L);

        assertSame(notice, result);
    }

    @Test
    void createNotice_savesNotice_sendsMessageAndCreatesAlarm() throws InterruptedException {
        NoticeParams params = NoticeParams.builder()
                .noticeTitle("긴급 공지")
                .noticeContent("A".repeat(220))
                .noticeDate("2026-04-21")
                .build();
        when(noticeRepository.save(any(Notice.class))).thenAnswer(invocation -> {
            Notice notice = invocation.getArgument(0);
            notice.setNoticeId(100L);
            return notice;
        });
        when(fcmService.getAllFcmToken()).thenReturn(List.of("token-1", "token-2"));

        noticeService.createNotice(params);

        ArgumentCaptor<Notice> noticeCaptor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(noticeCaptor.capture());
        assertEquals("긴급 공지", noticeCaptor.getValue().getNoticeTitle());

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(fcmService).sendMessageAll(any(List.class), eq("긴급 공지"), bodyCaptor.capture());
        assertEquals(198, bodyCaptor.getValue().length());

        ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
        verify(alarmService).createAlarm(alarmCaptor.capture());
        assertEquals("긴급 공지", alarmCaptor.getValue().getAlarmTitle());
        assertEquals("notice", alarmCaptor.getValue().getIsNotice());
        assertEquals(100L, alarmCaptor.getValue().getItemId());
    }

    @Test
    void deleteNoticewithId_deletesById() {
        noticeService.deleteNoticewithId(3L);

        verify(noticeRepository).deleteById(3L);
    }

    @Test
    void updateNotice_updatesExistingNoticeOnly() {
        Notice notice = Notice.builder().noticeId(10L).noticeTitle("old").noticeContent("old body").noticeDate("2026-04-21").build();
        when(noticeRepository.findById(10L)).thenReturn(Optional.of(notice));

        noticeService.updateNotice(new NoticeUpdateParam(10L, "new", "new body"));

        assertEquals("new", notice.getNoticeTitle());
        assertEquals("new body", notice.getNoticeContent());
        verify(noticeRepository).save(notice);
    }
}
