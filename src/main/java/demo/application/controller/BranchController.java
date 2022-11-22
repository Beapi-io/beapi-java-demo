package demo.application.controller;

import demo.application.domain.Branch;
import demo.application.domain.Company;
import demo.application.service.BranchService;
import demo.application.service.CompanyService;
import io.beapi.api.controller.BeapiRequestHandler;
import io.beapi.api.properties.ApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller("branch")
public class BranchController extends BeapiRequestHandler{

	@Autowired
	private BranchService branchService;

	@Autowired
	private CompanyService compService;

	public Branch show(HttpServletRequest request, HttpServletResponse response){
		Long id = Long.parseLong(params.get("id").toString());
		Branch branch = branchService.findById(id);

		if (Objects.nonNull(branch)) {
			return branch;
		}

		return null;
    }


	public Branch create(HttpServletRequest request, HttpServletResponse response){
		Company comp = compService.findById(Long.valueOf(this.params.get("companyId")));

		if(Objects.nonNull(comp)) {
			Branch branch = new Branch();
			branch.setName(this.params.get("name"));
			branch.setCompanyId(comp);
			return branchService.save(branch);
		}else{
			return null;
		}

	}

	public LinkedHashMap delete(HttpServletRequest request, HttpServletResponse response) {
		Branch branch;
		Long id = Long.valueOf(this.params.get("id"));

		branch = branchService.findById(id);
		if(Objects.nonNull(branch)){
			branchService.deleteById(id);

			LinkedHashMap<String,Long> result = new LinkedHashMap<String, Long>();
			result.put("id",id);
			return result;
		}

		return null;
	}


}
