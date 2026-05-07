package org.enterprise.workflow.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.workflow.dto.WorkflowActionRequest;
import org.enterprise.workflow.dto.WorkflowStartRequest;
import org.enterprise.workflow.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    // =========================
    // START WORKFLOW
    // =========================

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('WORKFLOW_START')")
    public ResponseEntity<?> start(
            @RequestBody WorkflowStartRequest request
    ) {

        return ResponseEntity.ok(
                workflowService.startWorkflow(request)
        );
    }

    // =========================
    // APPROVE
    // =========================

    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('WORKFLOW_APPROVE')")
    public ResponseEntity<?> approve(
            @RequestBody WorkflowActionRequest request
    ) {

        workflowService.approve(request);

        return ResponseEntity.ok("Approved");
    }

    // =========================
    // REJECT
    // =========================

    @PostMapping("/reject")
    @PreAuthorize("hasAuthority('WORKFLOW_REJECT')")
    public ResponseEntity<?> reject(
            @RequestBody WorkflowActionRequest request
    ) {

        workflowService.reject(request);

        return ResponseEntity.ok("Rejected");
    }

    // =========================
    // MY TASKS
    // =========================

    @GetMapping("/my-tasks")
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ResponseEntity<?> myTasks() {

        return ResponseEntity.ok(
                workflowService.myPendingTasks()
        );
    }
}