package com.isa.backend.controller;

import com.isa.backend.dto.StreamingStatusDto;
import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.VideoPost;
import com.isa.backend.service.PopularVideosService;
import com.isa.backend.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(value = "/api/videos", produces = MediaType.APPLICATION_JSON_VALUE)
public class VideoPostController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private PopularVideosService popularVideosService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadVideo(@RequestPart("data") VideoPostUploadDto dto, @RequestPart("video") MultipartFile videoFile, @RequestPart("thumbnail") MultipartFile thumbnailFile, Principal principal){
        try{
            VideoPost createdPost = videoService.createVideoPost(dto, videoFile, thumbnailFile, principal.getName());
            return ResponseEntity.ok(createdPost);
        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Video upload failed: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<?> getThumbnail(@PathVariable Long id){
        try{
            byte[] image = videoService.getThumbnail(id);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<VideoPost>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<VideoPost> getVideoById(@PathVariable Long id) {
        VideoPost video = videoService.getVideoById(id);
        if (video != null) {
            return ResponseEntity.ok(video);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/{id}/stream")
    public ResponseEntity<ResourceRegion> streamVideo(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            ResourceRegion region = videoService.getVideoStream(id, headers);
            if (region.getCount() == 0) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
            }
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(region);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<?> incrementView(@PathVariable Long id) {
        videoService.recordView(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/details")
    public ResponseEntity<?> getVideoDetails(@PathVariable Long id, Principal principal){
        try {
            String username = (principal != null) ? principal.getName() : null;
            return ResponseEntity.ok(videoService.getVideoDetails(id, username));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, Principal principal) {
        try {
            videoService.toggleLike(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Like action failed: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/dislike")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> toggleDislike(@PathVariable Long id, Principal principal) {
        try {
            videoService.toggleDislike(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Dislike action failed: " + e.getMessage());
        }
    }

    @GetMapping("/trending/local")
    public ResponseEntity<List<VideoPost>> resolveUserLocation(@RequestParam(required = false) Double lat, @RequestParam(required = false) Double lon, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return ResponseEntity.ok(videoService.resolveUserLocation(lat, lon, ip));
    }

    @GetMapping("/{id}/steaming-status")
    public ResponseEntity<StreamingStatusDto> getSteamingStatus(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getStreamingStatus(id));
    }
}
