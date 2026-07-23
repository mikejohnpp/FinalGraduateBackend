package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.conversation.mappers.UserResponseMapper;
import org.social.common.dto.story.request.StoryRequest;
import org.social.common.dto.story.views.StoryDTO;
import org.social.common.entities.Story;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
import org.social.common.repositories.StoryRepository;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.StoryService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;


    @Override
    public List<StoryDTO> getAllReel() {
        List<Story> storys = storyRepository.findAllWithUser();
        List<StoryDTO> rs = storys.stream().map(st -> new StoryDTO(st.getId(),st.getContent(),st.getUrlImage(),st.getUrlVideo(),userResponseMapper.toDTO(st.getUser()),st.getCreatedAt(),st.getIsActive(),st.getType(),st.getColor())).toList();
        return rs;
    }

    @Override
    public StoryDTO create(StoryRequest storyRequest) {

        User user = userRepository.findById(Long.valueOf(storyRequest.getUserId())).orElseThrow(() -> new BusinessException("Không tồn tại user"));

        Story st = new Story();
        st.setContent(storyRequest.getContent());
        st.setUrlImage(storyRequest.getUrlImage());
        st.setUrlVideo(storyRequest.getUrlVideo());
        st.setType(storyRequest.getType());
        st.setCreatedAt(Instant.now());
        st.setUser(user);
        st.setColor(storyRequest.getColor());
        st.setIsActive(true);

        Story st1 = storyRepository.save(st);

        StoryDTO rs = new StoryDTO(st1.getId(),st1.getContent(),st1.getUrlImage(),st1.getUrlVideo(),userResponseMapper.toDTO(st1.getUser()),st1.getCreatedAt(),st1.getIsActive(),st1.getType(),st1.getColor());
        return rs;
    }

    @Override
    public List<StoryDTO> getAllWithFriends(Integer userId) {
        List<Story> storys = storyRepository.findAllStoryFeed(userId);
        List<StoryDTO> rs = storys.stream().map(st -> new StoryDTO(st.getId(),st.getContent(),st.getUrlImage(),st.getUrlVideo(),userResponseMapper.toDTO(st.getUser()),st.getCreatedAt(),st.getIsActive(),st.getType(),st.getColor())).toList();
        return rs;
    }


}
