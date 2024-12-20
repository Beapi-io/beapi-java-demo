package demo.application.service;


import demo.application.domain.Test;

public interface ITest {

    public Test save(Test test);
    public void deleteById(Long id);
    Test findById(Long id);

}

