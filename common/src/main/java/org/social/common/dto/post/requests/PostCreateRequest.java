package org.social.common.dto.post.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.social.common.dto.media.MediaRequest;

import java.util.List;

public record PostCreateRequest(
                @NotNull(message = "ID người dùng không được để trống") Integer userId,
                Boolean isGroupPosted,
                Integer groupId,
                @NotBlank(message = "Nội dung bài viết không được để trống") @Size(max = 40, message = "Nội dung bài viết không được vượt quá 40 ký tự") String content,
                @Valid List<MediaRequest> media) {
}
