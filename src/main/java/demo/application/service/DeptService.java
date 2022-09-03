package demo.application.service;


import demo.application.domain.Dept;
import demo.application.repositories.DeptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DeptService implements IDept {

    DeptRepository deptrepo;

    @Autowired
    public DeptService(DeptRepository deptrepo) {
        this.deptrepo = deptrepo;
    }

    //@Override
    public Dept save(Dept dept){
        // TODO Auto-generated method stub
        deptrepo.save(dept);
        deptrepo.flush();
        return dept;
    }

    @Override
    public void deleteById(Long id){
        // TODO Auto-generated method stub
        deptrepo.deleteById(id);
        deptrepo.flush();
    }

    //@Override
    //public Dept findById(int id) {
    //    return deptrepo.findById(Long.valueOf(id));
    //}


    //@Override
    public Dept findById(Long id){
        // TODO Auto-generated method stub
        return deptrepo.findById(id).get();
    }

}
