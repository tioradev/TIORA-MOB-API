package com.tiora.mob.service;

import com.tiora.mob.entity.Branch;
import com.tiora.mob.entity.Branch.SalonType;
import com.tiora.mob.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;

    public List<Branch> getBranchesBySalonType(SalonType salonType) {
        return branchRepository.findBySalonType(salonType);
    }
}
