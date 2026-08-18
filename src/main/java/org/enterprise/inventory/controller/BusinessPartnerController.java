package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.service.BusinessPartnerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business-partners")
@RequiredArgsConstructor
public class BusinessPartnerController {

    private final BusinessPartnerService service;
    private final org.enterprise.finance.repository.AccountRepository accountRepository;

    @PostMapping
    public BusinessPartner create(@RequestBody BusinessPartner bp) {
        resolveAccounts(bp);
        return service.save(bp);
    }

    private void resolveAccounts(BusinessPartner bp) {
        if (bp.getCustomerDetail() != null) {
            bp.getCustomerDetail().setPartner(bp);
            if (bp.getCustomerDetail().getAccountsReceivableAccount() != null && bp.getCustomerDetail().getAccountsReceivableAccount().getId() != null) {
                bp.getCustomerDetail().setAccountsReceivableAccount(accountRepository.getReferenceById(bp.getCustomerDetail().getAccountsReceivableAccount().getId()));
            }
        }
        if (bp.getVendorDetail() != null) {
            bp.getVendorDetail().setPartner(bp);
            if (bp.getVendorDetail().getAccountsPayableAccount() != null && bp.getVendorDetail().getAccountsPayableAccount().getId() != null) {
                bp.getVendorDetail().setAccountsPayableAccount(accountRepository.getReferenceById(bp.getVendorDetail().getAccountsPayableAccount().getId()));
            }
            if (bp.getVendorDetail().getGrnClearingAccount() != null && bp.getVendorDetail().getGrnClearingAccount().getId() != null) {
                bp.getVendorDetail().setGrnClearingAccount(accountRepository.getReferenceById(bp.getVendorDetail().getGrnClearingAccount().getId()));
            }
        }
        if (bp.getShareholderDetail() != null) {
            bp.getShareholderDetail().setPartner(bp);
            if (bp.getShareholderDetail().getEquityAccount() != null && bp.getShareholderDetail().getEquityAccount().getId() != null) {
                bp.getShareholderDetail().setEquityAccount(accountRepository.getReferenceById(bp.getShareholderDetail().getEquityAccount().getId()));
            }
            if (bp.getShareholderDetail().getDividendPayableAccount() != null && bp.getShareholderDetail().getDividendPayableAccount().getId() != null) {
                bp.getShareholderDetail().setDividendPayableAccount(accountRepository.getReferenceById(bp.getShareholderDetail().getDividendPayableAccount().getId()));
            }
        }
    }

    @GetMapping
    public List<BusinessPartner> getAll(@RequestParam(required = false) String role) {
        if (role != null && !role.equals("BOTH")) {
            return service.findAllByRole(org.enterprise.inventory.entity.BusinessPartnerRole.RoleType.valueOf(role));
        }
        return service.findAll();
    }

    @GetMapping("/code/{code}")
    public BusinessPartner getByCode(@PathVariable String code) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        return service.findByCode(code, companyId)
                .orElseThrow(() -> new RuntimeException("Business Partner not found"));
    }

    @PutMapping("/code/{code}")
    public BusinessPartner update(@PathVariable String code, @RequestBody BusinessPartner bp) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        BusinessPartner existing = service.findByCode(code, companyId)
                .orElseThrow(() -> new RuntimeException("Business Partner not found"));
        
        existing.setName(bp.getName());
        existing.setPartnerType(bp.getPartnerType());
        existing.setEmail(bp.getEmail());
        existing.setPhone(bp.getPhone());
        existing.setMobile(bp.getMobile());
        existing.setWebsite(bp.getWebsite());
        existing.setActive(bp.getActive());
        existing.setRemarks(bp.getRemarks());
        existing.setRole(bp.getRole());
        
        if (bp.getCustomerDetail() != null) {
            if (existing.getCustomerDetail() == null) {
                existing.setCustomerDetail(new org.enterprise.inventory.entity.CustomerDetail());
            }
            existing.getCustomerDetail().setCreditLimit(bp.getCustomerDetail().getCreditLimit());
            existing.getCustomerDetail().setPaymentTermDays(bp.getCustomerDetail().getPaymentTermDays());
            existing.getCustomerDetail().setAccountsReceivableAccount(bp.getCustomerDetail().getAccountsReceivableAccount());
        }

        if (bp.getVendorDetail() != null) {
            if (existing.getVendorDetail() == null) {
                existing.setVendorDetail(new org.enterprise.inventory.entity.VendorDetail());
            }
            existing.getVendorDetail().setPaymentTermDays(bp.getVendorDetail().getPaymentTermDays());
            existing.getVendorDetail().setAccountsPayableAccount(bp.getVendorDetail().getAccountsPayableAccount());
            existing.getVendorDetail().setGrnClearingAccount(bp.getVendorDetail().getGrnClearingAccount());
        }

        if (bp.getShareholderDetail() != null) {
            if (existing.getShareholderDetail() == null) {
                existing.setShareholderDetail(new org.enterprise.inventory.entity.ShareholderDetail());
            }
            existing.getShareholderDetail().setEquityPercentage(bp.getShareholderDetail().getEquityPercentage());
            existing.getShareholderDetail().setEquityAccount(bp.getShareholderDetail().getEquityAccount());
            existing.getShareholderDetail().setDividendPayableAccount(bp.getShareholderDetail().getDividendPayableAccount());
        }

        resolveAccounts(existing);
        
        return service.save(existing);
    }
}