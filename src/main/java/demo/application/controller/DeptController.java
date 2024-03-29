package demo.application.controller;

import demo.application.domain.Branch;
import demo.application.domain.Dept;
import demo.application.service.BranchService;
import demo.application.service.DeptService;
import io.beapi.api.controller.BeapiRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


@Controller("dept")
public class DeptController extends BeapiRequestHandler{

    @Autowired
    private BranchService branchService;

    @Autowired
    private DeptService deptService;

    public Dept show(HttpServletRequest request, HttpServletResponse response){
        Long id = Long.valueOf(this.params.get("id"));
        Dept dept = deptService.findById(id);
        if (Objects.nonNull(dept)) {
            return dept;
        } else {
            return null;
        }
    }

    public Dept create(HttpServletRequest request, HttpServletResponse response){
        Branch branch = branchService.findById(Long.valueOf(this.params.get("branchId")));
        if(Objects.nonNull(branch)) {
            Dept dept = new Dept();
            dept.setName(this.params.get("name"));
            dept.setBranchId(branch);
            return deptService.save(dept);
        }else{
            return null;
        }
        // todo : need rollback upon fail

    }

    public Dept update(HttpServletRequest request, HttpServletResponse response){
        Long id = Long.parseLong(this.params.get("id"));
        Dept dept = deptService.findById(id);
        if (Objects.nonNull(dept)) {
            dept.setName(this.params.get("name").toString());

            // todo : need rollback upon fail
            if(Objects.nonNull(deptService.save(dept))){
                return dept;
            }
        }
        return null;
    }

    public LinkedHashMap delete(HttpServletRequest request, HttpServletResponse response) {
        Dept dept;
        Long id = Long.parseLong(this.params.get("id"));
        dept = deptService.findById(id);
        if(Objects.nonNull(dept)){
            deptService.deleteById(id);
            //(flush: true, failOnError: true)

            LinkedHashMap<String,Long> result = new LinkedHashMap<String, Long>();
            result.put("id",id);
            return result;

        }
        return null;
    }


}

