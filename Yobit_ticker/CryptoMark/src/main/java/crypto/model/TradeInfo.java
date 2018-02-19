package crypto.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by bthiru on 2/6/2018.
 */
public class TradeInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String type;
    BigDecimal price;
    BigDecimal amount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    Long lastUpdated;
}
