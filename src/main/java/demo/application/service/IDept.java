package demo.application.service;



import demo.application.domain.Dept;

public interface IDept {

    Dept save(Dept dept);
    void deleteById(Long id);
    Dept findById(Long id);


}
