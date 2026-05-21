package org.enterprise.finance.service;

import org.enterprise.finance.dto.AccountBalanceDTO;
import org.enterprise.finance.entity.AccountBalance;
import org.enterprise.finance.repository.AccountBalanceRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountBalanceService {

    private final AccountBalanceRepository accountBalanceRepository;

    public AccountBalanceService(AccountBalanceRepository accountBalanceRepository) {
        this.accountBalanceRepository = accountBalanceRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountBalanceDTO> findAll() {
        return accountBalanceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountBalanceDTO findById(Long id) {
        return accountBalanceRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public AccountBalanceDTO save(AccountBalanceDTO dto) {
        AccountBalance entity = convertToEntity(dto);
        AccountBalance saved = accountBalanceRepository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        accountBalanceRepository.deleteById(id);
    }

    private AccountBalanceDTO convertToDTO(AccountBalance entity) {
        AccountBalanceDTO dto = new AccountBalanceDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private AccountBalance convertToEntity(AccountBalanceDTO dto) {
        AccountBalance entity = new AccountBalance();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
