package org.social.userservice.services;

import org.social.common.dto.story.request.StoryRequest;
import org.social.common.dto.story.views.StoryDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface StoryService {

    List<StoryDTO> getAllReel();

    StoryDTO create(StoryRequest storyRequest);

    List<StoryDTO> getAllWithFriends(Integer userId);



}
