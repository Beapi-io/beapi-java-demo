package demo.application.repositories;


import demo.application.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    public Company save(Company company);
    public void deleteById(Long id);
    //Company findById(Long id);

}