package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Product;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "crm_registered_products")
@Getter
@Setter
public class RegisteredProduct extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private BusinessPartner customer;

    private String serialNumber;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    @Enumerated(EnumType.STRING)
    private RegisteredProductStatus status = RegisteredProductStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "crm_registered_product_attrs", joinColumns = @JoinColumn(name = "registered_product_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    public enum RegisteredProductStatus {
        ACTIVE, INACTIVE, REPLACED, RETIRED
    }
}

/*
 * 1. For a Vehicle
 * Vehicles have unique identifiers like VINs and License Plates.
 * {
 * "productId": 101,
 * "customerId": 55,
 * "serialNumber": "ENG-998877",
 * "purchaseDate": "2024-01-15",
 * "warrantyExpiryDate": "2027-01-15",
 * "status": "ACTIVE",
 * "attributes": {
 * "VIN": "1HGCM82633A004123",
 * "LicensePlate": "DHK-1234",
 * "Color": "Metallic Silver",
 * "EngineCapacity": "1500cc",
 * "Transmission": "Automatic"
 * }
 * }
 * 2. For a Mobile Device
 * Mobiles usually have one or two IMEI numbers, color, and storage capacity.
 * 
 * json
 * {
 * "productId": 205,
 * "customerId": 102,
 * "serialNumber": "SAMSUNG-S24-1122",
 * "purchaseDate": "2024-05-10",
 * "warrantyExpiryDate": "2025-05-10",
 * "status": "ACTIVE",
 * "attributes": {
 * "IMEI_1": "358123456789012",
 * "IMEI_2": "358123456789013",
 * "Storage": "256GB",
 * "Color": "Titanium Black"
 * }
 * }
 * 3. For Consumer Electronics (TV / Freezer)
 * A TV might have a MAC address for its smart features, while a Freezer might
 * have compressor details.
 * 
 * json
 * {
 * "productId": 310,
 * "customerId": 88,
 * "serialNumber": "LG-OLED-778899",
 * "purchaseDate": "2023-11-20",
 * "warrantyExpiryDate": "2025-11-20",
 * "status": "ACTIVE",
 * "attributes": {
 * "ModelNumber": "OLED65CXPUA",
 * "MAC_Address_WiFi": "00:1A:2B:3C:4D:5E",
 * "ScreenSize": "65 Inch"
 * }
 * }
 */