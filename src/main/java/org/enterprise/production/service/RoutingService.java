package org.enterprise.production.service;

import org.enterprise.production.dto.RoutingDto;
import org.enterprise.production.dto.RoutingOperationDto;
import org.enterprise.production.entity.Routing;
import org.enterprise.production.entity.RoutingOperation;
import org.enterprise.production.repository.RoutingRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutingService {

    private final RoutingRepository repository;

    public RoutingService(RoutingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RoutingDto> findAll() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoutingDto findById(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public RoutingDto save(RoutingDto dto) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        Routing entity = convertToEntity(dto);
        entity.setCompanyId(companyId);

        if (entity.getOperations() != null) {
            for (RoutingOperation op : entity.getOperations()) {
                if (op.getCompanyId() == null) {
                    op.setCompanyId(companyId);
                }
            }
        }

        Routing saved = repository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private RoutingDto convertToDTO(Routing entity) {
        RoutingDto dto = new RoutingDto();
        BeanUtils.copyProperties(entity, dto, "operations");
        if (entity.getOperations() != null) {
            dto.setOperations(entity.getOperations().stream().map(op -> {
                RoutingOperationDto opDto = new RoutingOperationDto();
                BeanUtils.copyProperties(op, opDto);
                if (op.getWorkCenter() != null) opDto.setWorkCenterId(op.getWorkCenter().getId());
                return opDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Routing convertToEntity(RoutingDto dto) {
        Routing entity = new Routing();
        BeanUtils.copyProperties(dto, entity, "operations");
        if (dto.getOperations() != null) {
            entity.setOperations(dto.getOperations().stream().map(opDto -> {
                RoutingOperation op = new RoutingOperation();
                BeanUtils.copyProperties(opDto, op);
                op.setRouting(entity);
                if (opDto.getWorkCenterId() != null) {
                    org.enterprise.production.entity.WorkCenter wc = new org.enterprise.production.entity.WorkCenter();
                    wc.setId(opDto.getWorkCenterId());
                    op.setWorkCenter(wc);
                }
                return op;
            }).collect(Collectors.toList()));
        }
        return entity;
    }
}
