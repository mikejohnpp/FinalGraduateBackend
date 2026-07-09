package org.social.common.dto.post.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.social.common.dto.media.MediaRequest;
import org.social.common.validation.MaxWords;

import java.util.List;

public record PostUpdateRequest(
        @NotBlank(message = "Nội dung bài viết không được để trống") @MaxWords(value = 40, message = "Nội dung bài viết không được vượt quá 40 từ") String content,
        @Valid List<MediaRequest> media) {
}
