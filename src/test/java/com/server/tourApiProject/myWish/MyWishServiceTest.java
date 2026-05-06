package com.server.tourApiProject.myWish;

import com.server.tourApiProject.bigPost.post.Post;
import com.server.tourApiProject.bigPost.post.PostRepository;
import com.server.tourApiProject.bigPost.postHashTag.PostHashTagRepository;
import com.server.tourApiProject.bigPost.postImage.PostImage;
import com.server.tourApiProject.bigPost.postImage.PostImageRepository;
import com.server.tourApiProject.observation.Observation;
import com.server.tourApiProject.observation.ObservationRepository;
import com.server.tourApiProject.observation.observeHashTag.ObserveHashTagRepository;
import com.server.tourApiProject.observation.observeImage.ObserveImage;
import com.server.tourApiProject.observation.observeImage.ObserveImageRepository;
import com.server.tourApiProject.touristPoint.contentType.ContentTypeRepository;
import com.server.tourApiProject.touristPoint.touristData.TouristData;
import com.server.tourApiProject.touristPoint.touristData.TouristDataRepository;
import com.server.tourApiProject.touristPoint.touristDataHashTag.TouristDataHashTagRepository;
import com.server.tourApiProject.user.User;
import com.server.tourApiProject.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyWishServiceTest {

    @Mock
    private MyWishRepository myWishRepository;

    @Mock
    private ContentTypeRepository contentTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private ObserveImageRepository observeImageRepository;

    @Mock
    private ObserveHashTagRepository observeHashTagRepository;

    @Mock
    private TouristDataRepository touristDataRepository;

    @Mock
    private TouristDataHashTagRepository touristDataHashTagRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private PostHashTagRepository postHashTagRepository;

    @InjectMocks
    private MyWishService myWishService;

    @Test
    void createMyWish_forObservation_savesWishAndIncrementsSavedCount() {
        User user = user(1L);
        Observation observation = observation(10L, 3L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(observationRepository.findById(10L)).thenReturn(Optional.of(observation));

        myWishService.createMyWish(1L, 10L, 0);

        ArgumentCaptor<MyWish> captor = ArgumentCaptor.forClass(MyWish.class);
        verify(myWishRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(10L, captor.getValue().getItemId());
        assertEquals(0, captor.getValue().getWishType());
        assertTrue(captor.getValue().getWishTime() > 0L);
        assertEquals(4L, observation.getSaved());
    }

    @Test
    void createMyWish_forPost_initializesSavedCountWhenNull() {
        User user = user(2L);
        Post post = post(20L, null, "게시글");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(postRepository.findById(20L)).thenReturn(Optional.of(post));

        myWishService.createMyWish(2L, 20L, 2);

        assertEquals(1L, post.getSaved());
        verify(myWishRepository).save(any(MyWish.class));
    }

    @Test
    void isThereMyWish_returnsPresenceOfWish() {
        when(myWishRepository.findByUserIdAndItemIdAndWishType(1L, 10L, 0))
                .thenReturn(Optional.of(new MyWish()));

        assertTrue(myWishService.isThereMyWish(1L, 10L, 0));
    }

    @Test
    void deleteMyWish_forObservation_deletesWishAndDecrementsSavedCount() {
        MyWish myWish = MyWish.builder().userId(1L).itemId(10L).wishType(0).build();
        Observation observation = observation(10L, 5L);
        when(myWishRepository.findByUserIdAndItemIdAndWishType(1L, 10L, 0)).thenReturn(Optional.of(myWish));
        when(observationRepository.findById(10L)).thenReturn(Optional.of(observation));

        myWishService.deleteMyWish(1L, 10L, 0);

        verify(myWishRepository).delete(myWish);
        assertEquals(4L, observation.getSaved());
    }

    @Test
    void getMyWish3_returnsLatestThreeItemsWithTypeSpecificTitlesAndThumbnails() {
        List<MyWish> wishes = List.of(
                MyWish.builder().wishType(0).itemId(10L).wishTime(1L).build(),
                MyWish.builder().wishType(1).itemId(11L).wishTime(2L).build(),
                MyWish.builder().wishType(2).itemId(12L).wishTime(3L).build(),
                MyWish.builder().wishType(0).itemId(13L).wishTime(4L).build()
        );
        Observation latestObservation = observation(13L, 0L);
        latestObservation.setObservationName("최근 관측지");
        TouristData touristData = new TouristData();
        touristData.setContentId(11L);
        touristData.setTitle("관광지");
        touristData.setFirstImage("tour.png");
        Post post = post(12L, 2L, "게시글");
        when(myWishRepository.findByUserId(1L)).thenReturn(wishes);
        when(touristDataRepository.findById(11L)).thenReturn(Optional.of(touristData));
        when(postRepository.findById(12L)).thenReturn(Optional.of(post));
        when(postImageRepository.findByPostId(12L)).thenReturn(List.of(PostImage.builder().imageName("post.png").postId(12L).build()));
        when(observationRepository.findById(13L)).thenReturn(Optional.of(latestObservation));
        when(observeImageRepository.findByObservationId(13L)).thenReturn(List.of());

        List<MyWishParams3> result = myWishService.getMyWish3(1L);

        assertEquals(3, result.size());
        assertEquals(0, result.get(0).getWishType());
        assertEquals("최근 관측지", result.get(0).getTitle());
        assertNull(result.get(0).getThumbnail());
        assertEquals(2, result.get(1).getWishType());
        assertEquals("post.png", result.get(1).getThumbnail());
        assertEquals(1, result.get(2).getWishType());
        assertEquals("tour.png", result.get(2).getThumbnail());
    }

    @Test
    void updateSavedCount_updatesObservationAndPostCountsAndReturnsProjectionSize() {
        WishCountParams.WishCount observationCount = wishCount(10L, 0, 7L);
        WishCountParams.WishCount postCount = wishCount(20L, 2, 4L);
        WishCountParams.WishCount unsupportedCount = wishCount(30L, 1, 2L);
        Observation observation = observation(10L, 0L);
        Post post = post(20L, 0L, "게시글");
        when(myWishRepository.findWishCount()).thenReturn(List.of(observationCount, postCount, unsupportedCount));
        when(observationRepository.getById(10L)).thenReturn(observation);
        when(postRepository.getById(20L)).thenReturn(post);

        Integer result = myWishService.updateSavedCount();

        assertEquals(3, result);
        assertEquals(7L, observation.getSaved());
        assertEquals(4L, post.getSaved());
        verify(observationRepository).save(observation);
        verify(postRepository).save(post);
    }

    @Test
    void updateSavedCount_returnsZeroWhenProjectionIsEmpty() {
        when(myWishRepository.findWishCount()).thenReturn(List.of());

        Integer result = myWishService.updateSavedCount();

        assertEquals(0, result);
        verify(observationRepository, never()).save(any(Observation.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    private User user(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user" + userId + "@test.com");
        user.setNickName("user-" + userId);
        user.setSignUpDt(LocalDateTime.now());
        return user;
    }

    private Observation observation(Long observationId, Long saved) {
        Observation observation = new Observation();
        observation.setObservationId(observationId);
        observation.setObservationName("관측지-" + observationId);
        observation.setSaved(saved);
        observation.setAddress("서울");
        observation.setLatitude(37.0);
        observation.setLongitude(127.0);
        return observation;
    }

    private Post post(Long postId, Long saved, String title) {
        Post post = new Post();
        post.setPostId(postId);
        post.setSaved(saved);
        post.setPostTitle(title);
        post.setUser(user(99L));
        return post;
    }

    private WishCountParams.WishCount wishCount(Long itemId, Integer wishType, Long count) {
        return new WishCountParams.WishCount() {
            @Override
            public Long getItemId() {
                return itemId;
            }

            @Override
            public Integer getWishType() {
                return wishType;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }
}
