package demo.application.service;




import demo.application.domain.Hook;
import demo.application.domain.User;
import java.util.List;

public interface IHook {

    public Hook save(Hook hook);
    public void deleteById(Long id);
    List<Hook> findByUser(User user);
    Hook findByServiceAndUser(String service, User user);
    List<Hook> findByEnabledTrueAndService(String service);
}
