package crypto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bthiru on 1/21/2018.
 */
public class PairInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

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
    int readEntry =0;

    public int getReadEntry() {
        return readEntry;
    }

    public void setReadEntry(int readEntry) {
        this.readEntry = readEntry;
    }

}
