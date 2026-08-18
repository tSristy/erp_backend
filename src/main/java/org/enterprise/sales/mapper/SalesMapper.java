package org.enterprise.sales.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import org.enterprise.sales.entity.*;
import org.enterprise.sales.dto.*;

@Mapper(componentModel = "spring")
public interface SalesMapper {

    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "warehouse.id", source = "warehouseId")
    SalesQuotation toEntity(SalesQuotationDto dto);
    
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    SalesQuotationDto toDto(SalesQuotation entity);

    @Mapping(target = "salesQuotation", ignore = true)
    @Mapping(target = "product.id", source = "productId")
    SalesQuotationDetail toEntity(SalesQuotationDetailDto dto);
    
    @Mapping(target = "salesQuotationId", source = "salesQuotation.id")
    @Mapping(target = "productId", source = "product.id")
    SalesQuotationDetailDto toDto(SalesQuotationDetail entity);

    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "warehouse.id", source = "warehouseId")
    @Mapping(target = "referenceOrder.id", source = "referenceOrderId")
    SalesOrder toEntity(SalesOrderDto dto);
    
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "referenceOrderId", source = "referenceOrder.id")
    SalesOrderDto toDto(SalesOrder entity);

    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "product.id", source = "productId")
    SalesOrderDetail toEntity(SalesOrderDetailDto dto);
    
    @Mapping(target = "salesOrderId", source = "salesOrder.id")
    @Mapping(target = "productId", source = "product.id")
    SalesOrderDetailDto toDto(SalesOrderDetail entity);

    @Mapping(target = "salesOrder.id", source = "salesOrderId")
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "warehouse.id", source = "warehouseId")
    DeliveryNote toEntity(DeliveryNoteDto dto);
    
    @Mapping(target = "salesOrderId", source = "salesOrder.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    DeliveryNoteDto toDto(DeliveryNote entity);

    @Mapping(target = "deliveryNote", ignore = true)
    @Mapping(target = "salesOrderDetail.id", source = "salesOrderDetailId")
    @Mapping(target = "product.id", source = "productId")
    @Mapping(target = "batch.id", source = "batchId")
    DeliveryNoteDetail toEntity(DeliveryNoteDetailDto dto);
    
    @Mapping(target = "deliveryNoteId", source = "deliveryNote.id")
    @Mapping(target = "salesOrderDetailId", source = "salesOrderDetail.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "batchId", source = "batch.id")
    DeliveryNoteDetailDto toDto(DeliveryNoteDetail entity);

    @Mapping(target = "deliveryNote.id", source = "deliveryNoteId")
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "warehouse.id", source = "warehouseId")
    SalesInvoice toEntity(SalesInvoiceDto dto);
    
    @Mapping(target = "deliveryNoteId", source = "deliveryNote.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    SalesInvoiceDto toDto(SalesInvoice entity);

    @Mapping(target = "salesInvoice", ignore = true)
    @Mapping(target = "deliveryNoteDetail.id", source = "deliveryNoteDetailId")
    @Mapping(target = "product.id", source = "productId")
    SalesInvoiceDetail toEntity(SalesInvoiceDetailDto dto);
    
    @Mapping(target = "salesInvoiceId", source = "salesInvoice.id")
    @Mapping(target = "deliveryNoteDetailId", source = "deliveryNoteDetail.id")
    @Mapping(target = "productId", source = "product.id")
    SalesInvoiceDetailDto toDto(SalesInvoiceDetail entity);

    List<SalesQuotationDto> toDtoListSalesQuotation(List<SalesQuotation> entityList);
    List<SalesOrderDto> toDtoListSalesOrder(List<SalesOrder> entityList);
    List<DeliveryNoteDto> toDtoListDeliveryNote(List<DeliveryNote> entityList);
    List<SalesInvoiceDto> toDtoListSalesInvoice(List<SalesInvoice> entityList);

    @Mapping(target = "salesQuotation", ignore = true)
    SalesQuotationDiscount toEntity(SalesQuotationDiscountDto dto);
    SalesQuotationDiscountDto toDto(SalesQuotationDiscount entity);

    @Mapping(target = "salesQuotationDetail", ignore = true)
    SalesQuotationDetailDiscount toEntity(SalesQuotationDetailDiscountDto dto);
    SalesQuotationDetailDiscountDto toDto(SalesQuotationDetailDiscount entity);

    @Mapping(target = "salesOrder", ignore = true)
    SalesOrderDiscount toEntity(SalesOrderDiscountDto dto);
    SalesOrderDiscountDto toDto(SalesOrderDiscount entity);

    @Mapping(target = "salesOrderDetail", ignore = true)
    SalesOrderDetailDiscount toEntity(SalesOrderDetailDiscountDto dto);
    SalesOrderDetailDiscountDto toDto(SalesOrderDetailDiscount entity);

    @Mapping(target = "salesInvoice", ignore = true)
    SalesInvoiceDiscount toEntity(SalesInvoiceDiscountDto dto);
    SalesInvoiceDiscountDto toDto(SalesInvoiceDiscount entity);

    @Mapping(target = "salesInvoiceDetail", ignore = true)
    SalesInvoiceDetailDiscount toEntity(SalesInvoiceDetailDiscountDto dto);
    SalesInvoiceDetailDiscountDto toDto(SalesInvoiceDetailDiscount entity);
}
