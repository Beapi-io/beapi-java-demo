package demo.application.service;


import demo.application.domain.Company;

public interface ICompany {

    Company save(Company company);
    void deleteById(Long id);
    Company findById(Long id);

}
