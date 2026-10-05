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


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Location> searchLocations(String query, org.springframework.data.domain.Pageable pageable) {
        return locationRepository.findAll(org.enterprise.inventory.specification.LocationSpecification.searchByQuery(query), pageable);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Location save(Location entity) {
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            locationRepository.findTopByCodeStartingWithOrderByIdDesc("LOC-")
                .ifPresentOrElse(
                    lastLocation -> {
                        String lastCode = lastLocation.getCode();
                        try {
                            int seq = Integer.parseInt(lastCode.replace("LOC-", ""));
                            entity.setCode(String.format("LOC-%03d", seq + 1));
                        } catch (Exception e) {
                            entity.setCode("LOC-001");
                        }
                    },
                    () -> entity.setCode("LOC-001")
                );
        }
        return super.save(entity);
    }
}
