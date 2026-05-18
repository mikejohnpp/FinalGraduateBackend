package org.social.common.repositories;

import org.social.common.entities.PostDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostDetailRepository extends JpaRepository<PostDetail, Integer> {

    List<PostDetail> findByPost_Id(Integer postId);

    void deleteByPost_Id(Integer postId);
}
