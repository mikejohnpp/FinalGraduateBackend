package org.social.common.repositories;

import org.social.common.entities.DismissedSuggestion;
import org.social.common.entities.DismissedSuggestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DismissedSuggestionRepository extends JpaRepository<DismissedSuggestion, DismissedSuggestionId> {

    @Query("SELECT ds.id.dismissedUserId FROM DismissedSuggestion ds WHERE ds.id.userId = :userId")
    List<Integer> findDismissedUserIdsByUserId(Integer userId);

    boolean existsByIdUserIdAndIdDismissedUserId(Integer userId, Integer dismissedUserId);
}
