package org.enterprise.pos.retail.mapper;

import java.util.ArrayList;
import java.util.List;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.pos.retail.dto.RetailTransactionDetailDto;
import org.enterprise.pos.retail.dto.RetailTransactionDto;
import org.enterprise.pos.retail.dto.RetailTransactionPaymentDto;
import org.enterprise.pos.retail.entity.RetailTransaction;
import org.enterprise.pos.retail.entity.RetailTransactionDetail;
import org.enterprise.pos.retail.entity.RetailTransactionPayment;
import org.springframework.stereotype.Component;

@Component
public class PosRetailMapperImpl implements PosRetailMapper {

    @Override
    public RetailTransaction toEntity(RetailTransactionDto dto) {
        if (dto == null) {
            return null;
        }

        RetailTransaction retailTransaction = new RetailTransaction();

        if (dto.getCustomerId() != null) {
            BusinessPartner customer = new BusinessPartner();
            customer.setId(dto.getCustomerId());
            retailTransaction.setCustomer(customer);
        }
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(dto.getWarehouseId());
            retailTransaction.setWarehouse(warehouse);
        }
        if (dto.getReferenceTransactionId() != null) {
            RetailTransaction referenceTransaction = new RetailTransaction();
            referenceTransaction.setId(dto.getReferenceTransactionId());
            retailTransaction.setReferenceTransaction(referenceTransaction);
        }

        retailTransaction.setId(dto.getId());
        retailTransaction.setCompanyId(dto.getCompanyId());
        retailTransaction.setCreatedAt(dto.getCreatedAt());
        retailTransaction.setUpdatedAt(dto.getUpdatedAt());
        retailTransaction.setTransactionNo(dto.getTransactionNo());
        retailTransaction.setTransactionDate(map(dto.getTransactionDate()));
        retailTransaction.setType(dto.getType());
        retailTransaction.setStatus(dto.getStatus());

        
        if (dto.getDetails() != null) {
            List<RetailTransactionDetail> details = new ArrayList<>(dto.getDetails().size());
            for (RetailTransactionDetailDto detailDto : dto.getDetails()) {
                details.add(toEntity(detailDto));
            }
            retailTransaction.setDetails(details);
        }
        
        if (dto.getPayments() != null) {
            List<RetailTransactionPayment> payments = new ArrayList<>(dto.getPayments().size());
            for (RetailTransactionPaymentDto paymentDto : dto.getPayments()) {
                payments.add(toEntity(paymentDto));
            }
            retailTransaction.setPayments(payments);
        }

