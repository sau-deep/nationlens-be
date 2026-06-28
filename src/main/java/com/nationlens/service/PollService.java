package com.nationlens.service;

import com.nationlens.domain.entity.Poll;
import com.nationlens.domain.entity.PollOption;
import com.nationlens.domain.entity.PollVote;
import com.nationlens.dto.poll.AdminPollRequest;
import com.nationlens.dto.poll.PollDto;
import com.nationlens.dto.poll.PollOptionDto;
import com.nationlens.repository.PollOptionRepository;
import com.nationlens.repository.PollRepository;
import com.nationlens.repository.PollVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    /** Admin listing — includes inactive/expired polls. */
    public List<PollDto> listAll() {
        return pollRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toDto).toList();
    }

    @Transactional
    public PollDto create(AdminPollRequest req, Long createdBy) {
        Poll poll = new Poll();
        poll.setQuestionEn(req.getQuestionEn());
        poll.setQuestionHi(req.getQuestionHi());
        poll.setEntityId(req.getEntityId());
        poll.setDistrictId(req.getDistrictId());
        poll.setIsActive(req.getIsActive() == null ? Boolean.TRUE : req.getIsActive());
        poll.setExpiresAt(req.getExpiresAt());
        poll.setCreatedBy(createdBy);
        poll.setCreatedAt(LocalDateTime.now());

        int order = 0;
        for (AdminPollRequest.Option o : req.getOptions()) {
            PollOption opt = new PollOption();
            opt.setPoll(poll);
            opt.setLabelEn(o.getLabelEn());
            opt.setLabelHi(o.getLabelHi());
            opt.setDisplayOrder(o.getDisplayOrder() != null ? o.getDisplayOrder() : order);
            poll.getOptions().add(opt);
            order++;
        }

        pollRepository.save(poll);
        return toDto(poll);
    }

    @Transactional
    public PollDto update(Long id, AdminPollRequest req) {
        Poll poll = pollRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Poll not found: " + id));

        poll.setQuestionEn(req.getQuestionEn());
        poll.setQuestionHi(req.getQuestionHi());
        poll.setEntityId(req.getEntityId());
        poll.setDistrictId(req.getDistrictId());
        if (req.getIsActive() != null) poll.setIsActive(req.getIsActive());
        poll.setExpiresAt(req.getExpiresAt());

        // Reconcile options: update existing, add new, delete removed (with their votes).
        Map<Long, PollOption> existing = poll.getOptions().stream()
            .collect(Collectors.toMap(PollOption::getId, o -> o));
        Set<Long> keptIds = new HashSet<>();

        int order = 0;
        for (AdminPollRequest.Option o : req.getOptions()) {
            int displayOrder = o.getDisplayOrder() != null ? o.getDisplayOrder() : order;
            if (o.getId() != null && existing.containsKey(o.getId())) {
                PollOption opt = existing.get(o.getId());
                opt.setLabelEn(o.getLabelEn());
                opt.setLabelHi(o.getLabelHi());
                opt.setDisplayOrder(displayOrder);
                keptIds.add(opt.getId());
            } else {
                PollOption opt = new PollOption();
                opt.setPoll(poll);
                opt.setLabelEn(o.getLabelEn());
                opt.setLabelHi(o.getLabelHi());
                opt.setDisplayOrder(displayOrder);
                poll.getOptions().add(opt);
            }
            order++;
        }

        // Remove options no longer present, clearing their votes first (FK constraint).
        List<PollOption> toRemove = poll.getOptions().stream()
            .filter(o -> o.getId() != null && !keptIds.contains(o.getId()))
            .toList();
        for (PollOption opt : toRemove) {
            pollVoteRepository.deleteByPollOptionId(opt.getId());
        }
        poll.getOptions().removeAll(toRemove);

        pollRepository.save(poll);
        return toDto(poll);
    }

    @Transactional
    public PollDto setActive(Long id, boolean active) {
        Poll poll = pollRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Poll not found: " + id));
        poll.setIsActive(active);
        pollRepository.save(poll);
        return toDto(poll);
    }

    @Transactional
    public void delete(Long id) {
        Poll poll = pollRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Poll not found: " + id));
        // Clear votes first — poll_votes has FK constraints to polls and poll_options.
        pollVoteRepository.deleteByPollId(id);
        pollRepository.delete(poll); // options are removed via cascade
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
            .entityId(poll.getEntityId())
            .districtId(poll.getDistrictId())
            .isActive(poll.getIsActive())
            .expiresAt(poll.getExpiresAt())
            .createdAt(poll.getCreatedAt())
            .options(options)
            .totalVotes(total)
            .build();
    }
}
