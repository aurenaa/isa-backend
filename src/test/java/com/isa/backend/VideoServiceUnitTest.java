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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoServiceUnitTest {

    @Mock
    private VideoPostRepository mock_video_db;

    @Mock
    private UserRepository mock_user_db;

    @Mock
    private VideoLikeRepository videoLikeRepository;

    @Mock
    private VideoDislikeRepository videoDislikeRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private VideoServiceImpl videoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_get_video_success() {
        VideoPost mockVideo = new VideoPost();
        mockVideo.setId(10L);
        mockVideo.setTitle("Test Video");

        when(mock_video_db.findById(10L)).thenReturn(Optional.of(mockVideo));

        VideoPost result = videoService.getVideoById(10L);

        assertNotNull(result);
        assertEquals("Test Video", result.getTitle());
    }

    @Test
    public void test_get_video_not_found_exception() {
        when(mock_video_db.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            videoService.getVideoById(99L);
        });

        assertEquals("Video not found with id: 99", exception.getMessage());
    }

    @Test
    public void test_update_like_delete_if_exists() {
        User mockUser = new User();
        mockUser.setId(1L);
        VideoPost mockVideo = new VideoPost();
        mockVideo.setId(10L);

        when(mock_user_db.findByUsername("testuser")).thenReturn(mockUser);
        when(mock_video_db.findById(10L)).thenReturn(Optional.of(mockVideo));

        when(videoDislikeRepository.findByUserIdAndVideoId(1L, 10L)).thenReturn(Optional.empty());
        when(videoLikeRepository.findByUserIdAndVideoId(1L, 10L)).thenReturn(Optional.of(mock(com.isa.backend.model.VideoLike.class)));

        videoService.toggleLike(10L, "testuser");

        verify(videoLikeRepository, times(1)).delete(any());
        verify(videoLikeRepository, never()).save(any());
    }
}