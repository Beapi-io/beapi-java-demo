package demo.application.repositories;

import demo.application.domain.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {


    public Test save(Test test);
    public void deleteById(Long id);
    //Dept findById(Long id);

}
