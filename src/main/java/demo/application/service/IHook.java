package demo.application.service;




import demo.application.domain.Hook;
import io.beapi.api.domain.User;
import java.util.List;
import java.util.Optional;

public interface IHook {

    public Hook save(Hook hook);
    public void deleteById(Long id);
    List<Hook> findByUser(User user);
    Optional<Hook> findById(Long id);
    Hook findByServiceAndUser(String service, User user);
    List<Hook> findByEnabledTrueAndService(String service);
}
