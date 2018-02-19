package crypto.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by bthiru on 2/6/2018.
 */
public class TradeInfoStore implements Serializable {

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


    public Map<String, List<TradeInfo>> getTradeInfo() {
        return tradeInfo;
    }

    public void setTradeInfo(Map<String, List<TradeInfo>> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }
    Map<String,List<TradeInfo>> tradeInfo;
}
