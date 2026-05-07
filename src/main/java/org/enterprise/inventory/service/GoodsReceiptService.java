package org.enterprise.inventory.service;

import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalService;
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
    private final JournalService journalService;
    private final CostingService costingService;

    public GoodsReceiptService(
            GoodsReceiptRepository goodsReceiptRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            BusinessPartnerRepository businessPartnerRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryLedgerRepository inventoryLedgerRepository,
            StockBalanceRepository stockBalanceRepository,
            JournalService journalService,
            CostingService costingService) {
        super(goodsReceiptRepository);
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.businessPartnerRepository = businessPartnerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryLedgerRepository = inventoryLedgerRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.journalService = journalService;
        this.costingService = costingService;
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

            BigDecimal lineAmount = dto.getQuantity().multiply(dto.getUnitCost());
            detail.setLineTotal(lineAmount);

            totalAmount = totalAmount.add(lineAmount);
            details.add(detail);
        }

        goodsReceipt.setDetails(details);
        goodsReceipt.setTotalAmount(totalAmount);

        return goodsReceiptRepository.save(goodsReceipt);
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

        return goodsReceiptRepository.save(goodsReceipt);
    }

    /**
     * CREATE INVENTORY LEDGER
     */
    private void createInventoryLedger(GoodsReceipt goodsReceipt, boolean isInbound) {
        for (GoodsReceiptDetail detail : goodsReceipt.getDetails()) {
            InventoryLedger ledger = new InventoryLedger();
            ledger.setTransactionType(isInbound ? InventoryTransactionType.GRN : InventoryTransactionType.PURCHASE_RETURN);
            ledger.setDocumentId(goodsReceipt.getId());
            ledger.setDocumentType("GRN");
            ledger.setTransactionDate(LocalDateTime.now());
            ledger.setWarehouse(goodsReceipt.getWarehouse());
            ledger.setProduct(detail.getProduct());
            
            ledger.setQtyIn(isInbound ? detail.getQuantity() : BigDecimal.ZERO);
            ledger.setQtyOut(isInbound ? BigDecimal.ZERO : detail.getQuantity());
            ledger.setUnitCost(detail.getUnitCost());
            ledger.setTotalCost(detail.getLineTotal());

            StockBalance currentStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationId(
                            detail.getProduct().getId(),
                            goodsReceipt.getWarehouse().getId(),
                            null
                    )
                    .orElseGet(StockBalance::new);

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
            StockBalance stockBalance = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationId(
                            detail.getProduct().getId(),
                            goodsReceipt.getWarehouse().getId(),
                            null
                    )
                    .orElseGet(() -> isInbound ? new StockBalance() : null);

            if (stockBalance == null && !isInbound) {
                throw new RuntimeException("Insufficient stock for product " + detail.getProduct().getName());
            }

            if (isInbound && stockBalance.getProduct() == null) {
                stockBalance.setProduct(detail.getProduct());
                stockBalance.setWarehouse(goodsReceipt.getWarehouse());
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
        debitLine.setAccount(isInbound ? goodsReceipt.getWarehouse().getInventoryAccount() : goodsReceipt.getVendor().getGrnClearingAccount());
        debitLine.setDebit(totalAmount);
        debitLine.setCredit(BigDecimal.ZERO);
        lines.add(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journal);
        creditLine.setAccount(isInbound ? goodsReceipt.getVendor().getGrnClearingAccount() : goodsReceipt.getWarehouse().getInventoryAccount());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(totalAmount);
        lines.add(creditLine);

        journal.setLines(lines);
        journalService.save(journal);
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

        if (goodsReceipt.getVendor().getGrnClearingAccount() == null) {
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
            totalAmount = totalAmount.add(originalDetail.getLineTotal());
            returnDetails.add(returnDetail);
        }
        returnReceipt.setDetails(returnDetails);
        returnReceipt.setTotalAmount(totalAmount);

        return goodsReceiptRepository.save(returnReceipt);
    }
}