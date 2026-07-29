package org.social.common.dto.media;

import org.social.common.entities.Comment;
import org.social.common.entities.CommentMedia;
import org.social.common.entities.MediaType;
import org.social.common.entities.Post;
import org.social.common.entities.PostMedia;

import java.util.ArrayList;
import java.util.List;

public class MediaMapper {

    private static MediaType parseType(String type) {
        if (type == null || type.isBlank())
            return MediaType.IMAGE;
        try {
            return MediaType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return MediaType.IMAGE;
        }
    }

    public static List<PostMedia> toPostMediaEntities(List<MediaRequest> requests, Post post) {
        List<PostMedia> result = new ArrayList<>();
        if (requests == null)
            return result;
        int index = 0;
        for (MediaRequest req : requests) {
            PostMedia media = new PostMedia();
            media.setPost(post);
            media.setUrl(req.url());
            media.setMediaType(parseType(req.mediaType()));
            media.setPosition(req.position() != null ? req.position() : index);
            result.add(media);
            index++;
        }
        return result;
    }

    public static List<CommentMedia> toCommentMediaEntities(List<MediaRequest> requests, Comment comment) {
        List<CommentMedia> result = new ArrayList<>();
        if (requests == null)
            return result;
        int index = 0;
        for (MediaRequest req : requests) {
            CommentMedia media = new CommentMedia();
            media.setComment(comment);
            media.setUrl(req.url());
            media.setMediaType(parseType(req.mediaType()));
            media.setPosition(req.position() != null ? req.position() : index);
            result.add(media);
            index++;
        }
        return result;
    }

    public static MediaDTO toDTO(PostMedia media) {
        if (media == null)
            return null;
        return new MediaDTO(
                media.getId(),
                media.getUrl(),
                media.getMediaType() != null ? media.getMediaType().name() : null,
                media.getPosition());
    }

    public static MediaDTO toDTO(CommentMedia media) {
        if (media == null)
            return null;
        return new MediaDTO(
                media.getId(),
                media.getUrl(),
                media.getMediaType() != null ? media.getMediaType().name() : null,
                media.getPosition());
    }

    public static List<MediaDTO> toPostMediaDTOs(List<PostMedia> media) {
        if (media == null)
            return List.of();
        return media.stream().map(MediaMapper::toDTO).toList();
    }

    public static List<MediaDTO> toCommentMediaDTOs(List<CommentMedia> media) {
        if (media == null)
            return List.of();
        return media.stream().map(MediaMapper::toDTO).toList();
    }
}
