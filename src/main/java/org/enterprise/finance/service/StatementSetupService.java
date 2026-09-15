package org.enterprise.finance.service;

import org.enterprise.finance.dto.StatementSetupDTO;
import org.enterprise.finance.entity.StatementSetup;
import org.enterprise.finance.repository.StatementSetupRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatementSetupService {

    private final StatementSetupRepository statementSetupRepository;

    public StatementSetupService(StatementSetupRepository statementSetupRepository) {
        this.statementSetupRepository = statementSetupRepository;
    }

    @Transactional(readOnly = true)
    public List<StatementSetupDTO> findAll() {
        return statementSetupRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StatementSetupDTO findById(Long id) {
        return statementSetupRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public StatementSetupDTO save(StatementSetupDTO dto) {
        StatementSetup entity = convertToEntity(dto);
        StatementSetup saved = statementSetupRepository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        statementSetupRepository.deleteById(id);
    }

    private StatementSetupDTO convertToDTO(StatementSetup entity) {
        StatementSetupDTO dto = new StatementSetupDTO();
        BeanUtils.copyProperties(entity, dto, "accounts");
        if (entity.getAccounts() != null) {
                        dto.setAccounts(entity.getAccounts().stream().map(account -> {
                org.enterprise.finance.dto.StatementSetupAccountDTO accountDto = new org.enterprise.finance.dto.StatementSetupAccountDTO();
                BeanUtils.copyProperties(account, accountDto);
                if (account.getAccount() != null) {
                    org.enterprise.finance.dto.AccountDTO accDto = new org.enterprise.finance.dto.AccountDTO();
                    BeanUtils.copyProperties(account.getAccount(), accDto);
                    accountDto.setAccount(accDto);
                }
                return accountDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private StatementSetup convertToEntity(StatementSetupDTO dto) {
        StatementSetup entity = new StatementSetup();
        BeanUtils.copyProperties(dto, entity, "accounts");
        if (dto.getAccounts() != null) {
                        entity.setAccounts(dto.getAccounts().stream().map(accountDto -> {
                org.enterprise.finance.entity.StatementSetupAccount account = new org.enterprise.finance.entity.StatementSetupAccount();
                BeanUtils.copyProperties(accountDto, account);
                if (accountDto.getAccount() != null && accountDto.getAccount().getId() != null) {
                    org.enterprise.finance.entity.Account acc = new org.enterprise.finance.entity.Account();
                    acc.setId(accountDto.getAccount().getId());
                    account.setAccount(acc);
                }
                account.setStatementSetup(entity);
                return account;
            }).collect(Collectors.toList()));
        }
        return entity;
    }
}
