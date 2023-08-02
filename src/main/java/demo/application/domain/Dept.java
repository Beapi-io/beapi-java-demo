package demo.application.domain;


//import lombok.Getter;
//import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dept")
//@Getter
//@Setter
public class Dept implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false, name = "version")
    private Long version;


    @Column(nullable = false, name = "name")
    String name;

    @ManyToOne
    @JoinColumn(nullable = false, name = "branch_id")
    private Branch branchId;

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public Long getVersion(){
        return version;
    }

    public void setVersion(Long version){
        this.version = version;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Long getBranchId(){
        return this.branchId.getId();
    }

    public void setBranchId(Branch branch){
        this.branchId = branch;
    }
}

