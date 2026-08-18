package org.enterprise.reportengine.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.reportengine.entity.ReportMaster;
import org.enterprise.reportengine.repository.ReportMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportMasterService {

    private final ReportMasterRepository reportMasterRepository;

    public List<ReportMaster> getAll() {
        return reportMasterRepository.findAll();
    }

    public ReportMaster getById(Long id) {
        return reportMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report Master not found with ID: " + id));
    }

    @Transactional
    public ReportMaster save(ReportMaster reportMaster) {
        if (reportMaster.getParameters() != null) {
            reportMaster.setParameters(reportMaster.getParameters());
        }
        return reportMasterRepository.save(reportMaster);
    }

    @Transactional
    public void delete(Long id) {
        if (!reportMasterRepository.existsById(id)) {
            throw new RuntimeException("Report Master not found with ID: " + id);
        }
        reportMasterRepository.deleteById(id);
    }
}
