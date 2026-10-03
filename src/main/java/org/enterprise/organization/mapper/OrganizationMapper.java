package org.enterprise.organization.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import org.enterprise.organization.entity.*;
import org.enterprise.organization.dto.*;
import org.enterprise.hr.entity.Employee;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    Company toEntity(CompanyDto dto);
    CompanyDto toDto(Company entity);
    List<CompanyDto> toDtoListCompany(List<Company> entityList);

    @Mapping(target = "company.id", source = "companyId")
    Branch toEntity(BranchDto dto);

    @Mapping(target = "companyId", source = "company.id")
    BranchDto toDto(Branch entity);

    List<BranchDto> toDtoListBranch(List<Branch> entityList);

    @Mapping(target = "area.id", source = "areaId")
    @Mapping(target = "territoryManager.id", source = "territoryManagerId")
    Territory toEntity(TerritoryDto dto);

    @Mapping(target = "areaId", source = "area.id")
    @Mapping(target = "territoryManagerId", source = "territoryManager.id")
    TerritoryDto toDto(Territory entity);

    List<TerritoryDto> toDtoListTerritory(List<Territory> entityList);

    @Mapping(target = "regionalManager.id", source = "regionalManagerId")
    Zone toEntity(ZoneDto dto);

    @Mapping(target = "regionalManagerId", source = "regionalManager.id")
    ZoneDto toDto(Zone entity);

    List<ZoneDto> toDtoListZone(List<Zone> entityList);

    @Mapping(target = "zone.id", source = "zoneId")
    @Mapping(target = "areaManager.id", source = "areaManagerId")
    Area toEntity(AreaDto dto);

    @Mapping(target = "zoneId", source = "zone.id")
    @Mapping(target = "areaManagerId", source = "areaManager.id")
    AreaDto toDto(Area entity);

    List<AreaDto> toDtoListArea(List<Area> entityList);
}
