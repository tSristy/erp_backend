package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.repository.CompanyRepository;
import org.enterprise.security.dto.UserDto;
import org.enterprise.security.dto.UserRequest;
import org.enterprise.security.entity.Role;
import org.enterprise.security.entity.User;
import org.enterprise.security.entity.UserCompany;
import org.enterprise.security.entity.UserRole;
import org.enterprise.security.entity.UserBranch;
import org.enterprise.security.entity.UserWarehouse;
import org.enterprise.security.entity.UserProfitCenter;
import org.enterprise.security.entity.UserCostCenter;
import org.enterprise.organization.entity.Branch;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.finance.entity.ProfitCenter;
import org.enterprise.finance.entity.CostCenter;
import org.enterprise.security.repository.RoleRepository;
import org.enterprise.security.repository.UserRepository;
import org.enterprise.organization.repository.BranchRepository;
import org.enterprise.inventory.repository.WarehouseRepository;
import org.enterprise.finance.repository.ProfitCenterRepository;
import org.enterprise.finance.repository.CostCenterRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProfitCenterRepository profitCenterRepository;
    private final CostCenterRepository costCenterRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> getAllUsers() {
        Long companyId = TenantContext.get().getCompanyId();
        return userRepository.findActiveUsersByCompanyId(companyId).stream()
                .map(this::mapToDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public UserDto createUser(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setActive(request.getActive());
        user.setLocked(request.getLocked());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);

        updateUserRelations(user, request);

        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setActive(request.getActive());
        user.setLocked(request.getLocked());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Reset failed login attempts if unlocked
        if (Boolean.FALSE.equals(request.getLocked())) {
            user.setFailedLoginAttempts(0);
        }

        updateUserRelations(user, request);

        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    private void updateUserRelations(User user, UserRequest request) {
        Long currentCompanyId = TenantContext.get().getCompanyId();
        
        // Roles
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        Set<Long> requestedRoles = request.getRoles() != null ? new HashSet<>(request.getRoles()) : new HashSet<>();
        user.getRoles().removeIf(ur -> !requestedRoles.contains(ur.getRole().getId()));
        Set<Long> existingRoleIds = user.getRoles().stream().map(ur -> ur.getRole().getId()).collect(java.util.stream.Collectors.toSet());
        for (Long roleId : requestedRoles) {
            if (!existingRoleIds.contains(roleId)) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
                UserRole ur = new UserRole();
                ur.setUser(user);
                ur.setRole(role);
                ur.setCompanyId(currentCompanyId);
                ur.setActive(true);
                user.getRoles().add(ur);
            }
        }
        
        // Companies
        if (user.getCompanies() == null) {
            user.setCompanies(new java.util.ArrayList<>());
        }
        Set<Long> requestedCompanies = request.getCompanies() != null ? new HashSet<>(request.getCompanies()) : new HashSet<>();
        user.getCompanies().removeIf(uc -> !requestedCompanies.contains(uc.getCompany().getId()));
        Set<Long> existingCompanyIds = user.getCompanies().stream().map(uc -> uc.getCompany().getId()).collect(java.util.stream.Collectors.toSet());
        for (Long companyId : requestedCompanies) {
            if (!existingCompanyIds.contains(companyId)) {
                Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
                UserCompany uc = new UserCompany();
                uc.setUser(user);
                uc.setCompany(company);
                uc.setCompanyId(company.getId());
                uc.setDefaultCompany(company.getId().equals(currentCompanyId));
                uc.setActive(true);
                user.getCompanies().add(uc);
            }
        }
        
        // Branches
        if (user.getUserBranches() == null) {
            user.setUserBranches(new java.util.ArrayList<>());
        }
        Set<Long> requestedBranches = request.getBranches() != null ? new HashSet<>(request.getBranches()) : new HashSet<>();
        user.getUserBranches().removeIf(ub -> !requestedBranches.contains(ub.getBranch().getId()));
        Set<Long> existingBranchIds = user.getUserBranches().stream().map(ub -> ub.getBranch().getId()).collect(java.util.stream.Collectors.toSet());
        for (Long branchId : requestedBranches) {
            if (!existingBranchIds.contains(branchId)) {
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
                UserBranch ub = new UserBranch();
                ub.setUser(user);
                ub.setBranch(branch);
                ub.setCompanyId(currentCompanyId);
                user.getUserBranches().add(ub);
            }
        }
        
        // Warehouses
        if (user.getUserWarehouses() == null) {
            user.setUserWarehouses(new java.util.ArrayList<>());
        }
        Set<Long> requestedWarehouses = request.getWarehouses() != null ? new HashSet<>(request.getWarehouses()) : new HashSet<>();
        user.getUserWarehouses().removeIf(uw -> !requestedWarehouses.contains(uw.getWarehouse().getId()));
        Set<Long> existingWarehouseIds = user.getUserWarehouses().stream().map(uw -> uw.getWarehouse().getId()).collect(java.util.stream.Collectors.toSet());
        for (Long warehouseId : requestedWarehouses) {
            if (!existingWarehouseIds.contains(warehouseId)) {
                Warehouse warehouse = warehouseRepository.findById(warehouseId)
                        .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseId));
                UserWarehouse uw = new UserWarehouse();
                uw.setUser(user);
                uw.setWarehouse(warehouse);
                uw.setCompanyId(currentCompanyId);
                user.getUserWarehouses().add(uw);
            }
        }
        
        // Profit Centers
        if (user.getUserProfitCenters() == null) {
            user.setUserProfitCenters(new java.util.ArrayList<>());
        }
        Set<Long> requestedProfitCenters = request.getProfitCenters() != null ? new HashSet<>(request.getProfitCenters()) : new HashSet<>();
        user.getUserProfitCenters().removeIf(upc -> !requestedProfitCenters.contains(upc.getProfitCenter().getId()));
        Set<Long> existingProfitCenterIds = user.getUserProfitCenters().stream().map(upc -> upc.getProfitCenter().getId()).collect(java.util.stream.Collectors.toSet());
        for (Long pcId : requestedProfitCenters) {
            if (!existingProfitCenterIds.contains(pcId)) {
                ProfitCenter pc = profitCenterRepository.findById(pcId)
                        .orElseThrow(() -> new RuntimeException("ProfitCenter not found: " + pcId));
                UserProfitCenter upc = new UserProfitCenter();
                upc.setUser(user);
                upc.setProfitCenter(pc);
                upc.setCompanyId(currentCompanyId);
                user.getUserProfitCenters().add(upc);
            }
        }
        
        // Cost Centers
        if (user.getUserCostCenters() == null) {
            user.setUserCostCenters(new java.util.ArrayList<>());
        }
        Set<Long> requestedCostCenters = request.getCostCenters() != null ? new HashSet<>(request.getCostCenters()) : new HashSet<>();
        user.getUserCostCenters().removeIf(ucc -> !requestedCostCenters.contains(ucc.getCostCenter().getId()));
        Set<Long> existingCostCenterIds = user.getUserCostCenters().stream().map(ucc -> ucc.getCostCenter().getId()).collect(java.util.stream.Collectors.toSet());
        for (Long ccId : requestedCostCenters) {
            if (!existingCostCenterIds.contains(ccId)) {
                CostCenter cc = costCenterRepository.findById(ccId)
                        .orElseThrow(() -> new RuntimeException("CostCenter not found: " + ccId));
                UserCostCenter ucc = new UserCostCenter();
                ucc.setUser(user);
                ucc.setCostCenter(cc);
                ucc.setCompanyId(currentCompanyId);
                user.getUserCostCenters().add(ucc);
            }
        }
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setActive(user.getActive());
        dto.setLocked(user.getLocked());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());

        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .filter(ur -> Boolean.TRUE.equals(ur.getActive()) && ur.getRole() != null)
                    .map(ur -> ur.getRole().getId())
                    .toList());
        }

        if (user.getCompanies() != null) {
            dto.setCompanies(user.getCompanies().stream()
                    .filter(uc -> Boolean.TRUE.equals(uc.getActive()) && uc.getCompany() != null)
                    .map(uc -> uc.getCompany().getId())
                    .toList());
        }
        
        if (user.getUserBranches() != null) {
            dto.setBranches(user.getUserBranches().stream()
                    .filter(ub -> ub.getBranch() != null)
                    .map(ub -> ub.getBranch().getId())
                    .toList());
        }

        if (user.getUserWarehouses() != null) {
            dto.setWarehouses(user.getUserWarehouses().stream()
                    .filter(uw -> uw.getWarehouse() != null)
                    .map(uw -> uw.getWarehouse().getId())
                    .toList());
        }

        if (user.getUserProfitCenters() != null) {
            dto.setProfitCenters(user.getUserProfitCenters().stream()
                    .filter(up -> up.getProfitCenter() != null)
                    .map(up -> up.getProfitCenter().getId())
                    .toList());
        }

        if (user.getUserCostCenters() != null) {
            dto.setCostCenters(user.getUserCostCenters().stream()
                    .filter(uc -> uc.getCostCenter() != null)
                    .map(uc -> uc.getCostCenter().getId())
                    .toList());
        }

        return dto;
    }
}
