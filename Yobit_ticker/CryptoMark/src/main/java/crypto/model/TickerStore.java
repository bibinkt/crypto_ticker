package crypto.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by bthiru on 1/21/2018.
 */
public class TickerStore implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    BigDecimal curentVol;
    BigDecimal recentTrxRate;
    BigDecimal low24H;
    BigDecimal high24H;
    Long lastUpdated;
    String primaryCoin;
    String pairCoin;

    @Id
    String pairId;

    public BigDecimal getCurentVol() {
        return curentVol;
    }

    public void setCurentVol(BigDecimal curentVol) {
        this.curentVol = curentVol;
    }

    public BigDecimal getRecentTrxRate() {
        return recentTrxRate;
    }

    public void setRecentTrxRate(BigDecimal recentTrxRate) {
        this.recentTrxRate = recentTrxRate;
    }

    public BigDecimal getLow24H() {
        return low24H;
    }

    public void setLow24H(BigDecimal low24H) {
        this.low24H = low24H;
    }

    public BigDecimal getHigh24H() {
        return high24H;
    }

    public void setHigh24H(BigDecimal high24H) {
        this.high24H = high24H;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getPrimaryCoin() {
        return primaryCoin;
    }

    public void setPrimaryCoin(String primaryCoin) {
        this.primaryCoin = primaryCoin;
    }

    public String getPairCoin() {
        return pairCoin;
    }

    public void setPairCoin(String pairCoin) {
        this.pairCoin = pairCoin;
    }

    public String getPairId() {
        return pairId;
    }

    public void setPairId(String pairId) {
        this.pairId = pairId;
    }
}
