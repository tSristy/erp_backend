package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.Location;
import org.enterprise.inventory.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService extends BaseService<Location, Long> {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        super(locationRepository);
        this.locationRepository = locationRepository;
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public java.util.List<Location> getLocationsByWarehouseId(Long warehouseId) {
        return locationRepository.findByWarehouseId(warehouseId);
    }

    public java.util.List<Location> getLocationsByParentId(Long parentId) {
        return locationRepository.findByParentId(parentId);
    }

    public java.util.List<Location> getRootLocationsByWarehouseId(Long warehouseId) {
        return locationRepository.findByWarehouseIdAndParentIsNull(warehouseId);
    }
}
