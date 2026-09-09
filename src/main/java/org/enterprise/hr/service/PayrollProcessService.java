package org.enterprise.hr.service;

import org.enterprise.hr.dto.PayrollProcessDto;
import org.enterprise.hr.entity.Employee;
import org.enterprise.hr.entity.EmployeeSalary;
import org.enterprise.hr.entity.PayrollProcess;
import org.enterprise.hr.entity.Payslip;
import org.enterprise.hr.entity.PayslipComponent;
import org.enterprise.hr.entity.SalaryComponent;
import org.enterprise.hr.repository.EmployeeRepository;
import org.enterprise.hr.repository.EmployeeSalaryRepository;
import org.enterprise.hr.repository.PayrollProcessRepository;
import org.enterprise.hr.repository.PayslipComponentRepository;
import org.enterprise.hr.repository.PayslipRepository;
import org.enterprise.hr.repository.SalaryComponentRepository;
import org.enterprise.hr.repository.LoanInstallmentRepository;
import org.enterprise.hr.repository.EmployeeLoanRepository;
import org.enterprise.hr.entity.LoanInstallment;
import org.enterprise.hr.entity.EmployeeLoan;
import org.enterprise.workflow.dto.WorkflowStartRequest;
import org.enterprise.workflow.entity.WorkflowInstance;
import org.enterprise.workflow.service.WorkflowService;
import org.enterprise.workflow.event.WorkflowStatusEvent;
import org.springframework.context.event.EventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollProcessService {

    private final PayrollProcessRepository repository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final PayslipRepository payslipRepository;
    private final PayslipComponentRepository payslipComponentRepository;
    private final WorkflowService workflowService;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final EmployeeLoanRepository employeeLoanRepository;

    public PayrollProcessDto create(PayrollProcessDto dto) {
        PayrollProcess entity = new PayrollProcess();
        mapDtoToEntity(dto, entity);
        
        entity.setTotalEarning(0.0);
        entity.setTotalDeduction(0.0);
        entity.setNetPayment(0.0);
        entity.setStatus("Generated");
        
        entity = repository.save(entity);

        List<Employee> activeEmployees = employeeRepository.findByActiveTrue();
        List<SalaryComponent> globalComponents = salaryComponentRepository.findAll();

        double processTotalEarning = 0.0;
        double processTotalDeduction = 0.0;

        for (Employee employee : activeEmployees) {
            Optional<EmployeeSalary> optSalary = employeeSalaryRepository.findByEmployeeId(employee.getId());
            if (optSalary.isEmpty()) {
                continue;
            }

            EmployeeSalary empSalary = optSalary.get();
            double grossSalary = empSalary.getGrossSalary() != null ? empSalary.getGrossSalary() : 0.0;

            Payslip payslip = new Payslip();
            payslip.setEmployee(employee);
            payslip.setPayrollProcess(entity);
            payslip.setProcessMonth(entity.getProcessMonth());
            payslip.setProcessYear(entity.getProcessYear());
            payslip.setGrossSalary(grossSalary);
            payslip.setStatus("Generated");
            
            payslip = payslipRepository.save(payslip);

            double payslipEarning = 0.0;
            double payslipDeduction = 0.0;

            for (SalaryComponent comp : globalComponents) {
                double amount = 0.0;
                if ("Percentage".equalsIgnoreCase(comp.getCalculationMethod())) {
                    amount = grossSalary * ((comp.getAmount() != null ? comp.getAmount() : 0.0) / 100.0);
                } else {
                    amount = comp.getAmount() != null ? comp.getAmount() : 0.0;
                }

                PayslipComponent pComponent = new PayslipComponent();
                pComponent.setPayslip(payslip);
                pComponent.setSalaryComponent(comp);
                pComponent.setType(comp.getType());
                pComponent.setAmount(amount);

                if ("Earning".equalsIgnoreCase(comp.getType())) {
                    payslipEarning += amount;
                } else if ("Deduction".equalsIgnoreCase(comp.getType())) {
                    payslipDeduction += amount;
                }

                payslipComponentRepository.save(pComponent);
            }

            // Deduct pending loan installments
            List<LoanInstallment> pendingInstallments = loanInstallmentRepository.findByLoan_Employee_IdAndStatus(employee.getId(), "Pending");
            for (LoanInstallment inst : pendingInstallments) {
                if (inst.getDueDate().getYear() < entity.getProcessYear() || 
                   (inst.getDueDate().getYear() == entity.getProcessYear() && inst.getDueDate().getMonthValue() <= entity.getProcessMonth())) {
                    
                    PayslipComponent loanComp = new PayslipComponent();
                    loanComp.setPayslip(payslip);
                    loanComp.setType("Deduction");
                    loanComp.setAmount(inst.getAmount());
                    payslipComponentRepository.save(loanComp);
                    
                    payslipDeduction += inst.getAmount();

                    inst.setStatus("Paid");
                    loanInstallmentRepository.save(inst);
                    
                    EmployeeLoan loan = inst.getLoan();
                    loan.setPaidMonths((loan.getPaidMonths() == null ? 0 : loan.getPaidMonths()) + 1);
                    if (loan.getPaidMonths() >= loan.getMonths()) {
                        loan.setStatus("Closed");
                    }
                    employeeLoanRepository.save(loan);
                }
            }

            payslip.setTotalEarning(payslipEarning);
            payslip.setTotalDeduction(payslipDeduction);
            payslip.setNetPayable(payslipEarning - payslipDeduction);
            payslipRepository.save(payslip);

            processTotalEarning += payslipEarning;
            processTotalDeduction += payslipDeduction;
        }

        entity.setTotalEarning(processTotalEarning);
        entity.setTotalDeduction(processTotalDeduction);
        entity.setNetPayment(processTotalEarning - processTotalDeduction);
        entity.setStatus("PENDING"); // Pending approval
        entity = repository.save(entity);

        WorkflowStartRequest workflowReq = new WorkflowStartRequest();
        workflowReq.setWorkflowCode("PAYROLL_APPROVAL");
        workflowReq.setEntityId(entity.getId());
        workflowReq.setEntityName("PayrollProcess");
        workflowReq.setDocumentNo("PR-" + entity.getProcessMonth() + "-" + entity.getProcessYear());
        workflowReq.setAmount(java.math.BigDecimal.valueOf(entity.getNetPayment()));

        try {
            WorkflowInstance instance = workflowService.startWorkflow(workflowReq);
            entity.setWorkflowInstanceId(instance.getId());
            entity = repository.save(entity);
        } catch (Exception e) {
            System.err.println("Failed to start workflow: " + e.getMessage());
        }

        return mapEntityToDto(entity);
    }

    public PayrollProcessDto update(Long id, PayrollProcessDto dto) {
        PayrollProcess entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayrollProcess not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public PayrollProcessDto getById(Long id) {
        PayrollProcess entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayrollProcess not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<PayrollProcessDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(PayrollProcessDto dto, PayrollProcess entity) {
        entity.setProcessDate(dto.getProcessDate());
        entity.setProcessMonth(dto.getProcessMonth());
        entity.setProcessYear(dto.getProcessYear());
        entity.setStatus(dto.getStatus());
        entity.setTotalEarning(dto.getTotalEarning());
        entity.setTotalDeduction(dto.getTotalDeduction());
        entity.setNetPayment(dto.getNetPayment());
        entity.setWorkflowInstanceId(dto.getWorkflowInstanceId());
    }

    private PayrollProcessDto mapEntityToDto(PayrollProcess entity) {
        PayrollProcessDto dto = new PayrollProcessDto();
        dto.setId(entity.getId());
        dto.setProcessDate(entity.getProcessDate());
        dto.setProcessMonth(entity.getProcessMonth());
        dto.setProcessYear(entity.getProcessYear());
        dto.setStatus(entity.getStatus());
        dto.setTotalEarning(entity.getTotalEarning());
        dto.setTotalDeduction(entity.getTotalDeduction());
        dto.setNetPayment(entity.getNetPayment());
        dto.setWorkflowInstanceId(entity.getWorkflowInstanceId());
        return dto;
    }

    @EventListener
    @Transactional
    public void handleWorkflowStatusChange(WorkflowStatusEvent event) {
        if ("PayrollProcess".equals(event.getEntityName())) {
            repository.findById(event.getEntityId()).ifPresent(pr -> {
                if ("APPROVED".equals(event.getStatus())) {
                    pr.setStatus("Approved");
                } else if ("REJECTED".equals(event.getStatus())) {
                    pr.setStatus("Rejected");
                }
                repository.save(pr);
            });
        }
    }
}
