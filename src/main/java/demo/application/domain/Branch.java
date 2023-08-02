package demo.application.domain;


//import lombok.Getter;
//import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "branch")
//@Getter
//@Setter
public class Branch implements Serializable {

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
    @JoinColumn(nullable = false, name = "company_id")
    private Company companyId;

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

    public Long getCompanyId(){
        return this.companyId.getId();
    }

    public void setCompanyId(Company comp){
        this.companyId = comp;
    }

}

