package demo.application.service;


import demo.application.domain.Test;
import demo.application.repositories.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService implements ITest {

    TestRepository testrepo;

    @Autowired
    public TestService(TestRepository testrepo) {
        this.testrepo = testrepo;
    }


    //@Override
    public Test save(Test test){
        // TODO Auto-generated method stub
        testrepo.save(test);
        testrepo.flush();
        return test;
    }

    @Override
    public void deleteById(Long id){
        // TODO Auto-generated method stub
        testrepo.deleteById(id);
        testrepo.flush();
    }

    //@Override
    //public Dept findById(int id) {
    //    return deptrepo.findById(Long.valueOf(id));
    //}


    //@Override
    public List<Test> findAll(){
        // TODO Auto-generated method stub
        return testrepo.findAll();
    }

    //@Override
    public Test findById(Long id){
        // TODO Auto-generated method stub
        return testrepo.findById(id).get();
    }

    //@Override
    public void flush(){
        // TODO Auto-generated method stub
        testrepo.flush();
    }
}

