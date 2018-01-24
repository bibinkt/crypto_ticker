package crypto.repository;
import crypto.model.PairInfo;
import crypto.model.TickerStore;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * Created by bthiru on 1/24/2018.
 */
@Component
public class TickerRepo {

    /**
     * Created by jt on 1/10/17.
     */
    public interface ProductRepository extends CrudRepository<TickerStore, String> {
    }
}
