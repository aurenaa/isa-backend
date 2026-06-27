package com.isa.backend;

import com.isa.backend.model.User;
import com.isa.backend.model.Location;
import com.isa.backend.model.VideoPost;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.repository.LocationRepository;
import com.isa.backend.service.VideoService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Disabled
@SpringBootTest
public class VideoViewsSimultaneousTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    public void testMultipleUsersAtSameTime() throws InterruptedException {
        User testUser = new User();
        testUser.setUsername("test_user_" + System.currentTimeMillis());
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("Test");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        Location testLocation = new Location();
        testLocation.setDisplayName("Test Location");
        testLocation.setLatitude(44.7866);
        testLocation.setLongitude(20.4489);
        testLocation = locationRepository.save(testLocation);

        VideoPost video = new VideoPost();
        video.setTitle("Test Video");
        video.setDescription("Test Description");
        video.setTags(Arrays.asList("test", "tags"));
        video.setUser(testUser);
        video.setViews(0L);
        video.setVideoPath("path/to/video.mp4");
        video.setThumbnailPath("path/to/thumbnail.jpg");
        video.setLocation(testLocation);
        video = videoPostRepository.save(video);
        Long videoId = video.getId();

        int userNum = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(userNum);

        for (int i = 0; i < userNum; i++){
            executorService.execute(() -> {
                try {
                    videoService.recordView(videoId);
                } catch (Exception e) {
                    System.err.println("Error recording view: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        VideoPost finalVideo = videoPostRepository.findById(videoId).get();
        assertEquals(100L, finalVideo.getViews(), "Counter must be exactly 100");
    }
}
