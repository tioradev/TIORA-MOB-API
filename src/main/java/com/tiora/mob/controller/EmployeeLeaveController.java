package com.tiora.mob.controller;

import com.tiora.mob.entity.EmployeeLeave;
import com.tiora.mob.dto.request.EmployeeLeaveRequestDTO;
import com.tiora.mob.dto.response.EmployeeLeaveResponseDTO;
import com.tiora.mob.service.EmployeeLeaveService;
import com.tiora.mob.entity.Employee;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/mobile/barber-leave")
public class EmployeeLeaveController {
    @Autowired
    private EmployeeLeaveService leaveService;

    @PostMapping("/barber-leave/request")
    public ResponseEntity<?> requestLeave(@RequestBody EmployeeLeaveRequestDTO request) {
        // Check if employee exists
        Employee employee = leaveService.findEmployeeById(request.getEmployeeId());
        if (employee == null) {
            return ResponseEntity.badRequest().body("You entered employee id invalid");
        }
        java.time.LocalDate startDate = java.time.LocalDate.parse(request.getStartDate());
        // Check for duplicate leave request for same date
        boolean exists = leaveService.existsLeaveForEmployeeAndDate(request.getEmployeeId(), startDate);
        if (exists) {
            return ResponseEntity.badRequest().body("You have previoulsy requested leave for the same date");
        }
        EmployeeLeave leave = new EmployeeLeave();
        leave.setEmployee(employee);
        leave.setStartDate(startDate);
        leave.setEndDate(java.time.LocalDate.parse(request.getEndDate()));
        leave.setLeaveReason(request.getLeaveReason());
        leave.setStatus(EmployeeLeave.LeaveStatus.PENDING);
        EmployeeLeave savedLeave = leaveService.requestLeave(request.getEmployeeId(), leave);
        EmployeeLeaveResponseDTO response = new EmployeeLeaveResponseDTO();
        response.setId(savedLeave.getId());
        response.setEmployeeId(savedLeave.getEmployee().getEmployeeId());
        response.setStartDate(savedLeave.getStartDate().toString());
        response.setEndDate(savedLeave.getEndDate().toString());
        response.setLeaveReason(savedLeave.getLeaveReason());
        response.setStatus(savedLeave.getStatus().name());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/barber-leave/list")
    public ResponseEntity<List<EmployeeLeaveResponseDTO>> getLeaves(@RequestParam Long employeeId) {
        List<EmployeeLeave> leaves = leaveService.getLeavesByEmployee(employeeId);
        List<EmployeeLeaveResponseDTO> responseList = leaves.stream().map(leave -> {
            EmployeeLeaveResponseDTO dto = new EmployeeLeaveResponseDTO();
            dto.setId(leave.getId());
            dto.setEmployeeId(leave.getEmployee().getEmployeeId());
            dto.setStartDate(leave.getStartDate().toString());
            dto.setEndDate(leave.getEndDate().toString());
            dto.setLeaveReason(leave.getLeaveReason());
            dto.setStatus(leave.getStatus().name());
            return dto;
        }).toList();
        return ResponseEntity.ok(responseList);
    }
}
