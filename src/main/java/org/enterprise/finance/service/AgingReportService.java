package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.dto.AgingReportLineDto;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.PurchaseInvoice;
import org.enterprise.inventory.repository.BusinessPartnerRepository;
import org.enterprise.inventory.repository.PurchaseInvoiceRepository;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.repository.SalesInvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgingReportService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final BusinessPartnerRepository businessPartnerRepository;

    public List<AgingReportLineDto> getCustomerAging() {

        
        List<SalesInvoice> unpaidInvoices = salesInvoiceRepository.findAll().stream()
                .filter(inv -> inv.getStatus() == SalesInvoice.InvoiceStatus.POSTED)
                .filter(inv -> inv.getTotalAmount().subtract(inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
                .toList();

        return calculateAging(unpaidInvoices, true);
    }

    public List<AgingReportLineDto> getVendorAging() {
        List<PurchaseInvoice> unpaidInvoices = purchaseInvoiceRepository.findAll().stream()
                .filter(inv -> inv.getStatus() == PurchaseInvoice.InvoiceStatus.POSTED)
                .filter(inv -> inv.getTotalAmount().subtract(inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
                .toList();

        return calculateAging(unpaidInvoices, false);
    }

    private List<AgingReportLineDto> calculateAging(List<?> unpaidInvoices, boolean isCustomer) {
        java.util.Map<Long, AgingReportLineDto> agingMap = new java.util.HashMap<>();

        LocalDate today = LocalDate.now();

        for (Object obj : unpaidInvoices) {
            Long partnerId;
            String partnerName;
            LocalDate dueDate;
            BigDecimal unpaidBalance;

            if (isCustomer) {
                SalesInvoice inv = (SalesInvoice) obj;
                partnerId = inv.getCustomer().getId();
                partnerName = inv.getCustomer().getName();
                dueDate = inv.getDueDate();
                unpaidBalance = inv.getTotalAmount().subtract(inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO);
            } else {
                PurchaseInvoice inv = (PurchaseInvoice) obj;
                partnerId = inv.getVendor().getId();
                partnerName = inv.getVendor().getName();
                dueDate = inv.getDueDate();
                unpaidBalance = inv.getTotalAmount().subtract(inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO);
            }
            
            if (dueDate == null) dueDate = today;

            AgingReportLineDto line = agingMap.computeIfAbsent(partnerId, id -> {
                AgingReportLineDto dto = new AgingReportLineDto();
                dto.setPartnerId(partnerId);
                dto.setPartnerName(partnerName);
                return dto;
            });

            long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);

            if (daysOverdue <= 0) {
                line.setCurrentBalance(line.getCurrentBalance().add(unpaidBalance));
            } else if (daysOverdue <= 30) {
                line.setDays30(line.getDays30().add(unpaidBalance));
            } else if (daysOverdue <= 60) {
                line.setDays60(line.getDays60().add(unpaidBalance));
            } else if (daysOverdue <= 90) {
                line.setDays90(line.getDays90().add(unpaidBalance));
            } else {
                line.setDaysOver90(line.getDaysOver90().add(unpaidBalance));
            }

            line.setTotalBalance(line.getTotalBalance().add(unpaidBalance));
        }

        return new ArrayList<>(agingMap.values());
    }
}
