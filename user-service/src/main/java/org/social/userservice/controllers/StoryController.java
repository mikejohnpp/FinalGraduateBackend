package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.story.request.StoryRequest;
import org.social.common.dto.story.views.StoryDTO;
import org.social.userservice.services.StoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/story")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;


    @GetMapping("/reel")
    public ResponseEntity<ApiResponse<List<StoryDTO>>> getAll(){
        List<StoryDTO> result = storyService.getAllReel();
        return ApiResponse.ok("Lấy story thành công",result);
    }

    @GetMapping("/friends")
    ResponseEntity<ApiResponse<List<StoryDTO>>> getAllWithFriends(@RequestParam Integer userId) {
        List<StoryDTO> result = storyService.getAllWithFriends(userId);
        return ApiResponse.ok("Lấy story thành công", result);
    }

    @PostMapping
    ResponseEntity<ApiResponse<StoryDTO>> createStory(@RequestBody StoryRequest storyRequest){
        StoryDTO rs = storyService.create(storyRequest);
        return ApiResponse.ok("Tạo story thành công", rs);
    }




}
