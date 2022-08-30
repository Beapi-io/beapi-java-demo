package demo.application.repositories;

import demo.application.domain.Branch;
import demo.application.domain.Company;
import demo.application.domain.User;
import demo.application.domain.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    public Branch save(Branch branch);
    public void deleteById(Long id);
    //Branch findById(Long id);


}