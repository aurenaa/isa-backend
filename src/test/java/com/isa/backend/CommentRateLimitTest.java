package com.isa.backend;

import com.isa.backend.dto.CommentRequestDto;
import com.isa.backend.model.Location;
import com.isa.backend.model.User;
import com.isa.backend.model.VideoPost;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.repository.LocationRepository;
import com.isa.backend.repository.CommentRepository;
import com.isa.backend.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
@Disabled
@SpringBootTest
public class CommentRateLimitTest {
    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private LocationRepository locationRepository;

    private User testUser;
    private VideoPost testVideo;

    @BeforeEach
    public void setup() {
        commentRepository.deleteAll();
        testUser = new User();
        testUser.setUsername("commenter_" + System.currentTimeMillis());
        testUser.setEmail("commenter@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        Location location = new Location();
        location.setDisplayName("Test City");
        location = locationRepository.save(location);

        testVideo = new VideoPost();
        testVideo.setTitle("Rate Limit Test Video");
        testVideo.setUser(testUser);
        testVideo.setVideoPath("test.mp4");
        testVideo.setLocation(location);
        testVideo = videoPostRepository.save(testVideo);
    }

    @Test
    public void testRateLimitAfter60Comments() {
        Long videoId = testVideo.getId();
        String username = testUser.getUsername();
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Automated test comment");

        System.out.println("Sending 60 comments...");
        for (int i = 0; i < 60; i++) {
            commentService.addComment(videoId, dto, username);
        }

        System.out.println("Attempting to send the 61st comment...");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            commentService.addComment(videoId, dto, username);
        });

        String expectedMessage = "Rate limit exceeded";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage),
                "Expected rate limit error message, but got: " + actualMessage);

        System.out.println("Test passed! Server correctly rejected the 61st comment.");
    }
}
