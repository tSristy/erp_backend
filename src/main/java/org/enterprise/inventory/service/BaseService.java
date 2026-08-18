package org.enterprise.inventory.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID> {

    protected final JpaRepository<T, ID> repository;

    protected BaseService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    public T save(T entity) {
        if (entity instanceof org.enterprise.common.entity.TenantEntity) {
            org.enterprise.common.entity.TenantEntity tenantEntity = (org.enterprise.common.entity.TenantEntity) entity;
            if (tenantEntity.getCompanyId() == null) {
                tenantEntity.setCompanyId(org.enterprise.common.util.TenantContext.getCompanyId());
            }
        }
        return repository.save(entity);
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    public void delete(ID id) {
        repository.deleteById(id);
    }
}