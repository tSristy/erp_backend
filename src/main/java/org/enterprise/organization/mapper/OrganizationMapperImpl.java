package org.enterprise.organization.mapper;

import java.util.ArrayList;
import java.util.List;
import org.enterprise.hr.entity.Employee;
import org.enterprise.organization.dto.AreaDto;
import org.enterprise.organization.dto.BranchDto;
import org.enterprise.organization.dto.CompanyDto;
import org.enterprise.organization.dto.TerritoryDto;
import org.enterprise.organization.dto.ZoneDto;
import org.enterprise.organization.entity.Area;
import org.enterprise.organization.entity.Branch;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.entity.Territory;
import org.enterprise.organization.entity.Zone;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapperImpl implements OrganizationMapper {

    @Override
    public Company toEntity(CompanyDto dto) {
        if (dto == null) {
            return null;
        }
        Company company = new Company();
        company.setId(dto.getId());
        company.setCode(dto.getCode());
        company.setName(dto.getName());
        company.setShortName(dto.getShortName());
        company.setEmail(dto.getEmail());
        company.setPhone(dto.getPhone());
        company.setMobile(dto.getMobile());
        company.setWebsite(dto.getWebsite());
        company.setCountry(dto.getCountry());
        company.setDivision(dto.getDivision());
        company.setDistrict(dto.getDistrict());
        company.setCity(dto.getCity());
        company.setZipCode(dto.getZipCode());
        company.setAddress(dto.getAddress());
        company.setTaxNumber(dto.getTaxNumber());
        company.setVatNumber(dto.getVatNumber());
        company.setTradeLicenseNo(dto.getTradeLicenseNo());
        company.setCurrencyCode(dto.getCurrencyCode());
        company.setTimezone(dto.getTimezone());
        company.setLanguageCode(dto.getLanguageCode());
        company.setLogoUrl(dto.getLogoUrl());
        company.setActive(dto.getActive());
        company.setStartDate(dto.getStartDate());
        company.setEndDate(dto.getEndDate());
        return company;
    }

    @Override
    public CompanyDto toDto(Company entity) {
        if (entity == null) {
            return null;
        }
        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(entity.getId());
        companyDto.setCode(entity.getCode());
        companyDto.setName(entity.getName());
        companyDto.setShortName(entity.getShortName());
        companyDto.setEmail(entity.getEmail());
        companyDto.setPhone(entity.getPhone());
        companyDto.setMobile(entity.getMobile());
        companyDto.setWebsite(entity.getWebsite());
        companyDto.setCountry(entity.getCountry());
        companyDto.setDivision(entity.getDivision());
        companyDto.setDistrict(entity.getDistrict());
        companyDto.setCity(entity.getCity());
        companyDto.setZipCode(entity.getZipCode());
        companyDto.setAddress(entity.getAddress());
        companyDto.setTaxNumber(entity.getTaxNumber());
        companyDto.setVatNumber(entity.getVatNumber());
        companyDto.setTradeLicenseNo(entity.getTradeLicenseNo());
        companyDto.setCurrencyCode(entity.getCurrencyCode());
        companyDto.setTimezone(entity.getTimezone());
        companyDto.setLanguageCode(entity.getLanguageCode());
        companyDto.setLogoUrl(entity.getLogoUrl());
        companyDto.setActive(entity.getActive());
        companyDto.setStartDate(entity.getStartDate());
        companyDto.setEndDate(entity.getEndDate());
        return companyDto;
    }

    @Override
    public List<CompanyDto> toDtoListCompany(List<Company> entityList) {
        if (entityList == null) {
            return null;
        }
        List<CompanyDto> list = new ArrayList<>(entityList.size());
        for (Company company : entityList) {
            list.add(toDto(company));
        }
        return list;
    }

    @Override
    public Branch toEntity(BranchDto dto) {
        if (dto == null) {
            return null;
        }
        Branch branch = new Branch();
        if (dto.getCompanyId() != null) {
            Company company = new Company();
            company.setId(dto.getCompanyId());
            branch.setCompany(company);
        }
        branch.setId(dto.getId());
        branch.setCode(dto.getCode());
        branch.setName(dto.getName());
        branch.setShortName(dto.getShortName());
        branch.setBranchType(dto.getBranchType());
        branch.setEmail(dto.getEmail());
        branch.setPhone(dto.getPhone());
        branch.setCountry(dto.getCountry());
        branch.setCity(dto.getCity());
        branch.setZipCode(dto.getZipCode());
        branch.setAddress(dto.getAddress());
        branch.setActive(dto.getActive());
        branch.setHeadOffice(dto.getHeadOffice());
        branch.setCompanyId(dto.getCompanyId());
        return branch;
    }

    @Override
    public BranchDto toDto(Branch entity) {
        if (entity == null) {
            return null;
        }
        BranchDto branchDto = new BranchDto();
        if (entity.getCompany() != null) {
            branchDto.setCompanyId(entity.getCompany().getId());
            branchDto.setCompanyName(entity.getCompany().getName());
        }
        branchDto.setId(entity.getId());
        branchDto.setCode(entity.getCode());
        branchDto.setName(entity.getName());
        branchDto.setShortName(entity.getShortName());
        branchDto.setBranchType(entity.getBranchType());
        branchDto.setEmail(entity.getEmail());
        branchDto.setPhone(entity.getPhone());
        branchDto.setCountry(entity.getCountry());
        branchDto.setCity(entity.getCity());
        branchDto.setZipCode(entity.getZipCode());
        branchDto.setAddress(entity.getAddress());
        branchDto.setActive(entity.getActive());
        branchDto.setHeadOffice(entity.getHeadOffice());
        return branchDto;
    }

    @Override
    public List<BranchDto> toDtoListBranch(List<Branch> entityList) {
        if (entityList == null) {
            return null;
        }
        List<BranchDto> list = new ArrayList<>(entityList.size());
        for (Branch branch : entityList) {
            list.add(toDto(branch));
        }
        return list;
    }

    @Override
    public Territory toEntity(TerritoryDto dto) {
        if (dto == null) {
            return null;
        }
        Territory territory = new Territory();
        if (dto.getAreaId() != null) {
            Area area = new Area();
            area.setId(dto.getAreaId());
            territory.setArea(area);
        }
        if (dto.getTerritoryManagerId() != null) {
            Employee employee = new Employee();
            employee.setId(dto.getTerritoryManagerId());
            territory.setTerritoryManager(employee);
        }
        territory.setId(dto.getId());
        territory.setCode(dto.getCode());
        territory.setName(dto.getName());
        territory.setSalesType(dto.getSalesType());
        territory.setActive(dto.getActive());
        return territory;
    }

    @Override
    public TerritoryDto toDto(Territory entity) {
        if (entity == null) {
            return null;
        }
        TerritoryDto territoryDto = new TerritoryDto();
        if (entity.getArea() != null) {
            territoryDto.setAreaId(entity.getArea().getId());
        }
        if (entity.getTerritoryManager() != null) {
            territoryDto.setTerritoryManagerId(entity.getTerritoryManager().getId());
        }
        territoryDto.setId(entity.getId());
        territoryDto.setCode(entity.getCode());
        territoryDto.setName(entity.getName());
        territoryDto.setSalesType(entity.getSalesType());
        territoryDto.setActive(entity.getActive());
        return territoryDto;
    }

    @Override
    public List<TerritoryDto> toDtoListTerritory(List<Territory> entityList) {
        if (entityList == null) {
            return null;
        }
        List<TerritoryDto> list = new ArrayList<>(entityList.size());
        for (Territory territory : entityList) {
            list.add(toDto(territory));
        }
        return list;
    }

    @Override
    public Zone toEntity(ZoneDto dto) {
        if (dto == null) {
            return null;
        }
        Zone zone = new Zone();
        if (dto.getRegionalManagerId() != null) {
            Employee employee = new Employee();
            employee.setId(dto.getRegionalManagerId());
            zone.setRegionalManager(employee);
        }
        zone.setId(dto.getId());
        zone.setCode(dto.getCode());
        zone.setName(dto.getName());
        zone.setActive(dto.getActive());
        return zone;
    }

    @Override
    public ZoneDto toDto(Zone entity) {
        if (entity == null) {
            return null;
        }
        ZoneDto zoneDto = new ZoneDto();
        if (entity.getRegionalManager() != null) {
            zoneDto.setRegionalManagerId(entity.getRegionalManager().getId());
        }
        zoneDto.setId(entity.getId());
        zoneDto.setCode(entity.getCode());
        zoneDto.setName(entity.getName());
        zoneDto.setActive(entity.getActive());
        return zoneDto;
    }

    @Override
    public List<ZoneDto> toDtoListZone(List<Zone> entityList) {
        if (entityList == null) {
            return null;
        }
        List<ZoneDto> list = new ArrayList<>(entityList.size());
        for (Zone zone : entityList) {
            list.add(toDto(zone));
        }
        return list;
    }

    @Override
    public Area toEntity(AreaDto dto) {
        if (dto == null) {
            return null;
        }
        Area area = new Area();
        if (dto.getZoneId() != null) {
            Zone zone = new Zone();
            zone.setId(dto.getZoneId());
            area.setZone(zone);
        }
        if (dto.getAreaManagerId() != null) {
            Employee employee = new Employee();
            employee.setId(dto.getAreaManagerId());
            area.setAreaManager(employee);
        }
        area.setId(dto.getId());
        area.setCode(dto.getCode());
        area.setName(dto.getName());
        area.setActive(dto.getActive());
        return area;
    }

    @Override
    public AreaDto toDto(Area entity) {
        if (entity == null) {
            return null;
        }
        AreaDto areaDto = new AreaDto();
        if (entity.getZone() != null) {
            areaDto.setZoneId(entity.getZone().getId());
        }
        if (entity.getAreaManager() != null) {
            areaDto.setAreaManagerId(entity.getAreaManager().getId());
        }
        areaDto.setId(entity.getId());
        areaDto.setCode(entity.getCode());
        areaDto.setName(entity.getName());
        areaDto.setActive(entity.getActive());
        return areaDto;
    }

    @Override
    public List<AreaDto> toDtoListArea(List<Area> entityList) {
        if (entityList == null) {
            return null;
        }
        List<AreaDto> list = new ArrayList<>(entityList.size());
        for (Area area : entityList) {
            list.add(toDto(area));
        }
        return list;
    }
}
