package org.enterprise.organization.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.organization.dto.BranchDto;
import org.enterprise.organization.entity.Branch;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.repository.BranchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchService {

    private final BranchRepository repository;

    public BranchDto create(BranchDto dto) {

        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        Branch entity = new Branch();
        entity.setCompanyId(companyId);
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public BranchDto update(Long id, BranchDto dto) {
        Branch entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public BranchDto getById(Long id) {
        Branch entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<BranchDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(BranchDto dto, Branch entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setShortName(dto.getShortName());
        entity.setBranchType(dto.getBranchType());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setCountry(dto.getCountry());
        entity.setCity(dto.getCity());
        entity.setZipCode(dto.getZipCode());
        entity.setAddress(dto.getAddress());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        if (dto.getHeadOffice() != null) entity.setHeadOffice(dto.getHeadOffice());
        
        if (dto.getCompanyId() != null) {
            Company company = new Company();
            company.setId(dto.getCompanyId());
            entity.setCompany(company);
        } else {
            entity.setCompany(null);
        }
    }

    private BranchDto mapEntityToDto(Branch entity) {
        BranchDto dto = new BranchDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setShortName(entity.getShortName());
        dto.setBranchType(entity.getBranchType());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setCountry(entity.getCountry());
        dto.setCity(entity.getCity());
        dto.setZipCode(entity.getZipCode());
        dto.setAddress(entity.getAddress());
        dto.setActive(entity.getActive());
        dto.setHeadOffice(entity.getHeadOffice());
        
        if (entity.getCompany() != null) {
            dto.setCompanyId(entity.getCompany().getId());
        }
        return dto;
    }
}
