package com.server.tourApiProject.subBanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubBannerServiceTest {

    @Mock
    private SubBannerRepository subBannerRepository;

    @InjectMocks
    private SubBannerService subBannerService;

    @Test
    void createSubBanner_savesBanner() {
        SubBanner subBanner = SubBanner.builder().bannerImage("banner.png").link("https://x").isShow(true).build();

        subBannerService.createSubBanner(subBanner);

        verify(subBannerRepository).save(subBanner);
    }

    @Test
    void getLastSubBanner_returnsLatestBanner() {
        SubBanner subBanner = SubBanner.builder().subBannerId(10L).bannerImage("latest.png").build();
        when(subBannerRepository.findFirstByOrderBySubBannerIdDesc()).thenReturn(subBanner);

        SubBanner result = subBannerService.getLastSubBanner();

        assertSame(subBanner, result);
    }
}
