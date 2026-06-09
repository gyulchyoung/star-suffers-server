package com.server.tourApiProject.bigPost.post;

import com.server.tourApiProject.bigPost.postHashTag.PostHashTag;
import com.server.tourApiProject.bigPost.postHashTag.PostHashTagRepository;
import com.server.tourApiProject.bigPost.postImage.PostImage;
import com.server.tourApiProject.bigPost.postImage.PostImageRepository;
import com.server.tourApiProject.myWish.MyWishRepository;
import com.server.tourApiProject.observation.Observation;
import com.server.tourApiProject.observation.ObservationRepository;
import com.server.tourApiProject.search.Filter;
import com.server.tourApiProject.search.SearchParams1;
import com.server.tourApiProject.user.User;
import com.server.tourApiProject.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private PostHashTagRepository postHashTagRepository;

    @Mock
    private MyWishRepository myWishRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_mapsFieldsAndUsesObservationAreaWhenAreaIdIsZero() {
        Observation observation = observation(10L, "별빛 언덕", 55L);
        User user = user(1L, "tester");
        PostParams params = PostParams.builder()
                .postTitle("제목")
                .postContent("내용")
                .optionObservation("커스텀 관측지")
                .yearDate(LocalDate.of(2026, 4, 21))
                .time(LocalTime.of(22, 0))
                .writeDate(LocalDate.of(2026, 4, 21))
                .writeTime(LocalTime.of(22, 30))
                .userId(1L)
                .build();
        when(observationRepository.findByObservationName("별빛 언덕")).thenReturn(observation);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setPostId(100L);
            return post;
        });

        Long result = postService.createPost("별빛 언덕", params, 0L);

        assertEquals(100L, result);
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertEquals("제목", captor.getValue().getPostTitle());
        assertEquals(55L, captor.getValue().getAreaCode());
        assertEquals(0L, captor.getValue().getLiked());
        assertEquals(0L, captor.getValue().getSaved());
    }

    @Test
    void createPost_usesProvidedAreaIdWhenNonZero() {
        Observation observation = observation(10L, "별빛 언덕", 55L);
        User user = user(1L, "tester");
        PostParams params = PostParams.builder()
                .postTitle("제목")
                .postContent("내용")
                .userId(1L)
                .build();
        when(observationRepository.findByObservationName("별빛 언덕")).thenReturn(observation);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        postService.createPost("별빛 언덕", params, 99L);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertEquals(99L, captor.getValue().getAreaCode());
    }

    @Test
    void getMyPost3_returnsLatestThreePostsWithOptionalThumbnail() {
        Post first = post(1L, 1L, 10L, "첫 글");
        Post second = post(2L, 1L, 10L, "둘 글");
        Post third = post(3L, 1L, 10L, "셋 글");
        Post fourth = post(4L, 1L, 10L, "넷 글");
        when(postRepository.findByUserId(1L)).thenReturn(List.of(first, second, third, fourth));
        when(postImageRepository.findByPostId(4L)).thenReturn(List.of(PostImage.builder().postId(4L).imageName("four.png").build()));
        when(postImageRepository.findByPostId(3L)).thenReturn(List.of());
        when(postImageRepository.findByPostId(2L)).thenReturn(List.of(PostImage.builder().postId(2L).imageName("two.png").build()));

        List<PostParams2> result = postService.getMyPost3(1L);

        assertEquals(3, result.size());
        assertEquals("넷 글", result.get(0).getTitle());
        assertEquals("four.png", result.get(0).getThumbnail());
        assertEquals("셋 글", result.get(1).getTitle());
        assertNull(result.get(1).getThumbnail());
        assertEquals("둘 글", result.get(2).getTitle());
    }

    @Test
    void deletePost_deletesPostAndAssociatedWishEntries() {
        postService.deletePost(10L);

        verify(postRepository).deleteById(10L);
        verify(myWishRepository).deleteByItemIdAndWishType(10L, 2);
    }

    @Test
    void getMyPost_mapsAuthorThumbnailAndUpToThreeHashTags() {
        Post post = post(10L, 1L, 20L, "내 게시글");
        User user = user(1L, "writer");
        user.setProfileImage("profile.png");
        when(postRepository.findByUserId(1L)).thenReturn(List.of(post));
        when(postImageRepository.findByPostId(10L)).thenReturn(List.of(PostImage.builder().postId(10L).imageName("thumb.png").build()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postHashTagRepository.findByPostId(10L)).thenReturn(List.of(
                PostHashTag.builder().postId(10L).hashTagName("야경").build(),
                PostHashTag.builder().postId(10L).hashTagName("가족").build(),
                PostHashTag.builder().postId(10L).hashTagName("데이트").build(),
                PostHashTag.builder().postId(10L).hashTagName("추가").build()
        ));

        List<PostParams3> result = postService.getMyPost(1L);

        assertEquals(1, result.size());
        assertEquals("writer", result.get(0).getNickName());
        assertEquals("thumb.png", result.get(0).getThumbnail());
        assertEquals(List.of("야경", "가족", "데이트"), result.get(0).getHashTagNames());
    }

    @Test
    void getRelatePost_mapsOptionalUserAndAllHashTags() {
        Post post = post(10L, 2L, 30L, "관련 게시글");
        User user = user(2L, "author");
        user.setProfileImage("author.png");
        when(postRepository.findByObservationId(30L)).thenReturn(List.of(post));
        when(postImageRepository.findByPostId(10L)).thenReturn(List.of());
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(postHashTagRepository.findByPostId(10L)).thenReturn(List.of(
                PostHashTag.builder().postId(10L).hashTagName("태그1").build(),
                PostHashTag.builder().postId(10L).hashTagName("태그2").build()
        ));

        List<PostParams5> result = postService.getRelatePost(30L);

        assertEquals(1, result.size());
        assertEquals("author", result.get(0).getNickName());
        assertNull(result.get(0).getThumbnail());
        assertEquals(List.of("태그1", "태그2"), result.get(0).getHashTagNames());
    }

    @Test
    void getMainPost_mapsImagesHashTagsAndOptionHashTags() throws UnsupportedEncodingException {
        Post post = post(10L, 2L, 30L, "메인 게시글");
        post.setOptionObservation("커스텀 관측지");
        post.setOptionHashTag("옵션1");
        post.setOptionHashTag2("옵션2");
        Observation observation = observation(30L, "별빛 언덕", 55L);
        User user = user(2L, "author");
        user.setProfileImage("author.png");
        post.setObservation(observation);
        when(postRepository.findAll(any(Sort.class))).thenReturn(List.of(post));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(postImageRepository.findByPostId(10L)).thenReturn(List.of(
                PostImage.builder().postId(10L).imageName("main image.png").build()
        ));
        when(postHashTagRepository.findByPostId(10L)).thenReturn(List.of(
                PostHashTag.builder().postId(10L).hashTagName("야경").build()
        ));

        List<PostParams4> result = postService.getMainPost();

        assertEquals(1, result.size());
        assertEquals("author", result.get(0).getMainNickName());
        assertTrue(result.get(0).getImages().get(0).contains("main+image.png"));
        assertEquals(List.of("야경"), result.get(0).getHashTags());
        assertEquals("옵션1", result.get(0).getOptionHashTag());
        assertEquals("옵션2", result.get(0).getOptionHashTag2());
        assertEquals("별빛 언덕", result.get(0).getMainObservation());
    }

    @Test
    void getPostDataWithFilter_filtersByAreaHashTagAndSearchKey() {
        Filter filter = Filter.builder()
                .areaCodeList(List.of(7L))
                .hashTagIdList(List.of(100L))
                .build();
        Post post = post(10L, 2L, 30L, "은하수 게시글");
        post.setAreaCode(7L);
        post.setSaved(5L);
        when(postHashTagRepository.findByHashTagId(100L)).thenReturn(List.of(
                PostHashTag.builder().postId(10L).hashTagId(100L).hashTagName("야경").build()
        ));
        when(postRepository.findByAreaCode(7L)).thenReturn(List.of(post));
        when(postRepository.findByPostTitleContainingOrPostContentContaining("은하수", "은하수")).thenReturn(new ArrayList<>(List.of(post)));
        when(postHashTagRepository.findByPostId(10L)).thenReturn(List.of(
                PostHashTag.builder().postId(10L).hashTagName("야경").build(),
                PostHashTag.builder().postId(10L).hashTagName("가족").build(),
                PostHashTag.builder().postId(10L).hashTagName("데이트").build(),
                PostHashTag.builder().postId(10L).hashTagName("추가").build()
        ));
        when(postImageRepository.findByPostId(10L)).thenReturn(List.of(
                PostImage.builder().postId(10L).imageName("thumb.png").build()
        ));

        List<SearchParams1> result = postService.getPostDataWithFilter(filter, "은하수");

        assertEquals(1, result.size());
        assertEquals(List.of("야경", "가족", "데이트"), result.get(0).getHashTagNames());
        assertEquals("thumb.png", result.get(0).getThumbnail());
        assertEquals(5L, result.get(0).getSaved());
    }

    private Post post(Long postId, Long userId, Long observationId, String title) {
        Post post = new Post();
        post.setPostId(postId);
        post.setUserId(userId);
        post.setObservationId(observationId);
        post.setPostTitle(title);
        post.setSaved(0L);
        post.setLiked(0L);
        return post;
    }

    private User user(Long userId, String nickName) {
        User user = new User();
        user.setUserId(userId);
        user.setNickName(nickName);
        user.setMyPosts(new ArrayList<>());
        return user;
    }

    private Observation observation(Long observationId, String name, Long areaCode) {
        Observation observation = new Observation();
        observation.setObservationId(observationId);
        observation.setObservationName(name);
        observation.setAreaCode(areaCode);
        observation.setAddress("서울");
        observation.setLatitude(37.0);
        observation.setLongitude(127.0);
        return observation;
    }
}
