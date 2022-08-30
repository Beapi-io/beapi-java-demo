package demo.application.repositories;

import demo.application.domain.Branch;
import demo.application.domain.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface DeptRepository extends JpaRepository<Dept, Long> {

    public Dept save(Dept dept);
    public void deleteById(Long id);
    //Dept findById(Long id);


}