package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.entities.Comment;
import org.social.common.entities.Post;
import org.social.common.repositories.CommentRepository;
import org.social.common.repositories.PostRepository;
import org.social.userservice.services.AdminContentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void lockPosts(List<Integer> postIds) {
        if (postIds == null || postIds.isEmpty()) return;
        List<Post> posts = postRepository.findAllById(postIds);
        for (Post post : posts) {
            post.setIsActive(false);
            if (post.getGroup() != null && "PENDING".equals(post.getStatus())) {
                post.setStatus("REJECTED");
            }
        }
        postRepository.saveAll(posts);
    }

    @Override
    @Transactional
    public void unlockPosts(List<Integer> postIds) {
        if (postIds == null || postIds.isEmpty()) return;
        List<Post> posts = postRepository.findAllById(postIds);
        for (Post post : posts) {
            post.setIsActive(true);
        }
        postRepository.saveAll(posts);
    }

    @Override
    @Transactional
    public void lockComments(List<Integer> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) return;
        List<Comment> comments = commentRepository.findAllById(commentIds);
        for (Comment comment : comments) {
            if (Boolean.TRUE.equals(comment.getIsActive())) {
                comment.setIsActive(false);
                commentRepository.save(comment);

                int commentsRemoved = 1;
                if (comment.getParent() == null) {
                    List<Comment> replies = commentRepository.findAllByParentIdAndIsActiveTrue(comment.getId());
                    for (Comment reply : replies) {
                        reply.setIsActive(false);
                        commentRepository.save(reply);
                        commentsRemoved++;
                    }
                } else {
                    Comment parent = comment.getParent();
                    parent.setReplyCount(Math.max(0, parent.getReplyCount() - 1));
                    commentRepository.save(parent);
                }

                Post post = comment.getPost();
                post.setCommentCount(Math.max(0, post.getCommentCount() - commentsRemoved));
                postRepository.save(post);
            }
        }
    }

    @Override
    @Transactional
    public void unlockComments(List<Integer> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) return;
        List<Comment> comments = commentRepository.findAllById(commentIds);
        for (Comment comment : comments) {
            if (!Boolean.TRUE.equals(comment.getIsActive())) {
                comment.setIsActive(true);
                commentRepository.save(comment);

                if (comment.getParent() != null) {
                    Comment parent = comment.getParent();
                    parent.setReplyCount(parent.getReplyCount() + 1);
                    commentRepository.save(parent);
                }

                Post post = comment.getPost();
                post.setCommentCount(post.getCommentCount() + 1);
                postRepository.save(post);
            }
        }
    }
}
