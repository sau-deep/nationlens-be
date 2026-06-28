package com.nationlens.service;

import com.nationlens.domain.entity.District;
import com.nationlens.dto.admin.AdminDistrictDto;
import com.nationlens.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DistrictService {

    private final DistrictRepository districtRepository;

    public List<District> listAll() {
        return districtRepository.findAll().stream()
            .sorted(Comparator.comparing(District::getNameEn, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public List<AdminDistrictDto> listForAdmin() {
        return listAll().stream().map(this::toAdminDto).toList();
    }

    public Optional<District> findBySlug(String slug) {
        return districtRepository.findBySlug(slug);
    }

    public List<District> listByState(Long stateId) {
        return districtRepository.findByStateIdOrderByNameEnAsc(stateId);
    }

    public List<District> listFeaturedForHome(int limit) {
        return districtRepository.findByIsFeaturedTrueOrderByHomeDisplayOrderAscNameEnAsc().stream()
            .limit(limit)
            .toList();
    }

    @Transactional
    public AdminDistrictDto updateHomeFeatured(Long id, Boolean isFeatured, Integer homeDisplayOrder) {
        District district = districtRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("District not found: " + id));
        if (isFeatured != null) district.setIsFeatured(isFeatured);
        if (homeDisplayOrder != null) district.setHomeDisplayOrder(homeDisplayOrder);
        districtRepository.save(district);
        return toAdminDto(district);
    }

    private AdminDistrictDto toAdminDto(District d) {
        AdminDistrictDto.AdminDistrictDtoBuilder b = AdminDistrictDto.builder()
            .id(d.getId())
            .slug(d.getSlug())
            .nameEn(d.getNameEn())
            .nameHi(d.getNameHi())
            .isFeatured(d.getIsFeatured())
            .homeDisplayOrder(d.getHomeDisplayOrder())
            .population(d.getPopulation());
        if (d.getState() != null) {
            b.stateCode(d.getState().getCode())
             .stateNameEn(d.getState().getNameEn());
        }
        return b.build();
    }
}
