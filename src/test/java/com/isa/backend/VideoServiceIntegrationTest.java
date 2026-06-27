package com.isa.backend;

import com.isa.backend.model.User;
import com.isa.backend.model.VideoPost;
import com.isa.backend.repository.VideoDislikeRepository;
import com.isa.backend.repository.VideoLikeRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.service.impl.VideoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VideoServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private VideoDislikeRepository videoDislikeRepository;

    @Autowired
    private VideoServiceImpl videoService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;
    @BeforeEach
    void setUp() {
        videoLikeRepository.deleteAll();
        videoDislikeRepository.deleteAll();
        videoPostRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void test_get_video_success_integration() {
        User user = new User();
        user.setUsername("videoowner");
        user.setPassword("password");
        user = userRepository.save(user);

        VideoPost mockVideo = new VideoPost();
        mockVideo.setTitle("Test Video");
        mockVideo.setUser(user);
        VideoPost savedVideo = videoPostRepository.save(mockVideo);

        VideoPost result = videoService.getVideoById(savedVideo.getId());

        assertNotNull(result);
        assertEquals("Test Video", result.getTitle());
    }

    @Test
    void test_get_video_not_found_exception_integration() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            videoService.getVideoById(99L);
        });

        assertEquals("Video not found with id: 99", exception.getMessage());
    }

    @Test
    void test_update_like_delete_if_exists_integration() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("passwordd");
        user = userRepository.save(user);

        VideoPost video = new VideoPost();
        video.setTitle("Test Video");
        video.setUser(user);
        video = videoPostRepository.save(video);

        com.isa.backend.model.VideoLike videoLike = new com.isa.backend.model.VideoLike();
        videoLike.setUser(user);
        videoLike.setVideo(video);
        videoLikeRepository.save(videoLike);

        videoService.toggleLike(video.getId(), "testuser");

        Optional<com.isa.backend.model.VideoLike> result = videoLikeRepository.findByUserIdAndVideoId(user.getId(), video.getId());
        assertThat(result).isEmpty();
    }
}