package com.tiora.mob.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.tiora.mob.entity.Employee;

import com.tiora.mob.entity.EmployeeLeave;
import com.tiora.mob.repository.EmployeeLeaveRepository;
import com.tiora.mob.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeLeaveService {

    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    public Employee findEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId).orElse(null);
    }

    public boolean existsLeaveForEmployeeAndDate(Long employeeId, java.time.LocalDate date) {
        // Find all leaves for employee and date
        java.util.List<com.tiora.mob.entity.EmployeeLeave> leaves = employeeLeaveRepository.findByEmployee_EmployeeIdAndStartDate(employeeId, date);
        for (com.tiora.mob.entity.EmployeeLeave leave : leaves) {
            if (leave.getStatus() == com.tiora.mob.entity.EmployeeLeave.LeaveStatus.PENDING ||
                leave.getStatus() == com.tiora.mob.entity.EmployeeLeave.LeaveStatus.APPROVED) {
                return true;
            }
        }
        return false;
    }
    private final EmployeeLeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeLeaveService(EmployeeLeaveRepository leaveRepository, EmployeeRepository employeeRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }

    public EmployeeLeave requestLeave(Long employeeId, EmployeeLeave leave) {
        leave.setEmployee(employeeRepository.findById(employeeId).orElseThrow());
        leave.setStatus(EmployeeLeave.LeaveStatus.PENDING);
        return leaveRepository.save(leave);
    }

    public List<EmployeeLeave> getLeavesByEmployee(Long employeeId) {
        return leaveRepository.findByEmployee_EmployeeId(employeeId);
    }

    public List<EmployeeLeave> getAllLeaves() {
        return leaveRepository.findAll();
    }
}
