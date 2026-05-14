package org.enterprise.hr.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final PayrollProcessRepository payrollProcessRepository;
    private final PayslipRepository payslipRepository;
    private final PayslipComponentRepository payslipComponentRepository;

    public PayrollProcess processSalary(Integer year, Integer month) {

        // Check if payroll is already processed
        Optional<PayrollProcess> existingProcess = payrollProcessRepository.findByProcessYearAndProcessMonth(year,
                month);
        if (existingProcess.isPresent()) {
            throw new RuntimeException("Payroll for " + month + "/" + year + " is already processed.");
        }

        PayrollProcess process = new PayrollProcess();
        process.setProcessDate(LocalDate.now());
        process.setProcessMonth(month);
        process.setProcessYear(year);
        process.setStatus("Draft");

        process = payrollProcessRepository.save(process);

        double totalProcessEarning = 0.0;
        double totalProcessDeduction = 0.0;
        double totalProcessNet = 0.0;

        List<Employee> activeEmployees = employeeRepository.findAll(); // Should filter by active=true, but using
                                                                       // findAll for simplicity assuming we have active
                                                                       // filter in repo or doing it here
        List<SalaryComponent> components = salaryComponentRepository.findAll();

        for (Employee employee : activeEmployees) {
            if (employee.getActive() == null || !employee.getActive()) {
                continue;
            }

            Optional<EmployeeSalary> optSalary = employeeSalaryRepository.findByEmployeeId(employee.getId());
            if (optSalary.isEmpty()) {
                continue;
            }

            EmployeeSalary employeeSalary = optSalary.get();
            double gross = employeeSalary.getGrossSalary() != null ? employeeSalary.getGrossSalary() : 0.0;

            Payslip payslip = new Payslip();
            payslip.setEmployee(employee);
            payslip.setPayrollProcess(process);
            payslip.setProcessMonth(month);
            payslip.setProcessYear(year);
            payslip.setGrossSalary(gross);
            payslip.setStatus("Draft");

            payslip = payslipRepository.save(payslip);

            double totalEarning = 0.0;
            double totalDeduction = 0.0;

            for (SalaryComponent component : components) {
                PayslipComponent pc = new PayslipComponent();
                pc.setPayslip(payslip);
                pc.setSalaryComponent(component);
                pc.setType(component.getType());

                double amount = 0.0;
                if ("Percentage".equalsIgnoreCase(component.getCalculationMethod())) {
                    amount = (gross * component.getAmount()) / 100.0;
                } else {
                    amount = component.getAmount();
                }

                pc.setAmount(amount);
                payslipComponentRepository.save(pc);

                if ("Earning".equalsIgnoreCase(component.getType())) {
                    totalEarning += amount;
                } else if ("Deduction".equalsIgnoreCase(component.getType())) {
                    totalDeduction += amount;
                }
            }

            payslip.setTotalEarning(totalEarning);
            payslip.setTotalDeduction(totalDeduction);
            payslip.setNetPayable((gross + totalEarning) - totalDeduction);

            // Note: Simplistic calculation where base is gross, and earning/deductions are
            // added/subtracted on top.
            // Often "Basic" is part of the components that sum up to Gross. For this
            // example, we'll keep it simple.

            payslipRepository.save(payslip);

            totalProcessEarning += totalEarning;
            totalProcessDeduction += totalDeduction;
            totalProcessNet += payslip.getNetPayable();
        }

        process.setTotalEarning(totalProcessEarning);
        process.setTotalDeduction(totalProcessDeduction);
        process.setNetPayment(totalProcessNet);

        return payrollProcessRepository.save(process);
    }
}
