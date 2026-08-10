package org.social.userservice.services;

import java.util.List;

public interface AdminContentService {
    void lockPosts(List<Integer> postIds);
    void unlockPosts(List<Integer> postIds);
    void lockComments(List<Integer> commentIds);
    void unlockComments(List<Integer> commentIds);
}
