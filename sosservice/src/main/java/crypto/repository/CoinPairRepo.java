package crypto.repository;
import crypto.model.PairInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * Created by bthiru on 1/24/2018.
 */
@Component
public interface CoinPairRepo extends CrudRepository<PairInfo, String> {

}
