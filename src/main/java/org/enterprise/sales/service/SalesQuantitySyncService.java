package org.enterprise.sales.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.dto.DeliveryNoteDetailDto;
import org.enterprise.sales.dto.DeliveryNoteDto;
import org.enterprise.sales.dto.SalesInvoiceDetailDto;
import org.enterprise.sales.dto.SalesInvoiceDto;
import org.enterprise.sales.dto.SalesOrderDetailDto;
import org.enterprise.sales.dto.SalesOrderDto;
import org.enterprise.sales.entity.DeliveryNote;
import org.enterprise.sales.entity.DeliveryNoteDetail;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.entity.SalesInvoiceDetail;
import org.enterprise.sales.entity.SalesOrder;
import org.enterprise.sales.entity.SalesOrderDetail;
import org.enterprise.sales.entity.SalesQuotation;
import org.enterprise.sales.entity.SalesQuotationDetail;
import org.enterprise.sales.repository.DeliveryNoteRepository;
import org.enterprise.sales.repository.SalesInvoiceRepository;
import org.enterprise.sales.repository.SalesOrderRepository;
import org.enterprise.sales.repository.SalesQuotationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SalesQuantitySyncService {

    private final SalesQuotationRepository salesQuotationRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;

    private static Long getProductId(org.enterprise.inventory.entity.Product p) {
        return p != null ? p.getId() : null;
    }

    private static Long getVariantId(org.enterprise.inventory.entity.ProductVariant v) {
        return v != null ? v.getId() : null;
    }

    private static boolean matchesProductAndVariant(
            Long productIdA, Long variantIdA,
            Long productIdB, Long variantIdB) {
        if (productIdA == null || productIdB == null || !productIdA.equals(productIdB)) {
            return false;
        }
        if (variantIdA != null && variantIdB != null) {
            return variantIdA.equals(variantIdB);
        }
        return variantIdA == null && variantIdB == null;
    }

    private static boolean matchesProductFallback(
            Long productIdA, Long variantIdA,
            Long productIdB, Long variantIdB) {
        if (matchesProductAndVariant(productIdA, variantIdA, productIdB, variantIdB)) {
            return true;
        }
        return productIdA != null && productIdA.equals(productIdB) && (variantIdA == null || variantIdB == null);
    }

    @Transactional
    public void syncQuotationById(Long quotationId) {
        if (quotationId == null) return;
        salesQuotationRepository.findById(quotationId).ifPresent(this::syncQuotationQuantities);
    }

    @Transactional
    public void syncQuotationQuantities(SalesQuotation quotation) {
        if (quotation == null || quotation.getId() == null || quotation.getDetails() == null) {
            return;
        }

        List<SalesOrder> linkedOrders = salesOrderRepository.findBySalesQuotationId(quotation.getId());
        List<SalesOrderDetail> activeOrderLines = new ArrayList<>();
        for (SalesOrder so : linkedOrders) {
            if (so.getStatus() == SalesOrder.SalesOrderStatus.CANCELLED) continue;
            if (so.getOrderType() == SalesOrder.OrderType.RETURN) continue;
            if (so.getDetails() != null) {
                activeOrderLines.addAll(so.getDetails());
            }
        }

        boolean changed = false;
        for (SalesQuotationDetail sqd : quotation.getDetails()) {
            Long sqProdId = getProductId(sqd.getProduct());
            Long sqVarId = getVariantId(sqd.getVariant());
            BigDecimal orderedSum = BigDecimal.ZERO;

            for (SalesOrderDetail sod : activeOrderLines) {
                Long soProdId = getProductId(sod.getProduct());
                Long soVarId = getVariantId(sod.getVariant());
                if (matchesProductFallback(sqProdId, sqVarId, soProdId, soVarId)) {
                    if (sod.getQuantity() != null) {
                        orderedSum = orderedSum.add(sod.getQuantity());
                    }
                }
            }

            BigDecimal currentOrdered = sqd.getOrderedQuantity() != null ? sqd.getOrderedQuantity() : BigDecimal.ZERO;
            if (currentOrdered.compareTo(orderedSum) != 0) {
                sqd.setOrderedQuantity(orderedSum);
                changed = true;
            }
        }

        if (changed) {
            salesQuotationRepository.save(quotation);
        }
    }

    @Transactional
    public void syncSalesOrderById(Long salesOrderId) {
        if (salesOrderId == null) return;
        salesOrderRepository.findById(salesOrderId).ifPresent(this::syncSalesOrderQuantities);
    }

    @Transactional
    public void syncSalesOrderQuantities(SalesOrder order) {
        if (order == null || order.getId() == null) {
            return;
        }

        if (order.getSalesQuotation() != null && order.getSalesQuotation().getId() != null) {
            syncQuotationById(order.getSalesQuotation().getId());
        }

        if (order.getOrderType() == SalesOrder.OrderType.RETURN) {
            if (order.getReferenceOrder() != null && order.getReferenceOrder().getId() != null) {
                syncSalesOrderById(order.getReferenceOrder().getId());
            }
            return;
        }

        if (order.getDetails() == null || order.getDetails().isEmpty()) {
            return;
        }

        List<DeliveryNote> deliveries = deliveryNoteRepository.findBySalesOrderId(order.getId());
        Map<Long, SalesInvoice> invoiceMap = new LinkedHashMap<>();
        for (SalesInvoice inv : salesInvoiceRepository.findBySalesOrderId(order.getId())) {
            if (inv.getId() != null && inv.getStatus() != SalesInvoice.InvoiceStatus.CANCELLED) {
                invoiceMap.put(inv.getId(), inv);
            }
        }
        for (DeliveryNote dn : deliveries) {
            if (dn.getId() != null) {
                for (SalesInvoice inv : salesInvoiceRepository.findByDeliveryNoteId(dn.getId())) {
                    if (inv.getId() != null && inv.getStatus() != SalesInvoice.InvoiceStatus.CANCELLED) {
                        invoiceMap.put(inv.getId(), inv);
                    }
                }
            }
        }
        List<SalesInvoice> activeInvoices = new ArrayList<>(invoiceMap.values());

        // Link any unlinked DeliveryNoteDetails to matching SalesOrderDetails & sync DeliveryNoteDetail.invoicedQuantity
        for (DeliveryNote dn : deliveries) {
            if (dn.getDetails() == null) continue;
            boolean dnChanged = false;
            for (DeliveryNoteDetail dnd : dn.getDetails()) {
                if (dnd.getSalesOrderDetail() == null) {
                    SalesOrderDetail matchedSod = findMatchingOrderDetail(order.getDetails(), null, getProductId(dnd.getProduct()), getVariantId(dnd.getVariant()));
                    if (matchedSod != null) {
                        dnd.setSalesOrderDetail(matchedSod);
                        dnChanged = true;
                    }
                }
                BigDecimal dndInvoiced = BigDecimal.ZERO;
                for (SalesInvoice inv : activeInvoices) {
                    if (inv.getDetails() == null) continue;
                    boolean isCreditMemo = inv.getInvoiceType() == SalesInvoice.InvoiceType.CREDIT_MEMO;
                    for (SalesInvoiceDetail sid : inv.getDetails()) {
                        boolean matched = false;
                        if (sid.getDeliveryNoteDetail() != null && sid.getDeliveryNoteDetail().getId() != null && dnd.getId() != null) {
                            matched = sid.getDeliveryNoteDetail().getId().equals(dnd.getId());
                        } else if (inv.getDeliveryNote() != null && inv.getDeliveryNote().getId() != null && dn.getId() != null
                                && inv.getDeliveryNote().getId().equals(dn.getId())) {
                            matched = matchesProductFallback(
                                    getProductId(dnd.getProduct()), getVariantId(dnd.getVariant()),
                                    getProductId(sid.getProduct()), getVariantId(sid.getVariant()));
                        }
                        if (matched && sid.getQuantity() != null) {
                            dndInvoiced = isCreditMemo ? dndInvoiced.subtract(sid.getQuantity()) : dndInvoiced.add(sid.getQuantity());
                        }
                    }
                }
                if (dndInvoiced.compareTo(BigDecimal.ZERO) < 0) {
                    dndInvoiced = BigDecimal.ZERO;
                }
                BigDecimal currentDndInvoiced = dnd.getInvoicedQuantity() != null ? dnd.getInvoicedQuantity() : BigDecimal.ZERO;
                if (currentDndInvoiced.compareTo(dndInvoiced) != 0) {
                    dnd.setInvoicedQuantity(dndInvoiced);
                    dnChanged = true;
                }
            }
            if (dnChanged) {
                deliveryNoteRepository.save(dn);
            }
        }

        // Load return sales orders referencing this order
        List<SalesOrder> returnOrders = salesOrderRepository.findByReferenceOrderId(order.getId());

        boolean orderChanged = false;
        for (SalesOrderDetail sod : order.getDetails()) {
            Long sodId = sod.getId();
            Long sodProdId = getProductId(sod.getProduct());
            Long sodVarId = getVariantId(sod.getVariant());

            // 1. Shipped quantity (from non-cancelled OUTBOUND delivery notes)
            BigDecimal shippedSum = BigDecimal.ZERO;
            // 2. Direct returned quantity (from non-cancelled INBOUND_RETURN delivery notes directly linked to this order)
            BigDecimal directReturnedSum = BigDecimal.ZERO;

            for (DeliveryNote dn : deliveries) {
                if (dn.getStatus() == DeliveryNote.DeliveryStatus.CANCELLED || dn.getDetails() == null) {
                    continue;
                }
                boolean isReturnDelivery = dn.getDeliveryType() == DeliveryNote.DeliveryType.INBOUND_RETURN;
                for (DeliveryNoteDetail dnd : dn.getDetails()) {
                    if (matchesOrderDetail(sod, dnd.getSalesOrderDetail() != null ? dnd.getSalesOrderDetail().getId() : null,
                            getProductId(dnd.getProduct()), getVariantId(dnd.getVariant()))) {
                        BigDecimal q = dnd.getQuantity() != null ? dnd.getQuantity() : BigDecimal.ZERO;
                        if (isReturnDelivery) {
                            directReturnedSum = directReturnedSum.add(q);
                        } else {
                            shippedSum = shippedSum.add(q);
                        }
                    }
                }
            }

            // 3. Returned quantity from Return Sales Orders (orderType == RETURN, referenceOrder == this order)
            BigDecimal returnOrderSum = BigDecimal.ZERO;
            for (SalesOrder ro : returnOrders) {
                if (ro.getStatus() == SalesOrder.SalesOrderStatus.CANCELLED) continue;
                if (ro.getOrderType() != SalesOrder.OrderType.RETURN) continue;

                BigDecimal roLineQty = BigDecimal.ZERO;
                if (ro.getDetails() != null) {
                    for (SalesOrderDetail rod : ro.getDetails()) {
                        if (matchesProductFallback(sodProdId, sodVarId, getProductId(rod.getProduct()), getVariantId(rod.getVariant()))) {
                            if (rod.getQuantity() != null) {
                                roLineQty = roLineQty.add(rod.getQuantity());
                            }
                        }
                    }
                }

                BigDecimal roInboundDeliveredQty = BigDecimal.ZERO;
                if (ro.getId() != null) {
                    List<DeliveryNote> roDeliveries = deliveryNoteRepository.findBySalesOrderId(ro.getId());
                    for (DeliveryNote rdn : roDeliveries) {
                        if (rdn.getStatus() == DeliveryNote.DeliveryStatus.CANCELLED || rdn.getDetails() == null) continue;
                        for (DeliveryNoteDetail rdnd : rdn.getDetails()) {
                            if (matchesProductFallback(sodProdId, sodVarId, getProductId(rdnd.getProduct()), getVariantId(rdnd.getVariant()))) {
                                if (rdnd.getQuantity() != null) {
                                    roInboundDeliveredQty = roInboundDeliveredQty.add(rdnd.getQuantity());
                                }
                            }
                        }
                    }
                }

                returnOrderSum = returnOrderSum.add(roLineQty.max(roInboundDeliveredQty));
            }

            BigDecimal totalReturned = directReturnedSum.add(returnOrderSum);

            // 4. Invoiced quantity (from non-cancelled SalesInvoices linked to this order or its deliveries)
            BigDecimal invoicedSum = BigDecimal.ZERO;
            for (SalesInvoice inv : activeInvoices) {
                if (inv.getDetails() == null) continue;
                boolean isCreditMemo = inv.getInvoiceType() == SalesInvoice.InvoiceType.CREDIT_MEMO;
                for (SalesInvoiceDetail sid : inv.getDetails()) {
                    Long linkedSodId = sid.getSalesOrderDetail() != null ? sid.getSalesOrderDetail().getId()
                            : (sid.getDeliveryNoteDetail() != null && sid.getDeliveryNoteDetail().getSalesOrderDetail() != null
                                ? sid.getDeliveryNoteDetail().getSalesOrderDetail().getId() : null);
                    if (matchesOrderDetail(sod, linkedSodId, getProductId(sid.getProduct()), getVariantId(sid.getVariant()))) {
                        BigDecimal q = sid.getQuantity() != null ? sid.getQuantity() : BigDecimal.ZERO;
                        invoicedSum = isCreditMemo ? invoicedSum.subtract(q) : invoicedSum.add(q);
                    }
                }
            }
            if (invoicedSum.compareTo(BigDecimal.ZERO) < 0) {
                invoicedSum = BigDecimal.ZERO;
            }

            // If invoices were created directly against the SalesOrder (without specifying DeliveryNote),
            // propagate invoiced quantity onto outbound DeliveryNoteDetails that still have invoicedQuantity == 0
            if (invoicedSum.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remainingToAllocate = invoicedSum;
                for (DeliveryNote dn : deliveries) {
                    if (dn.getStatus() == DeliveryNote.DeliveryStatus.CANCELLED
                            || dn.getDeliveryType() == DeliveryNote.DeliveryType.INBOUND_RETURN
                            || dn.getDetails() == null) {
                        continue;
                    }
                    boolean dnUpdated = false;
                    for (DeliveryNoteDetail dnd : dn.getDetails()) {
                        if (matchesOrderDetail(sod, dnd.getSalesOrderDetail() != null ? dnd.getSalesOrderDetail().getId() : null,
                                getProductId(dnd.getProduct()), getVariantId(dnd.getVariant()))) {
                            BigDecimal dndQty = dnd.getQuantity() != null ? dnd.getQuantity() : BigDecimal.ZERO;
                            BigDecimal alloc = remainingToAllocate.min(dndQty).max(BigDecimal.ZERO);
                            BigDecimal curr = dnd.getInvoicedQuantity() != null ? dnd.getInvoicedQuantity() : BigDecimal.ZERO;
                            if (curr.compareTo(alloc) < 0) {
                                dnd.setInvoicedQuantity(alloc);
                                dnUpdated = true;
                            }
                            remainingToAllocate = remainingToAllocate.subtract(alloc).max(BigDecimal.ZERO);
                        }
                    }
                    if (dnUpdated) {
                        deliveryNoteRepository.save(dn);
                    }
                }
            }

            BigDecimal currShipped = sod.getShippedQuantity() != null ? sod.getShippedQuantity() : BigDecimal.ZERO;
            BigDecimal currReturned = sod.getReturnedQuantity() != null ? sod.getReturnedQuantity() : BigDecimal.ZERO;
            BigDecimal currInvoiced = sod.getInvoicedQuantity() != null ? sod.getInvoicedQuantity() : BigDecimal.ZERO;

            if (currShipped.compareTo(shippedSum) != 0) {
                sod.setShippedQuantity(shippedSum);
                orderChanged = true;
            }
            if (currReturned.compareTo(totalReturned) != 0) {
                sod.setReturnedQuantity(totalReturned);
                orderChanged = true;
            }
            if (currInvoiced.compareTo(invoicedSum) != 0) {
                sod.setInvoicedQuantity(invoicedSum);
                orderChanged = true;
            }
        }

        if (orderChanged) {
            salesOrderRepository.save(order);
        }
    }

    @Transactional
    public void syncDeliveryNoteQuantities(DeliveryNote delivery) {
        if (delivery == null) return;
        if (delivery.getSalesOrder() != null && delivery.getSalesOrder().getId() != null) {
            syncSalesOrderById(delivery.getSalesOrder().getId());
        } else if (delivery.getId() != null && delivery.getDetails() != null) {
            List<SalesInvoice> invoices = salesInvoiceRepository.findByDeliveryNoteId(delivery.getId());
            boolean changed = false;
            for (DeliveryNoteDetail dnd : delivery.getDetails()) {
                BigDecimal invoiced = BigDecimal.ZERO;
                for (SalesInvoice inv : invoices) {
                    if (inv.getStatus() == SalesInvoice.InvoiceStatus.CANCELLED || inv.getDetails() == null) continue;
                    boolean isCreditMemo = inv.getInvoiceType() == SalesInvoice.InvoiceType.CREDIT_MEMO;
                    for (SalesInvoiceDetail sid : inv.getDetails()) {
                        boolean matched = (sid.getDeliveryNoteDetail() != null && dnd.getId() != null
                                && dnd.getId().equals(sid.getDeliveryNoteDetail().getId()))
                                || matchesProductFallback(
                                        getProductId(dnd.getProduct()), getVariantId(dnd.getVariant()),
                                        getProductId(sid.getProduct()), getVariantId(sid.getVariant()));
                        if (matched && sid.getQuantity() != null) {
                            invoiced = isCreditMemo ? invoiced.subtract(sid.getQuantity()) : invoiced.add(sid.getQuantity());
                        }
                    }
                }
                if (invoiced.compareTo(BigDecimal.ZERO) < 0) invoiced = BigDecimal.ZERO;
                BigDecimal curr = dnd.getInvoicedQuantity() != null ? dnd.getInvoicedQuantity() : BigDecimal.ZERO;
                if (curr.compareTo(invoiced) != 0) {
                    dnd.setInvoicedQuantity(invoiced);
                    changed = true;
                }
            }
            if (changed) {
                deliveryNoteRepository.save(delivery);
            }
        }
    }

    @Transactional
    public void enrichSalesOrderDto(SalesOrder entity, SalesOrderDto dto) {
        if (entity == null || dto == null) return;
        if (entity.getCustomer() != null) {
            dto.setCustomerName(entity.getCustomer().getName());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        if (entity.getOpportunity() != null) {
            dto.setOpportunityName(entity.getOpportunity().getName());
        }
        if (entity.getSalesQuotation() != null && entity.getSalesQuotation().getId() != null) {
            SalesQuotation sq = salesQuotationRepository.findById(entity.getSalesQuotation().getId()).orElse(entity.getSalesQuotation());
            dto.setSalesQuotationNo(sq.getQuotationNo());
            syncQuotationQuantities(sq);

            if (dto.getDetails() != null && sq.getDetails() != null) {
                List<SalesOrder> linkedOrders = salesOrderRepository.findBySalesQuotationId(sq.getId());
                for (SalesOrderDetailDto detailDto : dto.getDetails()) {
                    Long prodId = detailDto.getProductId();
                    Long varId = detailDto.getVariantId();
                    for (SalesQuotationDetail sqd : sq.getDetails()) {
                        if (matchesProductFallback(getProductId(sqd.getProduct()), getVariantId(sqd.getVariant()), prodId, varId)) {
                            BigDecimal quotedQty = sqd.getQuantity() != null ? sqd.getQuantity() : BigDecimal.ZERO;
                            BigDecimal otherOrdered = BigDecimal.ZERO;
                            for (SalesOrder otherSo : linkedOrders) {
                                if (otherSo.getStatus() == SalesOrder.SalesOrderStatus.CANCELLED) continue;
                                if (otherSo.getOrderType() == SalesOrder.OrderType.RETURN) continue;
                                if (entity.getId() != null && Objects.equals(otherSo.getId(), entity.getId())) continue;
                                if (otherSo.getDetails() != null) {
                                    for (SalesOrderDetail otherSod : otherSo.getDetails()) {
                                        if (matchesProductFallback(prodId, varId, getProductId(otherSod.getProduct()), getVariantId(otherSod.getVariant()))) {
                                            if (otherSod.getQuantity() != null) {
                                                otherOrdered = otherOrdered.add(otherSod.getQuantity());
                                            }
                                        }
                                    }
                                }
                            }
                            BigDecimal availQty = quotedQty.subtract(otherOrdered).max(BigDecimal.ZERO);
                            detailDto.setQuotationQuantity(quotedQty);
                            detailDto.setQuotationOrderedQuantity(otherOrdered);
                            detailDto.setQuotationAvailableQuantity(availQty);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Transactional
    public void enrichDeliveryNoteDto(DeliveryNote entity, DeliveryNoteDto dto) {
        if (entity == null || dto == null || dto.getDetails() == null) return;
        syncDeliveryNoteQuantities(entity);

        if (entity.getSalesOrder() != null && entity.getSalesOrder().getId() != null) {
            SalesOrder so = salesOrderRepository.findById(entity.getSalesOrder().getId()).orElse(entity.getSalesOrder());
            List<DeliveryNote> allDeliveries = deliveryNoteRepository.findBySalesOrderId(so.getId());
            boolean isInboundReturn = entity.getDeliveryType() == DeliveryNote.DeliveryType.INBOUND_RETURN;

            for (DeliveryNoteDetailDto detailDto : dto.getDetails()) {
                SalesOrderDetail sod = findMatchingOrderDetail(
                        so.getDetails(),
                        detailDto.getSalesOrderDetailId(),
                        detailDto.getProductId(),
                        detailDto.getVariantId()
                );
                if (sod != null) {
                    if (detailDto.getSalesOrderDetailId() == null) {
                        detailDto.setSalesOrderDetailId(sod.getId());
                    }
                    BigDecimal orderQty = sod.getQuantity() != null ? sod.getQuantity() : BigDecimal.ZERO;
                    BigDecimal prevDelivered = BigDecimal.ZERO;
                    if (!isInboundReturn) {
                        for (DeliveryNote otherDn : allDeliveries) {
                            if (otherDn.getStatus() == DeliveryNote.DeliveryStatus.CANCELLED) continue;
                            if (otherDn.getDeliveryType() == DeliveryNote.DeliveryType.INBOUND_RETURN) continue;
                            if (entity.getId() != null && Objects.equals(otherDn.getId(), entity.getId())) continue;
                            if (otherDn.getDetails() != null) {
                                for (DeliveryNoteDetail otherDnd : otherDn.getDetails()) {
                                    if (matchesOrderDetail(sod,
                                            otherDnd.getSalesOrderDetail() != null ? otherDnd.getSalesOrderDetail().getId() : null,
                                            getProductId(otherDnd.getProduct()),
                                            getVariantId(otherDnd.getVariant()))) {
                                        if (otherDnd.getQuantity() != null) {
                                            prevDelivered = prevDelivered.add(otherDnd.getQuantity());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    BigDecimal remaining = isInboundReturn
                            ? orderQty
                            : orderQty.subtract(prevDelivered).max(BigDecimal.ZERO);

                    detailDto.setSourceQuantity(orderQty);
                    detailDto.setPreviouslyDeliveredQuantity(prevDelivered);
                    detailDto.setRemainingQuantity(remaining);

                    BigDecimal dndInv = detailDto.getInvoicedQuantity() != null ? detailDto.getInvoicedQuantity() : BigDecimal.ZERO;
                    BigDecimal sodInv = sod.getInvoicedQuantity() != null ? sod.getInvoicedQuantity() : BigDecimal.ZERO;
                    if (dndInv.compareTo(BigDecimal.ZERO) == 0 && sodInv.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal dndQty = detailDto.getQuantity() != null ? detailDto.getQuantity() : orderQty;
                        detailDto.setInvoicedQuantity(sodInv.min(dndQty));
                    } else if (detailDto.getInvoicedQuantity() == null) {
                        detailDto.setInvoicedQuantity(BigDecimal.ZERO);
                    }
                }
            }
        }
    }

    @Transactional
    public void enrichSalesInvoiceDto(SalesInvoice entity, SalesInvoiceDto dto) {
        if (entity == null || dto == null || dto.getDetails() == null) return;

        Long soId = entity.getSalesOrder() != null ? entity.getSalesOrder().getId()
                : (entity.getDeliveryNote() != null && entity.getDeliveryNote().getSalesOrder() != null
                    ? entity.getDeliveryNote().getSalesOrder().getId() : null);

        if (soId != null) {
            syncSalesOrderById(soId);
            SalesOrder so = salesOrderRepository.findById(soId).orElse(null);
            if (so != null && so.getDetails() != null) {
                Map<Long, SalesInvoice> otherInvoicesMap = new LinkedHashMap<>();
                for (SalesInvoice inv : salesInvoiceRepository.findBySalesOrderId(soId)) {
                    if (inv.getId() != null && inv.getStatus() != SalesInvoice.InvoiceStatus.CANCELLED
                            && !Objects.equals(inv.getId(), entity.getId())) {
                        otherInvoicesMap.put(inv.getId(), inv);
                    }
                }
                if (entity.getDeliveryNote() != null && entity.getDeliveryNote().getId() != null) {
                    for (SalesInvoice inv : salesInvoiceRepository.findByDeliveryNoteId(entity.getDeliveryNote().getId())) {
                        if (inv.getId() != null && inv.getStatus() != SalesInvoice.InvoiceStatus.CANCELLED
                                && !Objects.equals(inv.getId(), entity.getId())) {
                            otherInvoicesMap.put(inv.getId(), inv);
                        }
                    }
                }

                for (SalesInvoiceDetailDto detailDto : dto.getDetails()) {
                    SalesOrderDetail sod = findMatchingOrderDetail(
                            so.getDetails(),
                            detailDto.getSalesOrderDetailId(),
                            detailDto.getProductId(),
                            detailDto.getVariantId()
                    );
                    if (sod != null) {
                        if (detailDto.getSalesOrderDetailId() == null) {
                            detailDto.setSalesOrderDetailId(sod.getId());
                        }
                        BigDecimal sourceQty = sod.getQuantity() != null ? sod.getQuantity() : BigDecimal.ZERO;
                        if (entity.getDeliveryNote() != null && entity.getDeliveryNote().getDetails() != null) {
                            for (DeliveryNoteDetail dnd : entity.getDeliveryNote().getDetails()) {
                                if ((detailDto.getDeliveryNoteDetailId() != null && Objects.equals(dnd.getId(), detailDto.getDeliveryNoteDetailId()))
                                        || matchesProductFallback(detailDto.getProductId(), detailDto.getVariantId(),
                                                getProductId(dnd.getProduct()), getVariantId(dnd.getVariant()))) {
                                    if (dnd.getQuantity() != null) {
                                        sourceQty = dnd.getQuantity();
                                    }
                                    break;
                                }
                            }
                        }

                        BigDecimal previouslyInvoiced = BigDecimal.ZERO;
                        for (SalesInvoice otherInv : otherInvoicesMap.values()) {
                            if (otherInv.getDetails() == null) continue;
                            boolean isCreditMemo = otherInv.getInvoiceType() == SalesInvoice.InvoiceType.CREDIT_MEMO;
                            for (SalesInvoiceDetail otherSid : otherInv.getDetails()) {
                                Long otherSodId = otherSid.getSalesOrderDetail() != null ? otherSid.getSalesOrderDetail().getId() : null;
                                if (matchesOrderDetail(sod, otherSodId, getProductId(otherSid.getProduct()), getVariantId(otherSid.getVariant()))) {
                                    BigDecimal q = otherSid.getQuantity() != null ? otherSid.getQuantity() : BigDecimal.ZERO;
                                    previouslyInvoiced = isCreditMemo ? previouslyInvoiced.subtract(q) : previouslyInvoiced.add(q);
                                }
                            }
                        }
                        if (previouslyInvoiced.compareTo(BigDecimal.ZERO) < 0) {
                            previouslyInvoiced = BigDecimal.ZERO;
                        }
                        BigDecimal availableQty = sourceQty.subtract(previouslyInvoiced).max(BigDecimal.ZERO);

                        detailDto.setSourceQuantity(sourceQty);
                        detailDto.setInvoicedQuantity(previouslyInvoiced);
                        detailDto.setAvailableQuantity(availableQty);
                    }
                }
            }
        }
    }

    public SalesOrderDetail findMatchingOrderDetail(
            List<SalesOrderDetail> orderDetails,
            Long salesOrderDetailId,
            Long productId,
            Long variantId) {
        if (orderDetails == null || orderDetails.isEmpty()) return null;
        if (salesOrderDetailId != null) {
            for (SalesOrderDetail sod : orderDetails) {
                if (sod.getId() != null && sod.getId().equals(salesOrderDetailId)) {
                    return sod;
                }
            }
        }
        if (productId != null) {
            for (SalesOrderDetail sod : orderDetails) {
                if (matchesProductAndVariant(getProductId(sod.getProduct()), getVariantId(sod.getVariant()), productId, variantId)) {
                    return sod;
                }
            }
            for (SalesOrderDetail sod : orderDetails) {
                if (matchesProductFallback(getProductId(sod.getProduct()), getVariantId(sod.getVariant()), productId, variantId)) {
                    return sod;
                }
            }
        }
        return null;
    }

    private boolean matchesOrderDetail(
            SalesOrderDetail sod,
            Long candidateOrderDetailId,
            Long candidateProductId,
            Long candidateVariantId) {
        if (sod == null) return false;
        if (candidateOrderDetailId != null && sod.getId() != null) {
            return sod.getId().equals(candidateOrderDetailId);
        }
        return matchesProductFallback(
                getProductId(sod.getProduct()),
                getVariantId(sod.getVariant()),
                candidateProductId,
                candidateVariantId
        );
    }
}
