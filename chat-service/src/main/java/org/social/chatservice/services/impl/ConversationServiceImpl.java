package org.social.chatservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.chatservice.services.ConversationService;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.conversation.mappers.ConversationResponseMapper;
import org.social.common.dto.conversation.mappers.MessageResponseMapper;
import org.social.common.dto.conversation.mappers.UserResponseMapper;
import org.social.common.dto.conversation.response.ConversationResponse;
import org.social.common.dto.conversation.response.ConversationResponseDetail;
import org.social.common.dto.conversation.response.MessageResponse;
import org.social.common.dto.conversation.response.UserResponse;
import org.social.common.entities.*;
import org.social.common.exceptions.BusinessException;
import org.social.common.repositories.ConversationRepository;
import org.social.common.repositories.ConversationUserRepository;
import org.social.common.repositories.MessageRepository;
import org.social.common.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
        private final UserRepository userRepository;
        private final ConversationRepository conversationRepository;
        private final MessageRepository messageRepository;
        private final ConversationUserRepository conversationUserRepository;
        private final ConversationResponseMapper conversationResponseMapper;
        private final MessageResponseMapper messageResponseMapper;
        private final UserResponseMapper userResponseMapper;

        @Override
        public Set<ConversationResponse> getAllConversations(int userId) {
                User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(
                                () -> new BusinessException("Không tìm thấy người"));

                Set<Conversation> conversations = user.getConversation();
                return conversations.stream()
                                .map(conversationResponseMapper::toDTO)
                                .collect(Collectors.toSet());
        }

        @Override
        public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(int userOppenentId,
                        int userCurrentId) {
                User user1 = userRepository.findById(Long.valueOf(userOppenentId))
                                .orElseThrow(() -> new BusinessException("Không tìm thấy user: " + userOppenentId));

                User user2 = userRepository.findById(Long.valueOf(userCurrentId))
                                .orElseThrow(() -> new BusinessException("Không tìm thấy user: " + userCurrentId));

                if (user1.getId() == user2.getId()) {
                        throw new BusinessException("Không thể tự chat với chính mình");
                }

                Optional<Conversation> existingConversation = conversationRepository.findPrivateConversation(user1,
                                user2);

                if (existingConversation.isPresent()) {
                        return ApiResponse.ok(
                                        "Đã tồn tại cuộc trò chuyện",
                                        conversationResponseMapper.toDTO(existingConversation.get()));
                }

                Conversation conversation = new Conversation();

                Set<User> members = conversation.getUser();
                members.add(user1);
                members.add(user2);

                conversation.setUser(members);
                conversation.setIsGroup(false);
                conversation.setIsActive(true);
                conversation.setCreatedAt(Instant.now());
                Conversation savedConversation = conversationRepository.save(conversation);
                ConversationUser conversationUser1 = new ConversationUser();
                ConversationUserId conversationUserId1 = new ConversationUserId();
                conversationUserId1.setConversationId(savedConversation.getId());
                conversationUserId1.setUserId(user1.getId());
                conversationUser1.setId(conversationUserId1);
                conversationUser1.setConversation(savedConversation);
                conversationUser1.setUser(user1);

                ConversationUser conversationUser2 = new ConversationUser();
                ConversationUserId conversationUserId2 = new ConversationUserId();
                conversationUserId2.setConversationId(savedConversation.getId());
                conversationUserId2.setUserId(user2.getId());
                conversationUser2.setId(conversationUserId2);
                conversationUser2.setConversation(savedConversation);
                conversationUser2.setUser(user2);

                conversationUserRepository.save(conversationUser1);
                conversationUserRepository.save(conversationUser2);
                return ApiResponse.ok(
                                "Tạo cuộc trò chuyện thành công",
                                conversationResponseMapper.toDTO(savedConversation));

        }

        @Override
        public ResponseEntity<ApiResponse<ConversationResponse>> createGroupConversation(
                        org.social.common.dto.conversation.requests.CreateConversationGroupRequest request) {
                Set<Integer> memberIds = new java.util.HashSet<>(request.getMemberIds());
                memberIds.add(request.getUserCurrentId());

                if (memberIds.size() < 2) {
                        throw new BusinessException("Nhóm phải có ít nhất 2 thành viên");
                }

                Set<User> members = memberIds.stream()
                                .map(id -> userRepository.findById(Long.valueOf(id))
                                                .orElseThrow(() -> new BusinessException("Không tìm thấy user: " + id)))
                                .collect(Collectors.toSet());

                Conversation conversation = new Conversation();
                conversation.setName(request.getName());
                conversation.setIsGroup(true);
                conversation.setIsActive(true);
                conversation.setCreatedAt(Instant.now());
                conversation.setUser(members);

                Conversation savedConversation = conversationRepository.save(conversation);

                for (User user : members) {
                        ConversationUser cu = new ConversationUser();
                        ConversationUserId cuId = new ConversationUserId();
                        cuId.setConversationId(savedConversation.getId());
                        cuId.setUserId(user.getId());
                        cu.setId(cuId);
                        cu.setConversation(savedConversation);
                        cu.setUser(user);
                        conversationUserRepository.save(cu);
                }

                return ApiResponse.ok(
                                "Tạo nhóm trò chuyện thành công",
                                conversationResponseMapper.toDTO(savedConversation));
        }

        @Override
        public ResponseEntity<ApiResponse<ConversationResponse>> addMembersToGroup(int conversationId, org.social.common.dto.conversation.requests.AddMemberRequest request) {
                Conversation conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new BusinessException("Không tìm thấy conversation"));
                
                if (!conversation.getIsGroup()) {
                        throw new BusinessException("Đây không phải là nhóm trò chuyện");
                }

                Set<Integer> currentMemberIds = conversation.getUser().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());

                Set<User> newMembers = request.getMemberIds().stream()
                        .filter(id -> !currentMemberIds.contains(id))
                        .map(id -> userRepository.findById(Long.valueOf(id))
                                .orElseThrow(() -> new BusinessException("Không tìm thấy user: " + id)))
                        .collect(Collectors.toSet());

                if (newMembers.isEmpty()) {
                        return ApiResponse.ok("Thành viên đã có trong nhóm", conversationResponseMapper.toDTO(conversation));
                }

                Set<User> updatedMembers = conversation.getUser();
                updatedMembers.addAll(newMembers);
                conversation.setUser(updatedMembers);

                Conversation savedConversation = conversationRepository.save(conversation);

                for(User user : newMembers) {
                        ConversationUser cu = new ConversationUser();
                        ConversationUserId cuId = new ConversationUserId();
                        cuId.setConversationId(savedConversation.getId());
                        cuId.setUserId(user.getId());
                        cu.setId(cuId);
                        cu.setConversation(savedConversation);
                        cu.setUser(user);
                        conversationUserRepository.save(cu);
                }

                return ApiResponse.ok(
                        "Thêm thành viên vào nhóm thành công",
                        conversationResponseMapper.toDTO(savedConversation));
        }

        @Override
        public ConversationResponseDetail getConversationDetail(int conversationId, int page, int size) {
                Conversation conversation = conversationRepository.findByIdAndIsActiveTrue(conversationId)
                                .orElseThrow(() -> new BusinessException(
                                                "Không tìm thấy conversation"));

                Sort sort = Sort.by("createdAt").descending();
                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                sort);
                Page<Message> messagePage = messageRepository.findByConversationIdAndIsActiveTrue(
                                conversationId,
                                pageable);

                Set<MessageResponse> messages = messagePage.getContent()
                                .stream()
                                .map(messageResponseMapper::toDTO)
                                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
                Set<UserResponse> members = conversation.getUser()
                                .stream()
                                .map(userResponseMapper::toDTO)
                                .collect(Collectors.toSet());
                return new ConversationResponseDetail(
                                conversation.getId(),
                                conversation.getName(),
                                conversation.getIsGroup(),
                                conversation.getCreatedAt(),
                                members,
                                messages,
                                messagePage.getNumber(),
                                messagePage.getTotalPages(),
                                messagePage.getTotalElements());
        }

        @Override
        public ConversationResponseDetail getConversationDetailImageAndFile(int conversationId) {
                Conversation conversation = conversationRepository.findByIdAndIsActiveTrue(conversationId)
                        .orElseThrow(() -> new BusinessException(
                                "Không tìm thấy conversation"));

                List<Message> messageList = messageRepository.findByConversationIdAndIsActiveTrueAndMessageTypeIn(conversation.getId(),List.of(MessageType.FILE,MessageType.IMAGE));
                Set<MessageResponse> messages = messageList
                        .stream()
                        .map(messageResponseMapper::toDTO)
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
                Set<UserResponse> members = conversation.getUser()
                        .stream()
                        .map(userResponseMapper::toDTO)
                        .collect(Collectors.toSet());
                ConversationResponseDetail response = new ConversationResponseDetail();
                response.setConversationId(conversation.getId());
                response.setMembers(members);
                response.setMessages(messages);
                return response;
        }


}