        return retailTransaction;
    }

    @Override
    public RetailTransactionDto toDto(RetailTransaction entity) {
        if (entity == null) {
            return null;
        }

        RetailTransactionDto retailTransactionDto = new RetailTransactionDto();

        if (entity.getCustomer() != null) {
            retailTransactionDto.setCustomerId(entity.getCustomer().getId());
        }
        if (entity.getWarehouse() != null) {
            retailTransactionDto.setWarehouseId(entity.getWarehouse().getId());
        }
        if (entity.getReferenceTransaction() != null) {
            retailTransactionDto.setReferenceTransactionId(entity.getReferenceTransaction().getId());
        }

        retailTransactionDto.setId(entity.getId());
        retailTransactionDto.setCompanyId(entity.getCompanyId());
        retailTransactionDto.setCreatedAt(entity.getCreatedAt());
        retailTransactionDto.setUpdatedAt(entity.getUpdatedAt());
        retailTransactionDto.setTransactionNo(entity.getTransactionNo());
        retailTransactionDto.setTransactionDate(map(entity.getTransactionDate()));
        retailTransactionDto.setType(entity.getType());
        retailTransactionDto.setStatus(entity.getStatus());


        java.math.BigDecimal subTotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal taxTotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal discountTotal = java.math.BigDecimal.ZERO;

        if (entity.getDetails() != null) {
            List<RetailTransactionDetailDto> details = new ArrayList<>(entity.getDetails().size());
            for (RetailTransactionDetail detail : entity.getDetails()) {
                details.add(toDto(detail));
                if (detail.getLineTotal() != null) {
                    subTotal = subTotal.add(detail.getLineTotal());
                }
            }
            retailTransactionDto.setDetails(details);
        }
        
        java.math.BigDecimal amountPaid = java.math.BigDecimal.ZERO;
        if (entity.getPayments() != null) {
            List<RetailTransactionPaymentDto> payments = new ArrayList<>(entity.getPayments().size());
            for (RetailTransactionPayment payment : entity.getPayments()) {
                payments.add(toDto(payment));
                if (payment.getAmount() != null) {
                    amountPaid = amountPaid.add(payment.getAmount());
                }
            }
            retailTransactionDto.setPayments(payments);
        }

        retailTransactionDto.setSubTotal(subTotal);
        retailTransactionDto.setTaxTotal(taxTotal);
        retailTransactionDto.setDiscountTotal(discountTotal);
        
        java.math.BigDecimal grandTotal = entity.getTotalAmount() != null ? entity.getTotalAmount() : java.math.BigDecimal.ZERO;
        retailTransactionDto.setGrandTotal(grandTotal);
        retailTransactionDto.setAmountPaid(amountPaid);
        retailTransactionDto.setAmountDue(grandTotal.subtract(amountPaid));

        return retailTransactionDto;
    }

    @Override
    public RetailTransactionDetail toEntity(RetailTransactionDetailDto dto) {
        if (dto == null) {
            return null;
        }

        RetailTransactionDetail retailTransactionDetail = new RetailTransactionDetail();

        if (dto.getProductId() != null) {
            Product product = new Product();
            product.setId(dto.getProductId());
            retailTransactionDetail.setProduct(product);
        }

        retailTransactionDetail.setId(dto.getId());
        retailTransactionDetail.setCompanyId(dto.getCompanyId());
        retailTransactionDetail.setCreatedAt(dto.getCreatedAt());
        retailTransactionDetail.setUpdatedAt(dto.getUpdatedAt());
        retailTransactionDetail.setQuantity(dto.getQuantity());
        retailTransactionDetail.setUnitPrice(dto.getUnitPrice());

        retailTransactionDetail.setLineTotal(dto.getLineTotal());

        return retailTransactionDetail;
    }

    @Override
    public RetailTransactionDetailDto toDto(RetailTransactionDetail entity) {
        if (entity == null) {
            return null;
        }

        RetailTransactionDetailDto retailTransactionDetailDto = new RetailTransactionDetailDto();

        if (entity.getTransaction() != null) {
            retailTransactionDetailDto.setTransactionId(entity.getTransaction().getId());
        }
        if (entity.getProduct() != null) {
            retailTransactionDetailDto.setProductId(entity.getProduct().getId());
        }

        retailTransactionDetailDto.setId(entity.getId());
        retailTransactionDetailDto.setCompanyId(entity.getCompanyId());
        retailTransactionDetailDto.setCreatedAt(entity.getCreatedAt());
        retailTransactionDetailDto.setUpdatedAt(entity.getUpdatedAt());
        retailTransactionDetailDto.setQuantity(entity.getQuantity());
        retailTransactionDetailDto.setUnitPrice(entity.getUnitPrice());

        retailTransactionDetailDto.setLineTotal(entity.getLineTotal());

        return retailTransactionDetailDto;
    }

    @Override
    public RetailTransactionPayment toEntity(RetailTransactionPaymentDto dto) {
        if (dto == null) {
            return null;
        }

        RetailTransactionPayment retailTransactionPayment = new RetailTransactionPayment();

        retailTransactionPayment.setId(dto.getId());
        retailTransactionPayment.setCompanyId(dto.getCompanyId());
        retailTransactionPayment.setCreatedAt(dto.getCreatedAt());
        retailTransactionPayment.setUpdatedAt(dto.getUpdatedAt());
        retailTransactionPayment.setPaymentMode(dto.getPaymentMode());
        retailTransactionPayment.setAmount(dto.getAmount());
        retailTransactionPayment.setReferenceNumber(dto.getReference());

        return retailTransactionPayment;
    }

    @Override
    public RetailTransactionPaymentDto toDto(RetailTransactionPayment entity) {
        if (entity == null) {
            return null;
        }

        RetailTransactionPaymentDto retailTransactionPaymentDto = new RetailTransactionPaymentDto();

        if (entity.getTransaction() != null) {
            retailTransactionPaymentDto.setTransactionId(entity.getTransaction().getId());
        }

        retailTransactionPaymentDto.setId(entity.getId());
        retailTransactionPaymentDto.setCompanyId(entity.getCompanyId());
        retailTransactionPaymentDto.setCreatedAt(entity.getCreatedAt());
        retailTransactionPaymentDto.setUpdatedAt(entity.getUpdatedAt());
        retailTransactionPaymentDto.setPaymentMode(entity.getPaymentMode());
        retailTransactionPaymentDto.setAmount(entity.getAmount());
        retailTransactionPaymentDto.setReference(entity.getReferenceNumber());

        return retailTransactionPaymentDto;
    }

    @Override
    public List<RetailTransactionDto> toDtoListRetailTransaction(List<RetailTransaction> entityList) {
        if (entityList == null) {
            return null;
        }

        List<RetailTransactionDto> list = new ArrayList<>(entityList.size());
        for (RetailTransaction retailTransaction : entityList) {
            list.add(toDto(retailTransaction));
        }

        return list;
    }
}
