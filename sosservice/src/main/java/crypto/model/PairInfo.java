package crypto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

/**
 * Created by bthiru on 1/21/2018.
 */
@RedisHash("products")
public class PairInfo {

    @Id
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getActivePairId() {
        return activePairId;
    }

    public void setActivePairId(List<String> activePairId) {
        this.activePairId = activePairId;
    }

    public List<String> getInActivePairId() {
        return inActivePairId;
    }

    public void setInActivePairId(List<String> inActivePairId) {
        this.inActivePairId = inActivePairId;
    }

    List<String> activePairId;
    List<String> inActivePairId;
}
