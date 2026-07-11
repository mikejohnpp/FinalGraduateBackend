package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.friend.mappers.FriendMapper;
import org.social.common.dto.friend.views.FriendRequestDTO;
import org.social.common.dto.friend.views.FriendSuggestionDTO;
import org.social.common.dto.friend.views.FriendshipDTO;
import org.social.common.entities.FriendStatus;
import org.social.common.entities.User;
import org.social.common.entities.UserFriend;
import org.social.common.entities.UserFriendId;
import org.social.common.entities.NotificationType;
import org.social.common.events.FriendAcceptedEvent;
import org.social.common.events.NotificationEvent;
import org.social.userservice.messaging.publishers.FriendEventProducer;
import org.social.userservice.messaging.publishers.NotificationProducer;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.social.common.repositories.UserFriendRepository;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.FriendService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

        private final UserFriendRepository userFriendRepository;
        private final UserRepository userRepository;
        private final NotificationProducer notificationProducer;
        private final FriendEventProducer friendEventProducer;

        @Override
        public CursorPageResponse<FriendRequestDTO> getPendingRequests(Integer userId, String cursor, int size) {
                Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();

                List<UserFriend> requests = userFriendRepository.findPendingRequestsBefore(userId, cursorInstant,
                                PageRequest.of(0, size + 1));

                boolean hasMore = requests.size() > size;
                List<UserFriend> pageData = hasMore ? requests.subList(0, size) : requests;

                String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

                List<FriendRequestDTO> dtos = pageData.stream()
                                .map(uf -> FriendMapper.toFriendRequestDTO(uf,
                                                userFriendRepository.countMutualFriends(userId,
                                                                uf.getId().getUserId())))
                                .toList();

                return new CursorPageResponse<>(dtos, nextCursor, hasMore);
        }

        @Override
        @Transactional
        public void sendRequest(Integer userId, Integer targetUserId) {
                if (userId.equals(targetUserId)) {
                        throw new BusinessException(HttpStatus.BAD_REQUEST, "Không thể tự kết bạn với chính mình");
                }

                userRepository.findByIdAndIsActiveTrue(Long.valueOf(targetUserId))
                                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", targetUserId));

                if (userFriendRepository.existsByIdUserIdAndIdFriendIdAndStatus(userId, targetUserId,
                                FriendStatus.ACCEPTED)
                                || userFriendRepository.existsByIdUserIdAndIdFriendIdAndStatus(targetUserId, userId,
                                                FriendStatus.ACCEPTED)) {
                        throw new BusinessException(HttpStatus.CONFLICT, "Hai người đã là bạn bè");
                }

                if (userFriendRepository.existsByIdUserIdAndIdFriendIdAndStatus(userId, targetUserId,
                                FriendStatus.PENDING)) {
                        throw new BusinessException(HttpStatus.CONFLICT, "Lời mời kết bạn đã tồn tại");
                }

                if (userFriendRepository.existsByIdUserIdAndIdFriendIdAndStatus(targetUserId, userId,
                                FriendStatus.PENDING)) {
                        acceptRequest(targetUserId, userId);
                        return;
                }

                User sender = userRepository.findByIdAndIsActiveTrue(Long.valueOf(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));
                User receiver = userRepository.findByIdAndIsActiveTrue(Long.valueOf(targetUserId))
                                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", targetUserId));

                UserFriend request = new UserFriend();
                request.setId(new UserFriendId(userId, targetUserId));
                request.setUser(sender);
                request.setFriend(receiver);
                request.setStatus(FriendStatus.PENDING);
                request.setCreatedAt(Instant.now());
                userFriendRepository.save(request);

                // Thông báo FRIEND_REQUEST cho người nhận
                notificationProducer.publish(new NotificationEvent(
                                targetUserId,
                                userId,
                                NotificationType.FRIEND_REQUEST.name(),
                                "FRIEND",
                                userId,
                                null,
                                "/friends/requests"));
        }

        @Override
        @Transactional
        public void acceptRequest(Integer requestId, Integer userId) {
                UserFriend pending = userFriendRepository
                                .findByIdUserIdAndIdFriendIdAndStatus(requestId, userId, FriendStatus.PENDING)
                                .orElseThrow(() -> new ResourceNotFoundException("Lời mời kết bạn", requestId));

                Instant now = Instant.now();
                pending.setStatus(FriendStatus.ACCEPTED);
                pending.setUpdatedAt(now);
                userFriendRepository.save(pending);

                User currentUser = userRepository.findByIdAndIsActiveTrue(Long.valueOf(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));
                User requester = userRepository.findByIdAndIsActiveTrue(Long.valueOf(requestId))
                                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", requestId));

                UserFriend reverse = new UserFriend();
                reverse.setId(new UserFriendId(userId, requestId));
                reverse.setUser(currentUser);
                reverse.setFriend(requester);
                reverse.setStatus(FriendStatus.ACCEPTED);
                reverse.setCreatedAt(now);
                userFriendRepository.save(reverse);

                // Thông báo FRIEND_ACCEPT cho người đã gửi lời mời
                notificationProducer.publish(new NotificationEvent(
                                requestId,
                                userId,
                                NotificationType.FRIEND_ACCEPT.name(),
                                "FRIEND",
                                userId,
                                null,
                                "/profile/" + userId));

                // Phát sự kiện kết bạn thành công để chat-service tạo sẵn cuộc trò chuyện 1-1
                friendEventProducer.publishAccepted(new FriendAcceptedEvent(requestId, userId));
        }

        @Override
        @Transactional
        public void declineRequest(Integer requestId, Integer userId) {
                userFriendRepository.findByIdUserIdAndIdFriendIdAndStatus(requestId, userId, FriendStatus.PENDING)
                                .orElseThrow(() -> new ResourceNotFoundException("Lời mời kết bạn", requestId));

                userFriendRepository.deleteByUserIdAndFriendId(requestId, userId);
        }

        @Override
        public CursorPageResponse<FriendshipDTO> getFriends(Integer userId, String cursor, int size) {
                Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();

                List<UserFriend> friends = userFriendRepository.findAcceptedFriendsBefore(userId, cursorInstant,
                                PageRequest.of(0, size + 1));

                boolean hasMore = friends.size() > size;
                List<UserFriend> pageData = hasMore ? friends.subList(0, size) : friends;

                String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

                List<FriendshipDTO> dtos = pageData.stream()
                                .map(uf -> FriendMapper.toFriendshipDTO(uf,
                                                userFriendRepository.countMutualFriends(userId,
                                                                uf.getId().getFriendId())))
                                .toList();

                return new CursorPageResponse<>(dtos, nextCursor, hasMore);
        }

        @Override
        public CursorPageResponse<FriendSuggestionDTO> getSuggestions(Integer userId, String cursor, int size) {
                List<Integer> myFriendIds = userFriendRepository.findAcceptedFriendIds(userId);
                List<Integer> pendingIds = userFriendRepository
                                .findPendingRequestsBefore(userId, Instant.now(), PageRequest.of(0, Integer.MAX_VALUE))
                                .stream().map(uf -> uf.getId().getUserId()).toList();

                Integer cursorUserId = (cursor != null) ? Integer.parseInt(cursor) : Integer.MAX_VALUE;

                List<User> candidates = userRepository.findAll().stream()
                                .filter(u -> !u.getId().equals(userId))
                                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                                .filter(u -> !myFriendIds.contains(u.getId()))
                                .filter(u -> !pendingIds.contains(u.getId()))
                                .filter(u -> !userFriendRepository.existsByIdUserIdAndIdFriendIdAndStatus(u.getId(),
                                                userId,
                                                FriendStatus.PENDING))
                                .sorted((a, b) -> {
                                        int ma = userFriendRepository.countMutualFriends(userId, a.getId());
                                        int mb = userFriendRepository.countMutualFriends(userId, b.getId());
                                        if (mb != ma)
                                                return mb - ma;
                                        return a.getId().compareTo(b.getId());
                                })
                                .filter(u -> u.getId() < cursorUserId || cursor == null)
                                .limit(size + 1L)
                                .toList();

                boolean hasMore = candidates.size() > size;
                List<User> pageData = hasMore ? candidates.subList(0, size) : candidates;

                String nextCursor = pageData.isEmpty() ? null : String.valueOf(pageData.getLast().getId());

                List<FriendSuggestionDTO> dtos = pageData.stream()
                                .map(u -> FriendMapper.toFriendSuggestionDTO(u,
                                                userFriendRepository.countMutualFriends(userId, u.getId())))
                                .toList();

                return new CursorPageResponse<>(dtos, nextCursor, hasMore);
        }

        @Override
        @Transactional
        public void unfriend(Integer userId, Integer friendUserId) {
                userFriendRepository.deleteByUserIdAndFriendId(userId, friendUserId);
                userFriendRepository.deleteByUserIdAndFriendId(friendUserId, userId);
        }

        @Override
        public int getPendingRequestCount(Integer userId) {
                return userFriendRepository.countPendingRequests(userId);
        }
}
