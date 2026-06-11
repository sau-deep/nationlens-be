package com.nationlens.service;

import com.nationlens.domain.entity.District;
import com.nationlens.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DistrictService {

    private final DistrictRepository districtRepository;

    public List<District> listAll() {
        return districtRepository.findAll();
    }

    public Optional<District> findBySlug(String slug) {
        return districtRepository.findBySlug(slug);
    }

    public List<District> listByState(Long stateId) {
        return districtRepository.findByStateIdOrderByNameEnAsc(stateId);
    }
}
