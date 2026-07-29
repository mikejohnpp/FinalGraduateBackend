package org.social.common.dto.comment.mappers;

import org.social.common.dto.comment.views.CommentDTO;
import org.social.common.dto.media.MediaMapper;
import org.social.common.dto.user.views.AuthorDTO;
import org.social.common.entities.Comment;
import org.social.common.entities.User;

public class CommentMapper {

    private static AuthorDTO toAuthorDTO(User user) {
        if (user == null)
            return null;
        return new AuthorDTO(
                user.getId(),
                user.getUserName(),
                user.getAvatar(),
                user.getNickName());
    }

    public static CommentDTO toCommentDTO(Comment comment, boolean liked) {
        Integer parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        return new CommentDTO(
                comment.getId(),
                toAuthorDTO(comment.getUser()),
                comment.getPost().getId(),
                parentId,
                comment.getContent(),
                comment.getLikeCount() != null ? comment.getLikeCount() : 0,
                comment.getReplyCount() != null ? comment.getReplyCount() : 0,
                liked,
                comment.getCreatedAt(),
                comment.getSentiment(),
                comment.getConfidence(),
                comment.getCancelReason(),
                MediaMapper.toCommentMediaDTOs(comment.getMedia()));
    }
}
