package demo.application.service;

import demo.application.domain.Branch;
import demo.application.repositories.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class BranchService implements IBranch {

    BranchRepository branchrepo;

    @Autowired
    public BranchService(BranchRepository branchrepo) {
        this.branchrepo = branchrepo;
    }

    //@Override
    public Branch save(Branch branch){
        // TODO Auto-generated method stub
        branchrepo.save(branch);
        branchrepo.flush();
        return branch;
    }

    @Override
    public void deleteById(Long id){
        // TODO Auto-generated method stub
        branchrepo.deleteById(id);
        branchrepo.flush();
    }

    //@Override
    //public Branch findById(int id) {
    //    return branchrepo.findById(Long.valueOf(id));
    //}


    public Branch findById(Long id){
        // TODO Auto-generated method stub
        return branchrepo.findById(id).get();
    }

}
