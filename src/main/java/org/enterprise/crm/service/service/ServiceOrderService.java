package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServiceEstimate;
import org.enterprise.crm.service.entity.ServiceEstimateDetail;
import org.enterprise.crm.service.entity.ServiceOrder;
import org.enterprise.crm.service.entity.ServicePartsRequisition;
import org.enterprise.crm.service.entity.ServicePartsRequisitionDetail;
import org.enterprise.crm.service.entity.ServiceRequest;
import org.enterprise.crm.service.repository.ServiceEstimateRepository;
import org.enterprise.crm.service.repository.ServiceOrderRepository;
import org.enterprise.crm.service.repository.ServicePartsRequisitionRepository;
import org.enterprise.crm.service.repository.ServiceRequestRepository;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.enums.ProductType;
import org.enterprise.inventory.repository.ProductRepository;
import org.enterprise.inventory.repository.WarehouseRepository;
import org.enterprise.inventory.service.BaseService;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.entity.SalesInvoiceDetail;
import org.enterprise.sales.entity.SalesInvoiceDiscount;
import org.enterprise.sales.service.SalesInvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ServiceOrderService extends BaseService<ServiceOrder, Long> {

    private final ServiceOrderRepository repository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceEstimateRepository serviceEstimateRepository;
    private final ServicePartsRequisitionRepository partsRequisitionRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesInvoiceService salesInvoiceService;

    public ServiceOrderService(
            ServiceOrderRepository repository,
            ServiceRequestRepository serviceRequestRepository,
            ServiceEstimateRepository serviceEstimateRepository,
            ServicePartsRequisitionRepository partsRequisitionRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            SalesInvoiceService salesInvoiceService) {
        super(repository);
        this.repository = repository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceEstimateRepository = serviceEstimateRepository;
        this.partsRequisitionRepository = partsRequisitionRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.salesInvoiceService = salesInvoiceService;
    }

    public List<ServiceOrder> findByServiceRequestId(Long serviceRequestId) {
        return repository.findByServiceRequestId(serviceRequestId);
    }

    public String generateNextOrderNumber() {
        return repository.findTopByOrderNumberStartingWithOrderByIdDesc("SO-")
            .map(lastOrder -> {
                try {
                    String raw = lastOrder.getOrderNumber();
                    String digits = raw.replaceAll("\\D+", "");
                    int seq = Integer.parseInt(digits);
                    return String.format("SO-%04d", seq + 1);
                } catch (Exception e) {
                    return "SO-" + String.format("%04d", lastOrder.getId() != null ? lastOrder.getId() + 1 : 1);
                }
            })
            .orElseGet(() -> {
                return repository.findTopByOrderByIdDesc()
                    .map(last -> String.format("SO-%04d", last.getId() + 1))
                    .orElse("SO-0001");
            });
    }

    @Transactional
    @Override
    public ServiceOrder save(ServiceOrder entity) {
        if (entity.getOrderNumber() == null || entity.getOrderNumber().trim().isEmpty()) {
            String nextNum = generateNextOrderNumber();
            while (repository.existsByOrderNumber(nextNum)) {
                try {
                    int seq = Integer.parseInt(nextNum.replaceAll("\\D+", ""));
                    nextNum = String.format("SO-%04d", seq + 1);
                } catch (Exception e) {
                    nextNum = "SO-" + (System.currentTimeMillis() % 100000);
                }
            }
            entity.setOrderNumber(nextNum);
        }

        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId != null && entity.getCompanyId() == null) {
            entity.setCompanyId(companyId);
        }

        Long reqId = null;
        if (entity.getServiceRequest() != null && entity.getServiceRequest().getId() != null) {
            reqId = entity.getServiceRequest().getId();
        } else if (entity.getServiceRequestId() != null) {
            reqId = entity.getServiceRequestId();
        }

        if (reqId != null) {
            entity.setServiceRequest(serviceRequestRepository.findById(reqId).orElse(null));
        }

        if (entity.getStatus() == ServiceOrder.ServiceOrderStatus.RESOLVED) {
            if (entity.getQcStatus() != ServiceOrder.QCStatus.PASSED) {
                throw new RuntimeException("Service Order cannot be marked as RESOLVED until Quality Control (QC) status is PASSED");
            }
        }

        ServiceOrder saved = super.save(entity);

        if (saved.getServiceRequest() != null) {
            org.enterprise.crm.service.entity.ServiceRequest sr = saved.getServiceRequest();
            boolean srChanged = false;

            if (saved.getOdometerReading() != null && sr.getOdometerReading() == null) {
                sr.setOdometerReading(saved.getOdometerReading());
                srChanged = true;
            } else if (saved.getOdometerReading() == null && sr.getOdometerReading() != null) {
                saved.setOdometerReading(sr.getOdometerReading());
                saved = super.save(saved);
            }

            if (saved.getStatus() == ServiceOrder.ServiceOrderStatus.RESOLVED
                    && saved.getQcStatus() == ServiceOrder.QCStatus.PASSED
                    && sr.getStatus() != org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.DELIVERED
                    && sr.getStatus() != org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.CLOSED) {
                sr.setStatus(org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.READY_FOR_DELIVERY);
                srChanged = true;
            } else if (saved.getQcStatus() == ServiceOrder.QCStatus.FAILED
                    && sr.getStatus() == org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.READY_FOR_DELIVERY) {
                sr.setStatus(org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.IN_PROGRESS);
                srChanged = true;
            } else if (saved.getStatus() == ServiceOrder.ServiceOrderStatus.DIAGNOSED
                    && sr.getStatus() == org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.OPEN) {
                sr.setStatus(org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.PENDING_ESTIMATE);
                srChanged = true;
            } else if (saved.getStatus() == ServiceOrder.ServiceOrderStatus.IN_PROGRESS
                    && (sr.getStatus() == org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.OPEN
                    || sr.getStatus() == org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.DIAGNOSIS
                    || sr.getStatus() == org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.PENDING_ESTIMATE
                    || sr.getStatus() == org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.WAITING_APPROVAL
                    || sr.getStatus() == org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.APPROVED
                    || sr.getStatus() == org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.WAITING_PARTS)) {
                sr.setStatus(org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.IN_PROGRESS);
                srChanged = true;
            } else if (saved.getStatus() == ServiceOrder.ServiceOrderStatus.WAITING_PARTS
                    && sr.getStatus() != org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.RESOLVED
                    && sr.getStatus() != org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.READY_FOR_DELIVERY
                    && sr.getStatus() != org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.DELIVERED
                    && sr.getStatus() != org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.CLOSED) {
                sr.setStatus(org.enterprise.crm.service.entity.ServiceRequest.ServiceRequestStatus.WAITING_PARTS);
                srChanged = true;
            }

            if (srChanged) {
                serviceRequestRepository.save(sr);
            }
        }

        return saved;
    }

    @Transactional
    public SalesInvoice generateCustomerBill(Long serviceOrderId) {
        ServiceOrder order = repository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Service Order not found: " + serviceOrderId));
        ServiceRequest request = order.getServiceRequest();
        if (request == null) {
            throw new RuntimeException("Service Order is not linked to a Service Request.");
        }
        return createOrGetServiceInvoice(request, order);
    }

    @Transactional
    public SalesInvoice generateCustomerBillForRequest(Long serviceRequestId) {
        ServiceRequest request = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new RuntimeException("Service Request not found: " + serviceRequestId));
        List<ServiceOrder> orders = repository.findByServiceRequestId(serviceRequestId);
        ServiceOrder primaryOrder = orders.isEmpty() ? null : orders.get(0);
        return createOrGetServiceInvoice(request, primaryOrder);
    }

    private SalesInvoice createOrGetServiceInvoice(ServiceRequest request, ServiceOrder order) {
        Long existingInvoiceId = order != null && order.getSalesInvoiceId() != null
                ? order.getSalesInvoiceId()
                : request.getSalesInvoiceId();
        if (existingInvoiceId != null) {
            var existingOpt = salesInvoiceService.findById(existingInvoiceId);
            if (existingOpt.isPresent()) {
                return existingOpt.get();
            }
        }

        if (request.getCustomer() == null) {
            throw new RuntimeException("Customer is required on the Service Request before generating a bill.");
        }

        Long companyId = request.getCompanyId() != null
                ? request.getCompanyId()
                : org.enterprise.common.util.TenantContext.getCompanyId();

        List<ServiceEstimate> estimates = serviceEstimateRepository.findByServiceRequestId(request.getId());
        ServiceEstimate targetEstimate = estimates.stream()
                .filter(e -> e.getStatus() == ServiceEstimate.EstimateStatus.APPROVED)
                .findFirst()
                .orElse(estimates.isEmpty() ? null : estimates.get(estimates.size() - 1));

        List<ServicePartsRequisition> requisitions = order != null
                ? partsRequisitionRepository.findByServiceOrderId(order.getId())
                : List.of();

        Warehouse targetWarehouse = requisitions.stream()
                .filter(r -> r.getWarehouse() != null && r.getStatus() != ServicePartsRequisition.RequisitionStatus.CANCELLED)
                .map(ServicePartsRequisition::getWarehouse)
                .findFirst()
                .orElseGet(() -> warehouseRepository.findAll().stream()
                        .filter(w -> companyId == null || companyId.equals(w.getCompanyId()))
                        .findFirst()
                        .orElse(null));

        SalesInvoice invoice = new SalesInvoice();
        invoice.setCompanyId(companyId);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(7));
        invoice.setStatus(SalesInvoice.InvoiceStatus.DRAFT);
        invoice.setInvoiceType(SalesInvoice.InvoiceType.INVOICE);
        invoice.setSalesChannel(SalesInvoice.SalesChannel.B2C);
        invoice.setCustomer(request.getCustomer());
        invoice.setWarehouse(targetWarehouse);
        invoice.setServiceRequestId(request.getId());
        if (order != null) {
            invoice.setServiceOrderId(order.getId());
        }

        StringBuilder remarks = new StringBuilder("Service Bill for Ticket ")
                .append(request.getRequestNumber() != null ? request.getRequestNumber() : ("#" + request.getId()));
        if (order != null && order.getOrderNumber() != null) {
            remarks.append(" / Job Card ").append(order.getOrderNumber());
        }
        if (request.getRegistrationNumber() != null && !request.getRegistrationNumber().isBlank()) {
            remarks.append(" | Reg/VIN: ").append(request.getRegistrationNumber());
        }
        if (request.getOdometerReading() != null) {
            remarks.append(" | Odometer: ").append(request.getOdometerReading()).append(" km");
        }
        invoice.setRemarks(remarks.toString());

        List<SalesInvoiceDetail> invoiceDetails = new ArrayList<>();
        Set<String> billedPartKeys = new HashSet<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        if (targetEstimate != null && targetEstimate.getDetails() != null) {
            for (ServiceEstimateDetail estLine : targetEstimate.getDetails()) {
                BigDecimal qty = estLine.getQuantity() != null && estLine.getQuantity().compareTo(BigDecimal.ZERO) > 0
                        ? estLine.getQuantity()
                        : BigDecimal.ONE;
                BigDecimal price = estLine.getUnitPrice() != null ? estLine.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal lineTotal = estLine.getLineTotal() != null && estLine.getLineTotal().compareTo(BigDecimal.ZERO) > 0
                        ? estLine.getLineTotal()
                        : qty.multiply(price);

                Product lineProduct = estLine.getProduct();
                if (lineProduct == null) {
                    lineProduct = resolveOrCreateServiceLaborProduct(companyId);
                } else {
                    Long vId = estLine.getVariant() != null ? estLine.getVariant().getId() : null;
                    billedPartKeys.add(lineProduct.getId() + ":" + (vId != null ? vId : "0"));
                }

                SalesInvoiceDetail invDetail = new SalesInvoiceDetail();
                invDetail.setSalesInvoice(invoice);
                invDetail.setCompanyId(companyId);
                invDetail.setProduct(lineProduct);
                invDetail.setVariant(estLine.getVariant());
                invDetail.setDescription(estLine.getDescription() != null && !estLine.getDescription().isBlank()
                        ? estLine.getDescription()
                        : (Boolean.FALSE.equals(estLine.getIsPart()) ? "Service Labor Charge" : lineProduct.getName()));
                invDetail.setQuantity(qty);
                invDetail.setUnitPrice(price);
                invDetail.setLineTotal(lineTotal);
                invDetail.setDiscountTotal(BigDecimal.ZERO);
                invDetail.setVatRate(BigDecimal.ZERO);
                invDetail.setVatTotal(BigDecimal.ZERO);
                invDetail.setIsVatInclusive(false);

                invoiceDetails.add(invDetail);
                subTotal = subTotal.add(lineTotal);
            }
        }

        // Also add any issued parts from Parts Requisitions that were not already in the estimate
        for (ServicePartsRequisition req : requisitions) {
            if (req.getStatus() == ServicePartsRequisition.RequisitionStatus.CANCELLED || req.getDetails() == null) {
                continue;
            }
            for (ServicePartsRequisitionDetail partDetail : req.getDetails()) {
                BigDecimal issuedQty = partDetail.getQuantityIssued() != null ? partDetail.getQuantityIssued() : BigDecimal.ZERO;
                if (issuedQty.compareTo(BigDecimal.ZERO) <= 0 || partDetail.getProduct() == null) {
                    continue;
                }
                Long pId = partDetail.getProduct().getId();
                Long vId = partDetail.getVariant() != null ? partDetail.getVariant().getId() : null;
                String key = pId + ":" + (vId != null ? vId : "0");
                if (billedPartKeys.contains(key)) {
                    continue;
                }
                billedPartKeys.add(key);

                Product partProd = partDetail.getProduct();
                BigDecimal unitPrice = partProd.getPrice() != null
                        ? BigDecimal.valueOf(partProd.getPrice())
                        : BigDecimal.ZERO;
                BigDecimal lineTotal = issuedQty.multiply(unitPrice);

                SalesInvoiceDetail invDetail = new SalesInvoiceDetail();
                invDetail.setSalesInvoice(invoice);
                invDetail.setCompanyId(companyId);
                invDetail.setProduct(partProd);
                invDetail.setVariant(partDetail.getVariant());
                invDetail.setDescription(partProd.getName());
                invDetail.setQuantity(issuedQty);
                invDetail.setUnitPrice(unitPrice);
                invDetail.setLineTotal(lineTotal);
                invDetail.setDiscountTotal(BigDecimal.ZERO);
                invDetail.setVatRate(BigDecimal.ZERO);
                invDetail.setVatTotal(BigDecimal.ZERO);
                invDetail.setIsVatInclusive(false);

                invoiceDetails.add(invDetail);
                subTotal = subTotal.add(lineTotal);
            }
        }

        if (invoiceDetails.isEmpty()) {
            Product serviceProduct = resolveOrCreateServiceLaborProduct(companyId);
            SalesInvoiceDetail defaultLaborLine = new SalesInvoiceDetail();
            defaultLaborLine.setSalesInvoice(invoice);
            defaultLaborLine.setCompanyId(companyId);
            defaultLaborLine.setProduct(serviceProduct);
            defaultLaborLine.setDescription("General Service & Inspection Charge");
            defaultLaborLine.setQuantity(BigDecimal.ONE);
            defaultLaborLine.setUnitPrice(BigDecimal.ZERO);
            defaultLaborLine.setLineTotal(BigDecimal.ZERO);
            defaultLaborLine.setDiscountTotal(BigDecimal.ZERO);
            defaultLaborLine.setVatRate(BigDecimal.ZERO);
            defaultLaborLine.setVatTotal(BigDecimal.ZERO);
            defaultLaborLine.setIsVatInclusive(false);
            invoiceDetails.add(defaultLaborLine);
        }

        invoice.setDetails(invoiceDetails);
        invoice.setSubTotal(subTotal);
        invoice.setVatTotal(BigDecimal.ZERO);

        if (Boolean.TRUE.equals(request.getWarrantyCovered()) && subTotal.compareTo(BigDecimal.ZERO) > 0) {
            SalesInvoiceDiscount warrantyDiscount = new SalesInvoiceDiscount();
            warrantyDiscount.setSalesInvoice(invoice);
            warrantyDiscount.setCompanyId(companyId);
            warrantyDiscount.setDiscountName("Warranty Coverage (100%)");
            warrantyDiscount.setDiscountAmount(subTotal);
            invoice.setDiscounts(List.of(warrantyDiscount));
            invoice.setDiscountTotal(subTotal);
            invoice.setTotalAmount(BigDecimal.ZERO);
        } else {
            invoice.setDiscountTotal(BigDecimal.ZERO);
            invoice.setTotalAmount(subTotal);
        }

        SalesInvoice savedInvoice = salesInvoiceService.save(invoice);

        request.setSalesInvoiceId(savedInvoice.getId());
        request.setSalesInvoiceNo(savedInvoice.getInvoiceNo());
        serviceRequestRepository.save(request);

        if (order != null) {
            order.setSalesInvoiceId(savedInvoice.getId());
            order.setSalesInvoiceNo(savedInvoice.getInvoiceNo());
            repository.save(order);
        }

        return savedInvoice;
    }

    private Product resolveOrCreateServiceLaborProduct(Long companyId) {
        String laborSku = "SRV-LABOR";
        if (companyId != null) {
            var existing = productRepository.findBySkuAndCompanyId(laborSku, companyId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        List<Product> serviceProducts = productRepository.findByProductType(ProductType.SERVICE);
        for (Product p : serviceProducts) {
            if (companyId == null || companyId.equals(p.getCompanyId())) {
                return p;
            }
        }
        Product laborProduct = new Product();
        laborProduct.setCompanyId(companyId);
        laborProduct.setSku(laborSku);
        laborProduct.setName("Service / Labor Charge");
        laborProduct.setProductType(ProductType.SERVICE);
        laborProduct.setInventoryItem(false);
        laborProduct.setPurchasable(false);
        laborProduct.setSaleable(true);
        laborProduct.setIsBatchManaged(false);
        laborProduct.setIsSerialManaged(false);
        return productRepository.save(laborProduct);
    }

    public Page<ServiceOrder> searchServiceOrders(Long companyId, String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return repository.searchServiceOrders(companyId, searchTerm.trim(), pageable);
        }
        return repository.findAllByCompanyId(companyId, pageable);
    }

}