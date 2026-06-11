package com.nationlens.service;

import com.nationlens.domain.entity.Poll;
import com.nationlens.domain.entity.PollVote;
import com.nationlens.dto.poll.PollDto;
import com.nationlens.dto.poll.PollOptionDto;
import com.nationlens.repository.PollOptionRepository;
import com.nationlens.repository.PollRepository;
import com.nationlens.repository.PollVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;

    public List<PollDto> listActive() {
        return pollRepository.findByIsActiveTrueOrderByCreatedAtDesc().stream()
            .map(this::toDto).toList();
    }

    public Optional<PollDto> findById(Long id) {
        return pollRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public PollDto vote(Long pollId, Long optionId, Long userId) {
        if (pollVoteRepository.existsByPollIdAndUserId(pollId, userId)) {
            throw new IllegalStateException("Already voted");
        }
        Poll poll = pollRepository.findById(pollId)
            .orElseThrow(() -> new IllegalArgumentException("Poll not found"));
        if (!Boolean.TRUE.equals(poll.getIsActive())) {
            throw new IllegalStateException("Poll is closed");
        }

        PollVote vote = new PollVote();
        vote.setPollId(pollId);
        vote.setPollOptionId(optionId);
        vote.setUserId(userId);
        vote.setVotedAt(LocalDateTime.now());
        pollVoteRepository.save(vote);

        return toDto(poll);
    }

    private PollDto toDto(Poll poll) {
        Map<Long, Long> voteCounts = pollOptionRepository
            .countVotesByPollId(poll.getId()).stream()
            .collect(Collectors.toMap(
                r -> (Long) r[0],
                r -> (Long) r[1]
            ));

        long total = voteCounts.values().stream().mapToLong(Long::longValue).sum();

        List<PollOptionDto> options = poll.getOptions().stream().map(o -> {
            long count = voteCounts.getOrDefault(o.getId(), 0L);
            double pct = total > 0 ? (count * 100.0 / total) : 0.0;
            return PollOptionDto.builder()
                .id(o.getId())
                .labelEn(o.getLabelEn())
                .labelHi(o.getLabelHi())
                .displayOrder(o.getDisplayOrder())
                .voteCount(count)
                .votePercentage(Math.round(pct * 10.0) / 10.0)
                .build();
        }).toList();

        return PollDto.builder()
            .id(poll.getId())
            .questionEn(poll.getQuestionEn())
            .questionHi(poll.getQuestionHi())
            .isActive(poll.getIsActive())
            .expiresAt(poll.getExpiresAt())
            .createdAt(poll.getCreatedAt())
            .options(options)
            .totalVotes(total)
            .build();
    }
}
