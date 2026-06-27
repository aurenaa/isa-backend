package com.isa.backend;

import com.isa.backend.dto.CommentRequestDto;
import com.isa.backend.model.Comment;
import com.isa.backend.model.User;
import com.isa.backend.repository.CommentRepository;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceUnitTest {

    @Mock
    private CommentRepository mock_comment_db;

    @Mock
    private UserRepository mock_user_db;

    @Mock
    private VideoPostRepository mock_video_db;

    @InjectMocks
    private CommentServiceImpl commentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_create_comment_rate_limit_exception() {
        User mockUser = new User();
        mockUser.setId(1L);

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Comment");

        when(mock_user_db.findByUsername("testuser")).thenReturn(mockUser);
        when(mock_comment_db.countByUserIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(60L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentService.addComment(10L, request, "testuser");
        });

        assertEquals("Rate limit exceeded: You can only post 60 comments per hour.", exception.getMessage());
        verify(mock_comment_db, never()).save(any(Comment.class));
    }

    @Test
    public void test_create_comment_video_not_found_exception() {
        User mockUser = new User();
        mockUser.setId(1L);

        CommentRequestDto request = new CommentRequestDto();
        request.setText("New comment");

        when(mock_user_db.findByUsername("testuser")).thenReturn(mockUser);
        when(mock_comment_db.countByUserIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(0L);
        when(mock_video_db.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentService.addComment(99L, request, "testuser");
        });

        assertEquals("Video not found!", exception.getMessage());
        verify(mock_comment_db, never()).save(any(Comment.class));
    }
}