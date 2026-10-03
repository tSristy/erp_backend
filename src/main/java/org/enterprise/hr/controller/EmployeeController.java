package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeDto;
import org.enterprise.hr.dto.EmployeeEducationDto;
import org.enterprise.hr.dto.EmployeeExperienceDto;
import org.enterprise.hr.service.EmployeeService;
import org.enterprise.hr.service.EmployeeEducationService;
import org.enterprise.hr.service.EmployeeExperienceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;
    private final EmployeeEducationService educationService;
    private final EmployeeExperienceService experienceService;

    @PostMapping
    public ResponseEntity<EmployeeDto> create(
            @RequestBody EmployeeDto dto) {

        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {

        return ResponseEntity.ok(service.search(keyword, pageable));
    }

    // Education Endpoints
    @PostMapping("/educations")
    public ResponseEntity<EmployeeEducationDto> createEducation(@Valid @RequestBody EmployeeEducationDto dto) {
        return ResponseEntity.ok(educationService.create(dto));
    }

    @PutMapping("/educations/{id}")
    public ResponseEntity<EmployeeEducationDto> updateEducation(@PathVariable Long id, @Valid @RequestBody EmployeeEducationDto dto) {
        return ResponseEntity.ok(educationService.update(id, dto));
    }

    @GetMapping("/educations/{id}")
    public ResponseEntity<EmployeeEducationDto> getEducationById(@PathVariable Long id) {
        return ResponseEntity.ok(educationService.getById(id));
    }

    @GetMapping("/educations")
    public ResponseEntity<Page<EmployeeEducationDto>> searchEducations(Pageable pageable) {
        return ResponseEntity.ok(educationService.search(pageable));
    }

    @DeleteMapping("/educations/{id}")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long id) {
        educationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Experience Endpoints
    @PostMapping("/experiences")
    public ResponseEntity<EmployeeExperienceDto> createExperience(@Valid @RequestBody EmployeeExperienceDto dto) {
        return ResponseEntity.ok(experienceService.create(dto));
    }

    @PutMapping("/experiences/{id}")
    public ResponseEntity<EmployeeExperienceDto> updateExperience(@PathVariable Long id, @Valid @RequestBody EmployeeExperienceDto dto) {
        return ResponseEntity.ok(experienceService.update(id, dto));
    }

    @GetMapping("/experiences/{id}")
    public ResponseEntity<EmployeeExperienceDto> getExperienceById(@PathVariable Long id) {
        return ResponseEntity.ok(experienceService.getById(id));
    }

    @GetMapping("/experiences")
    public ResponseEntity<Page<EmployeeExperienceDto>> searchExperiences(Pageable pageable) {
        return ResponseEntity.ok(experienceService.search(pageable));
    }

    @DeleteMapping("/experiences/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        experienceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.hr.dto.EmployeeDto> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchEmployees(org.enterprise.common.util.TenantContext.getCompanyId(), q, pageable);
    }

}