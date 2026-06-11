package com.nationlens.repository;

import com.nationlens.domain.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    List<PollOption> findByPollIdOrderByDisplayOrderAsc(Long pollId);

    @Query("SELECT po.id, COUNT(pv.id) FROM PollOption po LEFT JOIN PollVote pv ON pv.pollOptionId = po.id WHERE po.poll.id = :pollId GROUP BY po.id")
    List<Object[]> countVotesByPollId(@Param("pollId") Long pollId);
}
