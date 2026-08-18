package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.enums.InventoryTransactionType;
import org.enterprise.inventory.repository.BatchRepository;
import org.enterprise.inventory.repository.SerialNumberRepository;
import org.enterprise.inventory.repository.SerialNumberTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchSerialTrackingService {

    private final BatchRepository batchRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final SerialNumberTransactionRepository serialNumberTransactionRepository;

    public Batch findOrCreateBatch(Product product, String batchNo, LocalDate mfgDate, LocalDate expDate) {
        if (!product.getIsBatchManaged()) {
            return null;
        }
        if (batchNo == null || batchNo.trim().isEmpty()) {
            throw new RuntimeException("Batch number is required for batch managed product: " + product.getName());
        }

        return batchRepository.findByBatchNoAndProductId(batchNo, product.getId())
                .orElseGet(() -> {
                    Batch newBatch = new Batch();
                    newBatch.setBatchNo(batchNo);
                    newBatch.setProduct(product);
                    newBatch.setManufactureDate(mfgDate);
                    newBatch.setExpiryDate(expDate);
                    newBatch.setActive(true);
                    newBatch.setCompanyId(org.enterprise.common.util.TenantContext.getCompanyId());
                    return batchRepository.save(newBatch);
                });
    }

    public void validateSerialNumbers(Product product, List<String> serialNumbers, int expectedQuantity) {
        if (!product.getIsSerialManaged()) {
            return;
        }
        if (serialNumbers == null || serialNumbers.size() != expectedQuantity) {
            throw new RuntimeException("Provided serial numbers count (" + (serialNumbers == null ? 0 : serialNumbers.size()) +
                    ") does not match quantity (" + expectedQuantity + ") for product: " + product.getName());
        }
    }

    public void processInboundSerials(Product product, Batch batch, List<String> serialNumbers, Warehouse warehouse, Location location, InventoryTransactionType type, String docType, Long docId) {
        if (!product.getIsSerialManaged() || serialNumbers == null || serialNumbers.isEmpty()) {
            return;
        }

        for (String sNo : serialNumbers) {
            SerialNumber serial = serialNumberRepository.findBySerialNoAndProductId(sNo, product.getId())
                    .orElseGet(() -> {
                        SerialNumber newSerial = new SerialNumber();
                        newSerial.setSerialNo(sNo);
                        newSerial.setProduct(product);
                        newSerial.setCompanyId(org.enterprise.common.util.TenantContext.getCompanyId());
                        return newSerial;
                    });

            if (serial.getStatus() == SerialNumber.SerialStatus.IN_STOCK && serial.getId() != null) {
                throw new RuntimeException("Serial number " + sNo + " is already IN_STOCK");
            }

            serial.setBatch(batch);
            serial.setWarehouse(warehouse);
            serial.setLocation(location);
            serial.setStatus(SerialNumber.SerialStatus.IN_STOCK);
            serialNumberRepository.save(serial);

            logTransaction(serial, type, docType, docId, warehouse, location);
        }
    }

    public void processOutboundSerials(Product product, Batch batch, List<String> serialNumbers, Warehouse warehouse, Location location, SerialNumber.SerialStatus newStatus, InventoryTransactionType type, String docType, Long docId) {
        if (!product.getIsSerialManaged() || serialNumbers == null || serialNumbers.isEmpty()) {
            return;
        }

        for (String sNo : serialNumbers) {
            SerialNumber serial = serialNumberRepository.findBySerialNoAndProductId(sNo, product.getId())
                    .orElseThrow(() -> new RuntimeException("Serial number " + sNo + " not found for product: " + product.getName()));

            if (serial.getStatus() != SerialNumber.SerialStatus.IN_STOCK) {
                throw new RuntimeException("Serial number " + sNo + " is not IN_STOCK (Current status: " + serial.getStatus() + ")");
            }

            if (batch != null && serial.getBatch() != null && !serial.getBatch().getId().equals(batch.getId())) {
                throw new RuntimeException("Serial number " + sNo + " does not belong to batch " + batch.getBatchNo());
            }

            if (warehouse != null && serial.getWarehouse() != null && !serial.getWarehouse().getId().equals(warehouse.getId())) {
                throw new RuntimeException("Serial number " + sNo + " is currently in warehouse " + serial.getWarehouse().getName() + " but is being issued from " + warehouse.getName());
            }

            if (location != null && serial.getLocation() != null && !serial.getLocation().getId().equals(location.getId())) {
                throw new RuntimeException("Serial number " + sNo + " is currently at location " + serial.getLocation().getName() + " but is being issued from " + location.getName());
            }

            serial.setStatus(newStatus);
            // Location and Warehouse might change based on status (e.g. cleared if ISSUED, updated if TRANSFER)
            if (newStatus == SerialNumber.SerialStatus.IN_STOCK) {
                serial.setWarehouse(warehouse);
                serial.setLocation(location);
            } else if (newStatus == SerialNumber.SerialStatus.ISSUED) {
                serial.setWarehouse(null);
                serial.setLocation(null);
            }
            
            serialNumberRepository.save(serial);

            logTransaction(serial, type, docType, docId, warehouse, location);
        }
    }

    private void logTransaction(SerialNumber serial, InventoryTransactionType type, String docType, Long docId, Warehouse warehouse, Location location) {
        SerialNumberTransaction tx = new SerialNumberTransaction();
        tx.setSerialNumber(serial);
        tx.setTransactionType(type);
        tx.setDocumentType(docType);
        tx.setDocumentId(docId);
        tx.setWarehouse(warehouse);
        tx.setLocation(location);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setCompanyId(org.enterprise.common.util.TenantContext.getCompanyId());
        serialNumberTransactionRepository.save(tx);
    }
}
