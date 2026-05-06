package com.server.tourApiProject.observation.course;

import com.server.tourApiProject.observation.Observation;
import com.server.tourApiProject.observation.ObservationRepository;
import com.server.tourApiProject.touristPoint.contentType.ContentType;
import com.server.tourApiProject.touristPoint.contentType.ContentTypeRepository;
import com.server.tourApiProject.touristPoint.touristData.TouristData;
import com.server.tourApiProject.touristPoint.touristData.TouristDataCourseParams;
import com.server.tourApiProject.touristPoint.touristData.TouristDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TouristDataRepository touristDataRepository;

    @Mock
    private ContentTypeRepository contentTypeRepository;

    @Mock
    private ObservationRepository observationRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void getCourseTPList_sortsCoursesByOrderBeforeMappingTouristPoints() {
        Course later = Course.builder().observationId(1L).touristPointId(200L).courseOrder(2).build();
        Course earlier = Course.builder().observationId(1L).touristPointId(100L).courseOrder(1).build();
        when(courseRepository.findByObservationId(1L)).thenReturn(List.of(later, earlier));
        when(touristDataRepository.findByContentId(100L)).thenReturn(touristData(100L, 12L, "A", "CAT_A"));
        when(touristDataRepository.findByContentId(200L)).thenReturn(touristData(200L, 39L, "B", "CAT_B"));
        when(contentTypeRepository.findByCat3Code("CAT_A")).thenReturn(contentType("전망대"));
        when(contentTypeRepository.findByCat3Code("CAT_B")).thenReturn(contentType("맛집"));

        List<TouristDataCourseParams> result = courseService.getCourseTPList(1L);

        assertEquals(List.of("A", "B"), result.stream().map(TouristDataCourseParams::getTitle).toList());
    }

    @Test
    void getCourseNameList_insertsObservationNameAtObservationCourseOrder() {
        Observation observation = new Observation();
        observation.setObservationId(1L);
        observation.setObservationName("메인 관측지");
        observation.setCourseOrder(1);
        when(courseRepository.findByObservationId(1L)).thenReturn(List.of(
                Course.builder().observationId(1L).touristPointId(100L).courseOrder(0).build(),
                Course.builder().observationId(1L).touristPointId(200L).courseOrder(2).build()
        ));
        when(observationRepository.findById(1L)).thenReturn(Optional.of(observation));
        when(touristDataRepository.findByContentId(100L)).thenReturn(touristData(100L, 12L, "첫 코스", "CAT_A"));
        when(touristDataRepository.findByContentId(200L)).thenReturn(touristData(200L, 39L, "마지막 코스", "CAT_B"));
        when(contentTypeRepository.findByCat3Code("CAT_A")).thenReturn(contentType("전망대"));
        when(contentTypeRepository.findByCat3Code("CAT_B")).thenReturn(contentType("맛집"));

        List<String> result = courseService.getCourseNameList(1L);

        assertEquals(List.of("첫 코스", "메인 관측지", "마지막 코스"), result);
    }

    @Test
    void getCourseTouristPointData_mapsTouristAttractionFieldsForContentType12() {
        TouristData touristData = touristData(100L, 12L, "관광지", "CAT_A");
        touristData.setUseTime("09:00-18:00");
        touristData.setParking("가능");
        when(touristDataRepository.findByContentId(100L)).thenReturn(touristData);
        when(contentTypeRepository.findByCat3Code("CAT_A")).thenReturn(contentType("전망대"));

        TouristDataCourseParams result = courseService.getCourseTouristPointData(100L);

        assertEquals("09:00-18:00", result.getUseTime());
        assertEquals("가능", result.getParking());
        assertEquals("전망대", result.getCat3Name());
    }

    @Test
    void getCourseTouristPointData_mapsRestaurantFieldsForNonTouristContent() {
        TouristData touristData = touristData(200L, 39L, "식당", "CAT_B");
        touristData.setOpenTimeFood("10:00-22:00");
        touristData.setTreatMenu("칼국수");
        touristData.setParkingFood("주차장");
        when(touristDataRepository.findByContentId(200L)).thenReturn(touristData);
        when(contentTypeRepository.findByCat3Code("CAT_B")).thenReturn(contentType("한식"));

        TouristDataCourseParams result = courseService.getCourseTouristPointData(200L);

        assertEquals("10:00-22:00", result.getUseTime());
        assertEquals("칼국수", result.getTreatMenu());
        assertEquals("주차장", result.getParking());
        assertEquals("한식", result.getCat3Name());
    }

    private TouristData touristData(Long contentId, Long contentTypeId, String title, String cat3) {
        TouristData touristData = new TouristData();
        touristData.setContentId(contentId);
        touristData.setContentTypeId(contentTypeId);
        touristData.setTitle(title);
        touristData.setCat3(cat3);
        touristData.setFirstImage(title + ".png");
        touristData.setOverview(title + " 소개");
        touristData.setAddr(title + " 주소");
        return touristData;
    }

    private ContentType contentType(String cat3Name) {
        ContentType contentType = new ContentType();
        contentType.setCat3Name(cat3Name);
        return contentType;
    }
}
