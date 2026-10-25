package org.enterprise.finance.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.finance.entity.Account;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.repository.AccountRepository;
import org.enterprise.finance.service.JournalEntryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PosTransactionFinanceListener {

    private final JournalEntryService journalEntryService;
    private final AccountRepository accountRepository;

    /**
     * Listens for POS transactions and records the GL JournalEntry automatically.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        log.info("Finance Module received POS transaction completion event for: {} (Type: {}). Processing payments...", event.getTransactionNo(), event.getTransactionType());

        try {
            Long companyId = event.getCompanyId();
            if (companyId == null || event.getPayments() == null || event.getPayments().isEmpty()) {
                return;
            }

            Account cashAccount = accountRepository.findByCodeAndCompanyId("1010", companyId).orElse(null);
            Account arAccount = accountRepository.findByCodeAndCompanyId("1020", companyId).orElse(null);
            Account salesRevenueAccount = accountRepository.findByCodeAndCompanyId("4010", companyId).orElse(null);

            if (cashAccount == null || salesRevenueAccount == null) {
                log.warn("Skipping POS Finance GL JournalEntry for {}: required accounts (1010/4010) not found for company {}", event.getTransactionNo(), companyId);
                return;
            }

            boolean isReturn = "RETURN".equalsIgnoreCase(event.getTransactionType());

            JournalEntry journal = new JournalEntry();
            journal.setCompanyId(companyId);
            journal.setPostingDate(LocalDate.now());
            journal.setReferenceType("POS_TRANSACTION");
            journal.setReferenceId(event.getTransactionId());
            journal.setStatus(JournalStatus.POSTED);

            List<JournalEntryLine> lines = new ArrayList<>();
            BigDecimal totalPosted = BigDecimal.ZERO;

            for (PosTransactionCompletedEvent.PaymentDto payment : event.getPayments()) {
                if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal amount = payment.getAmount();
                boolean isDue = "DUE".equalsIgnoreCase(payment.getPaymentMode());
                Account settlementAccount = (isDue && arAccount != null) ? arAccount : cashAccount;

                JournalEntryLine line = new JournalEntryLine();
                line.setJournalEntry(journal);
                line.setCompanyId(companyId);
                line.setAccount(settlementAccount);
                if (isReturn) {
                    line.setDebit(BigDecimal.ZERO);
                    line.setCredit(amount);
                } else {
                    line.setDebit(amount);
                    line.setCredit(BigDecimal.ZERO);
                }
                lines.add(line);
                totalPosted = totalPosted.add(amount);
            }

            if (totalPosted.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            JournalEntryLine revenueLine = new JournalEntryLine();
            revenueLine.setJournalEntry(journal);
            revenueLine.setCompanyId(companyId);
            revenueLine.setAccount(salesRevenueAccount);
            if (isReturn) {
                revenueLine.setDebit(totalPosted);
                revenueLine.setCredit(BigDecimal.ZERO);
            } else {
                revenueLine.setDebit(BigDecimal.ZERO);
                revenueLine.setCredit(totalPosted);
            }
            lines.add(revenueLine);

            journal.setLines(lines);
            journalEntryService.save(journal);

            log.info("Finance GL JournalEntry recorded successfully for POS Transaction: {}", event.getTransactionNo());
        } catch (Exception ex) {
            log.warn("Could not auto-post Finance GL JournalEntry for POS Transaction {}: {}", event.getTransactionNo(), ex.getMessage());
        }
    }
}
