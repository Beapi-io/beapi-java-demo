package demo.application.controller;


import demo.application.domain.Company;
import demo.application.service.CompanyService;
import io.beapi.api.controller.BeapiRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


@Controller("company")
public class CompanyController extends BeapiRequestHandler{

	@Autowired
	private CompanyService compService;

	public Company show(HttpServletRequest request, HttpServletResponse response){
		Long id = Long.parseLong(this.params.get("id"));
		Company comp = compService.findById(id);
		if (Objects.nonNull(comp)) {
			return comp;
		}
		return null;
    }


	public Company create(HttpServletRequest request, HttpServletResponse response){
		Company comp = new Company();
		comp.setName(this.params.get("name"));

		// todo : need rollback upon fail
		return compService.save(comp);
	}

	public Company update(HttpServletRequest request, HttpServletResponse response){
		Long id = Long.parseLong(this.params.get("id"));
		System.out.println("id : "+id);

		Company comp = compService.findById(id);

		if (Objects.nonNull(comp)) {
			comp.setName(this.params.get("name"));

			// todo : need rollback upon fail
			if(Objects.nonNull(compService.save(comp))){
				return comp;
			}
		}
		return null;
	}

	public LinkedHashMap delete(HttpServletRequest request, HttpServletResponse response) {
		Company comp;
		Long id = Long.parseLong(this.params.get("id"));
		comp = compService.findById(id);
		if(Objects.nonNull(comp)){
			compService.deleteById(id);

			LinkedHashMap<String,Long> result = new LinkedHashMap<String, Long>();
			result.put("id",id);

			return result;
		}
		return null;
	}

}
