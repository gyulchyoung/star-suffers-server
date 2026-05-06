package com.server.tourApiProject.likes;

import com.server.tourApiProject.bigPost.post.Post;
import com.server.tourApiProject.bigPost.post.PostRepository;
import com.server.tourApiProject.bigPost.postHashTag.PostHashTagRepository;
import com.server.tourApiProject.bigPost.postImage.PostImageRepository;
import com.server.tourApiProject.observation.Observation;
import com.server.tourApiProject.observation.ObservationRepository;
import com.server.tourApiProject.observation.observeHashTag.ObserveHashTagRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

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
    private LikeService likeService;

    @Test
    void createLike_forObservation_savesLikeRecord() {
        User user = user(1L);
        Observation observation = observation(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(observationRepository.findById(10L)).thenReturn(Optional.of(observation));

        likeService.createLike(1L, 10L, 0);

        ArgumentCaptor<Likes> captor = ArgumentCaptor.forClass(Likes.class);
        verify(likeRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getLikeType());
        assertEquals(10L, captor.getValue().getItemId());
        assertTrue(captor.getValue().getLikeTime() > 0L);
    }

    @Test
    void createLike_forTouristData_savesLikeRecord() {
        User user = user(1L);
        TouristData touristData = new TouristData();
        touristData.setContentId(20L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(touristDataRepository.findById(20L)).thenReturn(Optional.of(touristData));

        likeService.createLike(1L, 20L, 1);

        ArgumentCaptor<Likes> captor = ArgumentCaptor.forClass(Likes.class);
        verify(likeRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getLikeType());
        assertEquals(20L, captor.getValue().getItemId());
    }

    @Test
    void createLike_forPost_incrementsPostLikedCount() {
        User user = user(1L);
        Post post = post(30L, 4L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(30L)).thenReturn(Optional.of(post));

        likeService.createLike(1L, 30L, 2);

        ArgumentCaptor<Likes> captor = ArgumentCaptor.forClass(Likes.class);
        verify(likeRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getLikeType());
        assertEquals(5L, post.getLiked());
    }

    @Test
    void isThereLike_returnsPresenceOfLikeRecord() {
        when(likeRepository.findByUserIdAndItemIdAndLikeType(1L, 30L, 2))
                .thenReturn(Optional.of(new Likes()));

        assertTrue(likeService.isThereLike(1L, 30L, 2));
    }

    @Test
    void deleteLike_removesLikeAndDecrementsPostLikedCount() {
        Likes like = Likes.builder().userId(1L).itemId(30L).likeType(2).build();
        Post post = post(30L, 7L);
        when(likeRepository.findByUserIdAndItemIdAndLikeType(1L, 30L, 2)).thenReturn(Optional.of(like));
        when(postRepository.findById(30L)).thenReturn(Optional.of(post));

        likeService.deleteLike(1L, 30L, 2);

        verify(likeRepository).delete(like);
        assertEquals(6L, post.getLiked());
    }

    @Test
    void getLikeCount_returnsPostLikedValue() {
        Post post = post(30L, 9L);
        when(postRepository.findById(30L)).thenReturn(Optional.of(post));

        Long result = likeService.getLikeCount(30L, 2);

        assertEquals(9L, result);
    }

    private User user(Long userId) {
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    private Observation observation(Long observationId) {
        Observation observation = new Observation();
        observation.setObservationId(observationId);
        observation.setObservationName("관측지");
        observation.setAddress("서울");
        observation.setLatitude(37.0);
        observation.setLongitude(127.0);
        return observation;
    }

    private Post post(Long postId, Long liked) {
        Post post = new Post();
        post.setPostId(postId);
        post.setLiked(liked);
        return post;
    }
}
