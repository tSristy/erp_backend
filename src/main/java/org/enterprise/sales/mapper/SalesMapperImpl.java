package org.enterprise.sales.mapper;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import org.enterprise.sales.entity.*;
import org.enterprise.sales.dto.*;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.Batch;

@Component
public class SalesMapperImpl implements SalesMapper {

    @Override
    public SalesQuotation toEntity(SalesQuotationDto dto) {
        if (dto == null) return null;
        SalesQuotation entity = new SalesQuotation();
        BeanUtils.copyProperties(dto, entity, "details", "discounts");
        
        if (dto.getCustomerId() != null) {
            BusinessPartner customer = new BusinessPartner();
            customer.setId(dto.getCustomerId());
            entity.setCustomer(customer);
        }
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(dto.getWarehouseId());
            entity.setWarehouse(warehouse);
        }
        
        if (dto.getDetails() != null) {
            List<SalesQuotationDetail> list = new ArrayList<>();
            for (SalesQuotationDetailDto item : dto.getDetails()) {
                list.add(toEntity(item));
            }
            entity.setDetails(list);
        }
        if (dto.getDiscounts() != null) {
            List<SalesQuotationDiscount> list = new ArrayList<>();
            for (SalesQuotationDiscountDto item : dto.getDiscounts()) {
                list.add(toEntity(item));
            }
            entity.setDiscounts(list);
        }
        return entity;
    }

    @Override
    public SalesQuotationDto toDto(SalesQuotation entity) {
        if (entity == null) return null;
        SalesQuotationDto dto = new SalesQuotationDto();
        BeanUtils.copyProperties(entity, dto, "details", "discounts");
        
        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
        }
        
        if (entity.getDetails() != null) {
            List<SalesQuotationDetailDto> list = new ArrayList<>();
            for (SalesQuotationDetail item : entity.getDetails()) {
                list.add(toDto(item));
            }
            dto.setDetails(list);
        }
        if (entity.getDiscounts() != null) {
            List<SalesQuotationDiscountDto> list = new ArrayList<>();
            for (SalesQuotationDiscount item : entity.getDiscounts()) {
                list.add(toDto(item));
            }
            dto.setDiscounts(list);
        }
        return dto;
    }

    @Override
    public SalesQuotationDetail toEntity(SalesQuotationDetailDto dto) {
        if (dto == null) return null;
        SalesQuotationDetail entity = new SalesQuotationDetail();
        BeanUtils.copyProperties(dto, entity, "discounts");
        
        if (dto.getProductId() != null) {
            Product product = new Product();
            product.setId(dto.getProductId());
            entity.setProduct(product);
        }
        
        if (dto.getDiscounts() != null) {
            List<SalesQuotationDetailDiscount> list = new ArrayList<>();
            for (SalesQuotationDetailDiscountDto item : dto.getDiscounts()) {
                list.add(toEntity(item));
            }
            entity.setDiscounts(list);
        }
        return entity;
    }

    @Override
    public SalesQuotationDetailDto toDto(SalesQuotationDetail entity) {
        if (entity == null) return null;
        SalesQuotationDetailDto dto = new SalesQuotationDetailDto();
        BeanUtils.copyProperties(entity, dto, "discounts");
        
        if (entity.getSalesQuotation() != null) {
            dto.setSalesQuotationId(entity.getSalesQuotation().getId());
        }
        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
        }
        
        if (entity.getDiscounts() != null) {
            List<SalesQuotationDetailDiscountDto> list = new ArrayList<>();
            for (SalesQuotationDetailDiscount item : entity.getDiscounts()) {
                list.add(toDto(item));
            }
            dto.setDiscounts(list);
        }
        return dto;
    }

    // Discounts
    @Override
    public SalesQuotationDiscount toEntity(SalesQuotationDiscountDto dto) {
        if (dto == null) return null;
        SalesQuotationDiscount entity = new SalesQuotationDiscount();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
    @Override
    public SalesQuotationDiscountDto toDto(SalesQuotationDiscount entity) {
        if (entity == null) return null;
        SalesQuotationDiscountDto dto = new SalesQuotationDiscountDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    @Override
    public SalesQuotationDetailDiscount toEntity(SalesQuotationDetailDiscountDto dto) {
        if (dto == null) return null;
        SalesQuotationDetailDiscount entity = new SalesQuotationDetailDiscount();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
    @Override
    public SalesQuotationDetailDiscountDto toDto(SalesQuotationDetailDiscount entity) {
        if (entity == null) return null;
        SalesQuotationDetailDiscountDto dto = new SalesQuotationDetailDiscountDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    // SalesOrder
    @Override
    public SalesOrder toEntity(SalesOrderDto dto) {
        if (dto == null) return null;
        SalesOrder entity = new SalesOrder();
        BeanUtils.copyProperties(dto, entity, "details", "discounts");
        
        if (dto.getCustomerId() != null) {
            BusinessPartner customer = new BusinessPartner();
            customer.setId(dto.getCustomerId());
            entity.setCustomer(customer);
        }
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(dto.getWarehouseId());
            entity.setWarehouse(warehouse);
        }
        if (dto.getReferenceOrderId() != null) {
            SalesOrder order = new SalesOrder();
            order.setId(dto.getReferenceOrderId());
            entity.setReferenceOrder(order);
        }
        
        if (dto.getDetails() != null) {
            List<SalesOrderDetail> list = new ArrayList<>();
            for (SalesOrderDetailDto item : dto.getDetails()) {
                list.add(toEntity(item));
            }
            entity.setDetails(list);
        }
        if (dto.getDiscounts() != null) {
            List<SalesOrderDiscount> list = new ArrayList<>();
            for (SalesOrderDiscountDto item : dto.getDiscounts()) {
                list.add(toEntity(item));
            }
            entity.setDiscounts(list);
        }
        return entity;
    }

    @Override
    public SalesOrderDto toDto(SalesOrder entity) {
        if (entity == null) return null;
        SalesOrderDto dto = new SalesOrderDto();
        BeanUtils.copyProperties(entity, dto, "details", "discounts");
        
        if (entity.getCustomer() != null) dto.setCustomerId(entity.getCustomer().getId());
        if (entity.getWarehouse() != null) dto.setWarehouseId(entity.getWarehouse().getId());
        if (entity.getReferenceOrder() != null) dto.setReferenceOrderId(entity.getReferenceOrder().getId());
        
        if (entity.getDetails() != null) {
            List<SalesOrderDetailDto> list = new ArrayList<>();
            for (SalesOrderDetail item : entity.getDetails()) list.add(toDto(item));
            dto.setDetails(list);
        }
        if (entity.getDiscounts() != null) {
            List<SalesOrderDiscountDto> list = new ArrayList<>();
            for (SalesOrderDiscount item : entity.getDiscounts()) list.add(toDto(item));
            dto.setDiscounts(list);
        }
        return dto;
    }

    // SalesOrderDetail
    @Override
    public SalesOrderDetail toEntity(SalesOrderDetailDto dto) {
        if (dto == null) return null;
        SalesOrderDetail entity = new SalesOrderDetail();
        BeanUtils.copyProperties(dto, entity, "discounts");
        
        if (dto.getProductId() != null) {
            Product product = new Product();
            product.setId(dto.getProductId());
            entity.setProduct(product);
        }
        
        if (dto.getDiscounts() != null) {
            List<SalesOrderDetailDiscount> list = new ArrayList<>();
            for (SalesOrderDetailDiscountDto item : dto.getDiscounts()) list.add(toEntity(item));
            entity.setDiscounts(list);
        }
        return entity;
    }

    @Override
    public SalesOrderDetailDto toDto(SalesOrderDetail entity) {
        if (entity == null) return null;
        SalesOrderDetailDto dto = new SalesOrderDetailDto();
        BeanUtils.copyProperties(entity, dto, "discounts");
        
        if (entity.getSalesOrder() != null) dto.setSalesOrderId(entity.getSalesOrder().getId());
        if (entity.getProduct() != null) dto.setProductId(entity.getProduct().getId());
        
        if (entity.getDiscounts() != null) {
            List<SalesOrderDetailDiscountDto> list = new ArrayList<>();
            for (SalesOrderDetailDiscount item : entity.getDiscounts()) list.add(toDto(item));
            dto.setDiscounts(list);
        }
        return dto;
    }

    // Order Discounts
    @Override
    public SalesOrderDiscount toEntity(SalesOrderDiscountDto dto) {
        if (dto == null) return null;
        SalesOrderDiscount entity = new SalesOrderDiscount();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
    @Override
    public SalesOrderDiscountDto toDto(SalesOrderDiscount entity) {
        if (entity == null) return null;
        SalesOrderDiscountDto dto = new SalesOrderDiscountDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    @Override
    public SalesOrderDetailDiscount toEntity(SalesOrderDetailDiscountDto dto) {
        if (dto == null) return null;
        SalesOrderDetailDiscount entity = new SalesOrderDetailDiscount();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
    @Override
    public SalesOrderDetailDiscountDto toDto(SalesOrderDetailDiscount entity) {
        if (entity == null) return null;
        SalesOrderDetailDiscountDto dto = new SalesOrderDetailDiscountDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    // DeliveryNote
    @Override
    public DeliveryNote toEntity(DeliveryNoteDto dto) {
        if (dto == null) return null;
        DeliveryNote entity = new DeliveryNote();
        BeanUtils.copyProperties(dto, entity, "details");
        
        if (dto.getSalesOrderId() != null) {
            SalesOrder order = new SalesOrder();
            order.setId(dto.getSalesOrderId());
            entity.setSalesOrder(order);
        }
        if (dto.getCustomerId() != null) {
            BusinessPartner customer = new BusinessPartner();
            customer.setId(dto.getCustomerId());
            entity.setCustomer(customer);
        }
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(dto.getWarehouseId());
            entity.setWarehouse(warehouse);
        }
        
        if (dto.getDetails() != null) {
            List<DeliveryNoteDetail> list = new ArrayList<>();
            for (DeliveryNoteDetailDto item : dto.getDetails()) list.add(toEntity(item));
            entity.setDetails(list);
        }
        return entity;
    }

    @Override
    public DeliveryNoteDto toDto(DeliveryNote entity) {
        if (entity == null) return null;
        DeliveryNoteDto dto = new DeliveryNoteDto();
        BeanUtils.copyProperties(entity, dto, "details");
        
        if (entity.getSalesOrder() != null) dto.setSalesOrderId(entity.getSalesOrder().getId());
        if (entity.getCustomer() != null) dto.setCustomerId(entity.getCustomer().getId());
        if (entity.getWarehouse() != null) dto.setWarehouseId(entity.getWarehouse().getId());
        
        if (entity.getDetails() != null) {
            List<DeliveryNoteDetailDto> list = new ArrayList<>();
            for (DeliveryNoteDetail item : entity.getDetails()) list.add(toDto(item));
            dto.setDetails(list);
        }
        return dto;
    }

    // DeliveryNoteDetail
    @Override
    public DeliveryNoteDetail toEntity(DeliveryNoteDetailDto dto) {
        if (dto == null) return null;
        DeliveryNoteDetail entity = new DeliveryNoteDetail();
        BeanUtils.copyProperties(dto, entity);
        
        if (dto.getSalesOrderDetailId() != null) {
            SalesOrderDetail orderDetail = new SalesOrderDetail();
            orderDetail.setId(dto.getSalesOrderDetailId());
            entity.setSalesOrderDetail(orderDetail);
        }
        if (dto.getProductId() != null) {
            Product product = new Product();
            product.setId(dto.getProductId());
            entity.setProduct(product);
        }
        if (dto.getBatchId() != null) {
            Batch batch = new Batch();
            batch.setId(dto.getBatchId());
            entity.setBatch(batch);
        }
        return entity;
    }

    @Override
    public DeliveryNoteDetailDto toDto(DeliveryNoteDetail entity) {
        if (entity == null) return null;
        DeliveryNoteDetailDto dto = new DeliveryNoteDetailDto();
        BeanUtils.copyProperties(entity, dto);
        
        if (entity.getDeliveryNote() != null) dto.setDeliveryNoteId(entity.getDeliveryNote().getId());
        if (entity.getSalesOrderDetail() != null) dto.setSalesOrderDetailId(entity.getSalesOrderDetail().getId());
        if (entity.getProduct() != null) dto.setProductId(entity.getProduct().getId());
        if (entity.getBatch() != null) dto.setBatchId(entity.getBatch().getId());
        return dto;
    }

    // SalesInvoice
    @Override
    public SalesInvoice toEntity(SalesInvoiceDto dto) {
        if (dto == null) return null;
        SalesInvoice entity = new SalesInvoice();
        BeanUtils.copyProperties(dto, entity, "details", "discounts");
        
        if (dto.getDeliveryNoteId() != null) {
            DeliveryNote deliveryNote = new DeliveryNote();
            deliveryNote.setId(dto.getDeliveryNoteId());
            entity.setDeliveryNote(deliveryNote);
        }
        if (dto.getCustomerId() != null) {
            BusinessPartner customer = new BusinessPartner();
            customer.setId(dto.getCustomerId());
            entity.setCustomer(customer);
        }
        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(dto.getWarehouseId());
            entity.setWarehouse(warehouse);
        }
        
        if (dto.getDetails() != null) {
            List<SalesInvoiceDetail> list = new ArrayList<>();
            for (SalesInvoiceDetailDto item : dto.getDetails()) list.add(toEntity(item));
            entity.setDetails(list);
        }
        if (dto.getDiscounts() != null) {
            List<SalesInvoiceDiscount> list = new ArrayList<>();
            for (SalesInvoiceDiscountDto item : dto.getDiscounts()) list.add(toEntity(item));
            entity.setDiscounts(list);
        }
        return entity;
    }

    @Override
    public SalesInvoiceDto toDto(SalesInvoice entity) {
        if (entity == null) return null;
        SalesInvoiceDto dto = new SalesInvoiceDto();
        BeanUtils.copyProperties(entity, dto, "details", "discounts");
        
        if (entity.getDeliveryNote() != null) dto.setDeliveryNoteId(entity.getDeliveryNote().getId());
        if (entity.getCustomer() != null) dto.setCustomerId(entity.getCustomer().getId());
        if (entity.getWarehouse() != null) dto.setWarehouseId(entity.getWarehouse().getId());
        
        if (entity.getDetails() != null) {
            List<SalesInvoiceDetailDto> list = new ArrayList<>();
            for (SalesInvoiceDetail item : entity.getDetails()) list.add(toDto(item));
            dto.setDetails(list);
        }
        if (entity.getDiscounts() != null) {
            List<SalesInvoiceDiscountDto> list = new ArrayList<>();
            for (SalesInvoiceDiscount item : entity.getDiscounts()) list.add(toDto(item));
            dto.setDiscounts(list);
        }
        return dto;
    }

    // SalesInvoiceDetail
    @Override
    public SalesInvoiceDetail toEntity(SalesInvoiceDetailDto dto) {
        if (dto == null) return null;
        SalesInvoiceDetail entity = new SalesInvoiceDetail();
        BeanUtils.copyProperties(dto, entity, "discounts");
        
        if (dto.getDeliveryNoteDetailId() != null) {
            DeliveryNoteDetail deliveryNoteDetail = new DeliveryNoteDetail();
            deliveryNoteDetail.setId(dto.getDeliveryNoteDetailId());
            entity.setDeliveryNoteDetail(deliveryNoteDetail);
        }
        if (dto.getProductId() != null) {
            Product product = new Product();
            product.setId(dto.getProductId());
            entity.setProduct(product);
        }
        
        if (dto.getDiscounts() != null) {
            List<SalesInvoiceDetailDiscount> list = new ArrayList<>();
            for (SalesInvoiceDetailDiscountDto item : dto.getDiscounts()) list.add(toEntity(item));
            entity.setDiscounts(list);
        }
        return entity;
    }

    @Override
    public SalesInvoiceDetailDto toDto(SalesInvoiceDetail entity) {
        if (entity == null) return null;
        SalesInvoiceDetailDto dto = new SalesInvoiceDetailDto();
        BeanUtils.copyProperties(entity, dto, "discounts");
        
        if (entity.getSalesInvoice() != null) dto.setSalesInvoiceId(entity.getSalesInvoice().getId());
        if (entity.getDeliveryNoteDetail() != null) dto.setDeliveryNoteDetailId(entity.getDeliveryNoteDetail().getId());
        if (entity.getProduct() != null) dto.setProductId(entity.getProduct().getId());
        
        if (entity.getDiscounts() != null) {
            List<SalesInvoiceDetailDiscountDto> list = new ArrayList<>();
            for (SalesInvoiceDetailDiscount item : entity.getDiscounts()) list.add(toDto(item));
            dto.setDiscounts(list);
        }
        return dto;
    }

    // Invoice Discounts
    @Override
    public SalesInvoiceDiscount toEntity(SalesInvoiceDiscountDto dto) {
        if (dto == null) return null;
        SalesInvoiceDiscount entity = new SalesInvoiceDiscount();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
    @Override
    public SalesInvoiceDiscountDto toDto(SalesInvoiceDiscount entity) {
        if (entity == null) return null;
        SalesInvoiceDiscountDto dto = new SalesInvoiceDiscountDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    @Override
    public SalesInvoiceDetailDiscount toEntity(SalesInvoiceDetailDiscountDto dto) {
        if (dto == null) return null;
        SalesInvoiceDetailDiscount entity = new SalesInvoiceDetailDiscount();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
    @Override
    public SalesInvoiceDetailDiscountDto toDto(SalesInvoiceDetailDiscount entity) {
        if (entity == null) return null;
        SalesInvoiceDetailDiscountDto dto = new SalesInvoiceDetailDiscountDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    // Lists
    @Override
    public List<SalesQuotationDto> toDtoListSalesQuotation(List<SalesQuotation> entityList) {
        if (entityList == null) return null;
        List<SalesQuotationDto> list = new ArrayList<>(entityList.size());
        for (SalesQuotation e : entityList) list.add(toDto(e));
        return list;
    }
    @Override
    public List<SalesOrderDto> toDtoListSalesOrder(List<SalesOrder> entityList) {
        if (entityList == null) return null;
        List<SalesOrderDto> list = new ArrayList<>(entityList.size());
        for (SalesOrder e : entityList) list.add(toDto(e));
        return list;
    }
    @Override
    public List<DeliveryNoteDto> toDtoListDeliveryNote(List<DeliveryNote> entityList) {
        if (entityList == null) return null;
        List<DeliveryNoteDto> list = new ArrayList<>(entityList.size());
        for (DeliveryNote e : entityList) list.add(toDto(e));
        return list;
    }
    @Override
    public List<SalesInvoiceDto> toDtoListSalesInvoice(List<SalesInvoice> entityList) {
        if (entityList == null) return null;
        List<SalesInvoiceDto> list = new ArrayList<>(entityList.size());
        for (SalesInvoice e : entityList) list.add(toDto(e));
        return list;
    }
}
