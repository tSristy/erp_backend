package org.enterprise.organization.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.CompanyDto;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository repository;

    public CompanyDto create(CompanyDto dto) {
        Company entity = new Company();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public CompanyDto update(Long id, CompanyDto dto) {
        Company entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public CompanyDto getById(Long id) {
        Company entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<CompanyDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(CompanyDto dto, Company entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setShortName(dto.getShortName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setMobile(dto.getMobile());
        entity.setWebsite(dto.getWebsite());
        entity.setCountry(dto.getCountry());
        entity.setDivision(dto.getDivision());
        entity.setDistrict(dto.getDistrict());
        entity.setCity(dto.getCity());
        entity.setZipCode(dto.getZipCode());
        entity.setAddress(dto.getAddress());
        entity.setTaxNumber(dto.getTaxNumber());
        entity.setVatNumber(dto.getVatNumber());
        entity.setTradeLicenseNo(dto.getTradeLicenseNo());
        entity.setCurrencyCode(dto.getCurrencyCode());
        entity.setTimezone(dto.getTimezone());
        entity.setLanguageCode(dto.getLanguageCode());
        entity.setLogoUrl(dto.getLogoUrl());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
    }

    private CompanyDto mapEntityToDto(Company entity) {
        CompanyDto dto = new CompanyDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setShortName(entity.getShortName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setMobile(entity.getMobile());
        dto.setWebsite(entity.getWebsite());
        dto.setCountry(entity.getCountry());
        dto.setDivision(entity.getDivision());
        dto.setDistrict(entity.getDistrict());
        dto.setCity(entity.getCity());
        dto.setZipCode(entity.getZipCode());
        dto.setAddress(entity.getAddress());
        dto.setTaxNumber(entity.getTaxNumber());
        dto.setVatNumber(entity.getVatNumber());
        dto.setTradeLicenseNo(entity.getTradeLicenseNo());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setTimezone(entity.getTimezone());
        dto.setLanguageCode(entity.getLanguageCode());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setActive(entity.getActive());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        return dto;
    }
}
