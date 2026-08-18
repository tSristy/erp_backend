package org.enterprise.inventory.service;

import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalEntryService;
import org.enterprise.inventory.dto.GoodsReceiptLineDto;
import org.enterprise.inventory.dto.GoodsReceiptRequestDto;
import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.enums.GoodsReceiptStatus;
import org.enterprise.inventory.enums.InventoryTransactionType;
import org.enterprise.inventory.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GoodsReceiptService extends BaseService<GoodsReceipt, Long> {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final BusinessPartnerRepository businessPartnerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final JournalEntryService journalEntryService;
    private final CostingService costingService;
    private final BatchSerialTrackingService batchSerialTrackingService;

    public GoodsReceiptService(
            GoodsReceiptRepository goodsReceiptRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            BusinessPartnerRepository businessPartnerRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryLedgerRepository inventoryLedgerRepository,
            StockBalanceRepository stockBalanceRepository,
            JournalEntryService journalEntryService,
            CostingService costingService,
            BatchSerialTrackingService batchSerialTrackingService) {
        super(goodsReceiptRepository);
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.businessPartnerRepository = businessPartnerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryLedgerRepository = inventoryLedgerRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.journalEntryService = journalEntryService;
        this.costingService = costingService;
        this.batchSerialTrackingService = batchSerialTrackingService;
    }

    @Override
    @Transactional
    public GoodsReceipt save(GoodsReceipt receipt) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (receipt.getCompanyId() == null) {
            receipt.setCompanyId(companyId);
        }
        
        if (receipt.getDetails() != null) {
            for (GoodsReceiptDetail detail : receipt.getDetails()) {
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
                detail.setGoodsReceipt(receipt);
            }
        }
        return super.save(receipt);
    }

    /**
     * CREATE GOODS RECEIPT
     */
    @Transactional
    public GoodsReceipt create(GoodsReceiptRequestDto request) {

        validateCreateRequest(request);

        GoodsReceipt goodsReceipt = new GoodsReceipt();
        goodsReceipt.setGrnNo("GRN-" + System.currentTimeMillis());
        goodsReceipt.setGrnDate(
                request.getGrnDate() != null
                        ? request.getGrnDate()
                        : LocalDate.now()
        );

        BusinessPartner vendor = businessPartnerRepository.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        goodsReceipt.setVendor(vendor);
        goodsReceipt.setWarehouse(warehouse);

        if (request.getPurchaseOrderId() != null) {
            PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                    .orElseThrow(() -> new RuntimeException("PO not found"));
            goodsReceipt.setPurchaseOrder(po);
        }

        goodsReceipt.setStatus(GoodsReceiptStatus.DRAFT);

        List<GoodsReceiptDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (GoodsReceiptLineDto dto : request.getDetails()) {
            GoodsReceiptDetail detail = new GoodsReceiptDetail();
            detail.setGoodsReceipt(goodsReceipt);

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            detail.setProduct(product);
            detail.setQuantity(dto.getQuantity());
            detail.setUnitCost(dto.getUnitCost());

            Batch batch = batchSerialTrackingService.findOrCreateBatch(product, dto.getBatchNo(), dto.getManufactureDate(), dto.getExpiryDate());
            detail.setBatch(batch);

            batchSerialTrackingService.validateSerialNumbers(product, dto.getSerialNumbers(), dto.getQuantity().intValue());
            if (dto.getSerialNumbers() != null) {
                detail.setSerialNumbers(dto.getSerialNumbers());
            }

            BigDecimal lineAmount = dto.getQuantity().multiply(dto.getUnitCost());
            detail.setLineTotal(lineAmount);

            totalAmount = totalAmount.add(lineAmount);
            details.add(detail);
        }

        goodsReceipt.setDetails(details);
        goodsReceipt.setTotalAmount(totalAmount);

        return this.save(goodsReceipt);
    }

    /**
     * UPDATE GOODS RECEIPT
     */
    @Transactional
    public GoodsReceipt update(Long id, GoodsReceiptRequestDto request) {
        GoodsReceipt goodsReceipt = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goods Receipt not found"));

        if (goodsReceipt.getStatus() != GoodsReceiptStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT receipts can be updated");
        }

        validateCreateRequest(request);

        BusinessPartner vendor = businessPartnerRepository.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        goodsReceipt.setVendor(vendor);
        goodsReceipt.setWarehouse(warehouse);
        goodsReceipt.setGrnDate(request.getGrnDate() != null ? request.getGrnDate() : LocalDate.now());

        if (request.getPurchaseOrderId() != null) {
            PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                    .orElseThrow(() -> new RuntimeException("PO not found"));
            goodsReceipt.setPurchaseOrder(po);
        } else {
            goodsReceipt.setPurchaseOrder(null);
        }

        goodsReceipt.getDetails().clear();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (GoodsReceiptLineDto dto : request.getDetails()) {
            GoodsReceiptDetail detail = new GoodsReceiptDetail();
            detail.setGoodsReceipt(goodsReceipt);

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            detail.setProduct(product);
            detail.setQuantity(dto.getQuantity());
            detail.setUnitCost(dto.getUnitCost());

            Batch batch = batchSerialTrackingService.findOrCreateBatch(product, dto.getBatchNo(), dto.getManufactureDate(), dto.getExpiryDate());
            detail.setBatch(batch);

            batchSerialTrackingService.validateSerialNumbers(product, dto.getSerialNumbers(), dto.getQuantity().intValue());
            if (dto.getSerialNumbers() != null) {
                detail.setSerialNumbers(dto.getSerialNumbers());
            }

            BigDecimal lineAmount = dto.getQuantity().multiply(dto.getUnitCost());
            detail.setLineTotal(lineAmount);

            totalAmount = totalAmount.add(lineAmount);
            goodsReceipt.getDetails().add(detail);
        }

        goodsReceipt.setTotalAmount(totalAmount);

        return this.save(goodsReceipt);
    }

    /**
     * POST GOODS RECEIPT
     */
    @Transactional
    public GoodsReceipt post(Long goodsReceiptId) {

        GoodsReceipt goodsReceipt = goodsReceiptRepository.findById(goodsReceiptId)
                .orElseThrow(() -> new RuntimeException("Goods Receipt not found"));

        if (goodsReceipt.getStatus() == GoodsReceiptStatus.POSTED) {
            throw new RuntimeException("Goods Receipt already posted");
        }

        validateAccounts(goodsReceipt);

        boolean isInbound = goodsReceipt.getReceiptType() == GoodsReceipt.ReceiptType.INBOUND_RECEIPT;
        
        // updateStockBalance updates detail.lineTotal for OUTBOUND_RETURN, so call it first.
        updateStockBalance(goodsReceipt, isInbound);
        createInventoryLedger(goodsReceipt, isInbound);
        createAccountingEntry(goodsReceipt, isInbound);

        goodsReceipt.setStatus(GoodsReceiptStatus.POSTED);

        return this.save(goodsReceipt);
    }

    /**
     * CREATE INVENTORY LEDGER
     */
    private void createInventoryLedger(GoodsReceipt goodsReceipt, boolean isInbound) {
        for (GoodsReceiptDetail detail : goodsReceipt.getDetails()) {
            InventoryLedger ledger = new InventoryLedger();
            ledger.setCompanyId(goodsReceipt.getCompanyId());
            ledger.setTransactionType(isInbound ? InventoryTransactionType.GRN : InventoryTransactionType.PURCHASE_RETURN);
            ledger.setDocumentId(goodsReceipt.getId());
            ledger.setDocumentType("GRN");
            ledger.setTransactionDate(LocalDateTime.now());
            ledger.setWarehouse(goodsReceipt.getWarehouse());
            ledger.setProduct(detail.getProduct());
            ledger.setBatch(detail.getBatch());
            
            ledger.setQtyIn(isInbound ? detail.getQuantity() : BigDecimal.ZERO);
            ledger.setQtyOut(isInbound ? BigDecimal.ZERO : detail.getQuantity());
            ledger.setUnitCost(detail.getUnitCost());
            ledger.setTotalCost(detail.getLineTotal());

            StockBalance currentStock;
            if (detail.getBatch() != null) {
                currentStock = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                                detail.getProduct().getId(),
                                goodsReceipt.getWarehouse().getId(),
                                null,
                                detail.getBatch().getId()
                        )
                        .orElseGet(StockBalance::new);
            } else {
                currentStock = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
                                detail.getProduct().getId(),
                                goodsReceipt.getWarehouse().getId(),
                                null
                        )
                        .orElseGet(StockBalance::new);
            }

            BigDecimal currentQty = Optional.ofNullable(currentStock.getQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal currentTotalValue = Optional.ofNullable(currentStock.getTotalValue()).orElse(BigDecimal.ZERO);

            ledger.setBalanceQty(currentQty);
            ledger.setBalanceCost(currentTotalValue);

            inventoryLedgerRepository.save(ledger);
        }
    }

    /**
     * UPDATE STOCK BALANCE
     */
    private void updateStockBalance(GoodsReceipt goodsReceipt, boolean isInbound) {
        BigDecimal updatedTotalAmount = BigDecimal.ZERO;
        
        for (GoodsReceiptDetail detail : goodsReceipt.getDetails()) {
            StockBalance stockBalance;
            if (detail.getBatch() != null) {
                stockBalance = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                                detail.getProduct().getId(),
                                goodsReceipt.getWarehouse().getId(),
                                null,
                                detail.getBatch().getId()
                        )
                        .orElseGet(() -> isInbound ? new StockBalance() : null);
            } else {
                stockBalance = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
                                detail.getProduct().getId(),
                                goodsReceipt.getWarehouse().getId(),
                                null
                        )
                        .orElseGet(() -> isInbound ? new StockBalance() : null);
            }

            if (stockBalance == null && !isInbound) {
                throw new RuntimeException("Insufficient stock for product " + detail.getProduct().getName());
            }

            if (isInbound && stockBalance.getProduct() == null) {
                stockBalance.setCompanyId(goodsReceipt.getCompanyId());
                stockBalance.setProduct(detail.getProduct());
                stockBalance.setWarehouse(goodsReceipt.getWarehouse());
                stockBalance.setBatch(detail.getBatch());
            }

            BigDecimal currentQty = Optional.ofNullable(stockBalance.getQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal currentTotalValue = Optional.ofNullable(stockBalance.getTotalValue()).orElse(BigDecimal.ZERO);

            if (!isInbound && currentQty.compareTo(detail.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient stock for product " + detail.getProduct().getName());
            }

            BigDecimal unitCost;
            BigDecimal lineTotal;
            BigDecimal newQty;
            BigDecimal newTotalValue;

            if (isInbound) {
                unitCost = detail.getUnitCost();
                lineTotal = detail.getLineTotal();
                newQty = currentQty.add(detail.getQuantity());
                newTotalValue = currentTotalValue.add(lineTotal);
                costingService.addCostLayer(detail.getProduct(), goodsReceipt.getWarehouse(), "GRN", goodsReceipt.getId(), detail.getQuantity(), unitCost);
            } else {
                lineTotal = costingService.consumeCost(detail.getProduct(), goodsReceipt.getWarehouse(), detail.getQuantity());
                unitCost = lineTotal.divide(detail.getQuantity(), 6, RoundingMode.HALF_UP);
                newQty = currentQty.subtract(detail.getQuantity());
                newTotalValue = currentTotalValue.subtract(lineTotal);
                
                detail.setUnitCost(unitCost);
                detail.setLineTotal(lineTotal);
            }
            
            updatedTotalAmount = updatedTotalAmount.add(lineTotal);

            stockBalance.setQuantity(newQty);
            stockBalance.setTotalValue(newTotalValue);

            if (newQty.compareTo(BigDecimal.ZERO) > 0 && isInbound) {
                stockBalance.setAverageCost(newTotalValue.divide(newQty, 6, RoundingMode.HALF_UP));
            } else if (newQty.compareTo(BigDecimal.ZERO) == 0) {
                stockBalance.setAverageCost(BigDecimal.ZERO);
                stockBalance.setTotalValue(BigDecimal.ZERO);
            }

            stockBalanceRepository.save(stockBalance);

            if (isInbound) {
                batchSerialTrackingService.processInboundSerials(detail.getProduct(), detail.getBatch(), detail.getSerialNumbers(), goodsReceipt.getWarehouse(), null, InventoryTransactionType.GRN, "GRN", goodsReceipt.getId());
            } else {
                batchSerialTrackingService.processOutboundSerials(detail.getProduct(), detail.getBatch(), detail.getSerialNumbers(), goodsReceipt.getWarehouse(), null, SerialNumber.SerialStatus.RETURNED, InventoryTransactionType.PURCHASE_RETURN, "GRN", goodsReceipt.getId());
            }
            
            if (detail.getPurchaseOrderDetail() != null) {
                if (isInbound) {
                    detail.getPurchaseOrderDetail().setReceivedQty(
                        detail.getPurchaseOrderDetail().getReceivedQty().add(detail.getQuantity())
                    );
                } else {
                    detail.getPurchaseOrderDetail().setReturnedQty(
                        detail.getPurchaseOrderDetail().getReturnedQty().add(detail.getQuantity())
                    );
                }
            }
        }
        goodsReceipt.setTotalAmount(updatedTotalAmount);
    }

    /**
     * CREATE ACCOUNTING ENTRY
     */
    private void createAccountingEntry(GoodsReceipt goodsReceipt, boolean isInbound) {
        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("GRN");
        journal.setReferenceId(goodsReceipt.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();
        BigDecimal totalAmount = goodsReceipt.getTotalAmount();

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journal);
        debitLine.setAccount(isInbound ? goodsReceipt.getWarehouse().getInventoryAccount() : goodsReceipt.getVendor().getVendorDetail().getGrnClearingAccount());
        debitLine.setDebit(totalAmount);
        debitLine.setCredit(BigDecimal.ZERO);
        if (!isInbound) {
            debitLine.setBusinessPartner(goodsReceipt.getVendor());
        }
        lines.add(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journal);
        creditLine.setAccount(isInbound ? goodsReceipt.getVendor().getVendorDetail().getGrnClearingAccount() : goodsReceipt.getWarehouse().getInventoryAccount());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(totalAmount);
        if (isInbound) {
            creditLine.setBusinessPartner(goodsReceipt.getVendor());
        }
        lines.add(creditLine);

        journal.setLines(lines);
        journalEntryService.save(journal);
    }

    /**
     * VALIDATE CREATE REQUEST
     */
    private void validateCreateRequest(GoodsReceiptRequestDto request) {
        if (request.getVendorId() == null) {
            throw new RuntimeException("Vendor is required");
        }

        if (request.getWarehouseId() == null) {
            throw new RuntimeException("Warehouse is required");
        }

        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new RuntimeException("Goods receipt details required");
        }
    }

    /**
     * VALIDATE ACCOUNT SETUP
     */
    private void validateAccounts(GoodsReceipt goodsReceipt) {
        if (goodsReceipt.getWarehouse().getInventoryAccount() == null) {
            throw new RuntimeException("Inventory account missing for warehouse: " + goodsReceipt.getWarehouse().getName());
        }

        if (goodsReceipt.getVendor() == null || goodsReceipt.getVendor().getVendorDetail() == null || goodsReceipt.getVendor().getVendorDetail().getGrnClearingAccount() == null) {
            throw new RuntimeException("GRN clearing account missing for vendor: " + goodsReceipt.getVendor().getName());
        }
    }

    @Transactional
    public GoodsReceipt createReturn(Long originalReceiptId) {
        GoodsReceipt original = goodsReceiptRepository.findById(originalReceiptId)
                .orElseThrow(() -> new RuntimeException("Original receipt not found"));

        GoodsReceipt returnReceipt = new GoodsReceipt();
        returnReceipt.setReceiptType(GoodsReceipt.ReceiptType.OUTBOUND_RETURN);
        returnReceipt.setPurchaseOrder(original.getPurchaseOrder());
        returnReceipt.setVendor(original.getVendor());
        returnReceipt.setWarehouse(original.getWarehouse());
        returnReceipt.setStatus(GoodsReceiptStatus.DRAFT);
        returnReceipt.setGrnDate(LocalDate.now());

        List<GoodsReceiptDetail> returnDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (GoodsReceiptDetail originalDetail : original.getDetails()) {
            GoodsReceiptDetail returnDetail = new GoodsReceiptDetail();
            returnDetail.setGoodsReceipt(returnReceipt);
            returnDetail.setPurchaseOrderDetail(originalDetail.getPurchaseOrderDetail());
            returnDetail.setProduct(originalDetail.getProduct());
            returnDetail.setQuantity(originalDetail.getQuantity());
            returnDetail.setUnitCost(originalDetail.getUnitCost());
            returnDetail.setLineTotal(originalDetail.getLineTotal());
            returnDetail.setBatch(originalDetail.getBatch());
            returnDetail.setSerialNumbers(originalDetail.getSerialNumbers());
            totalAmount = totalAmount.add(originalDetail.getLineTotal());
            returnDetails.add(returnDetail);
        }
        returnReceipt.setDetails(returnDetails);
        returnReceipt.setTotalAmount(totalAmount);

        return this.save(returnReceipt);
    }
}