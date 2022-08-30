package demo.application.service;

import demo.application.domain.Company;
import demo.application.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class CompanyService implements ICompany {

    CompanyRepository comprepo;

    @Autowired
    public CompanyService(CompanyRepository comprepo) {
        this.comprepo = comprepo;
    }

    //@Override
    public demo.application.domain.Company save(demo.application.domain.Company company){
        // TODO Auto-generated method stub
        return comprepo.save(company);
    }

    @Override
    public void deleteById(Long id){
        // TODO Auto-generated method stub
        comprepo.deleteById(id);
    }

    //@Override
    //public Company findById(int id) {
    //    return comprepo.findById(Long.valueOf(id));
    //}


    //@Override
    public demo.application.domain.Company findById(Long id){
        // TODO Auto-generated method stub
        return comprepo.findById(id).get();
    }

}
