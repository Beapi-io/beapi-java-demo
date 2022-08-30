package demo.application.service;


import demo.application.domain.Branch;

public interface IBranch {

    Branch save(Branch branch);
    void deleteById(Long id);
    Branch findById(Long id);

}
