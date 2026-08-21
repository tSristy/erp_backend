package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.dto.PaginatedResponse;
import org.enterprise.security.dto.LoginAuditDto;
import org.enterprise.security.entity.LoginAudit;
import org.enterprise.security.repository.LoginAuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginAuditService {

    private final LoginAuditRepository loginAuditRepository;

    @Transactional(readOnly = true)
    public PaginatedResponse<LoginAuditDto> getLoginAudits(int page, int limit, String search) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "loginAt"));
        
        Specification<LoginAudit> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.trim().isEmpty()) {
            String likePattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("username")), likePattern),
                    cb.like(cb.lower(root.get("ipAddress")), likePattern),
                    cb.like(cb.lower(root.get("deviceInfo")), likePattern)
                )
            );
        }

        Page<LoginAudit> pagedResult = loginAuditRepository.findAll(spec, pageable);
        
        PaginatedResponse<LoginAuditDto> response = new PaginatedResponse<>();
        response.setItems(pagedResult.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()));
        response.setTotalElements(pagedResult.getTotalElements());
        response.setPage(page);
        response.setLimit(limit);
        
        return response;
    }

    private LoginAuditDto mapToDto(LoginAudit audit) {
        LoginAuditDto dto = new LoginAuditDto();
        dto.setId(audit.getId());
        dto.setUsername(audit.getUsername());
        dto.setIpAddress(audit.getIpAddress());
        dto.setDeviceInfo(audit.getDeviceInfo());
        dto.setSuccess(audit.getSuccess());
        dto.setLoginAt(audit.getLoginAt());
        dto.setFailureReason(audit.getFailureReason());
        return dto;
    }
}
