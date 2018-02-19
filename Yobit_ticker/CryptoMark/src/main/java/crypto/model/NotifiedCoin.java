package crypto.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Created by bthiru on 1/21/2018.
 */
public class NotifiedCoin implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    BigDecimal diffVol;
    BigDecimal diffTrx;

    public BigDecimal getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(BigDecimal percentChange) {
        this.percentChange = percentChange;
    }

    BigDecimal percentChange;

    public boolean isBotInvolved() {
        return botInvolved;
    }

    public void setBotInvolved(boolean botInvolved) {
        this.botInvolved = botInvolved;
    }

    boolean botInvolved;
    public Set<BigDecimal> getDayPriceTrack() {
        return dayPriceTrack;
    }

    public void setDayPriceTrack(Set<BigDecimal> dayPriceTrack) {
        this.dayPriceTrack = dayPriceTrack;
    }

    public List<TradeInfo> getTradeInfos() {
        return tradeInfos;
    }

    public void setTradeInfos(List<TradeInfo> tradeInfos) {
        this.tradeInfos = tradeInfos;
    }

    public Long getBaseUpdated() {
        return BaseUpdated;
    }

    public void setBaseUpdated(Long baseUpdated) {
        BaseUpdated = baseUpdated;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    Set<BigDecimal> dayPriceTrack;
    List<TradeInfo> tradeInfos;
    public BigDecimal getOldVol() {
        return oldVol;
    }

    public void setOldVol(BigDecimal oldVol) {
        this.oldVol = oldVol;
    }

    public BigDecimal getNewVol() {
        return newVol;
    }

    public void setNewVol(BigDecimal newVol) {
        this.newVol = newVol;
    }

    BigDecimal oldVol;
    BigDecimal newVol;
    Long lastUpdated;
    Long BaseUpdated;
    BigDecimal percentage;
    String trend;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    int rank;

    public int getTradeRank() {
        return tradeRank;
    }

    public void setTradeRank(int tradeRank) {
        this.tradeRank = tradeRank;
    }

    int tradeRank;
    String pairCoin;
    String currency = "BTC";

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    BigDecimal currentPrice;

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    BigDecimal basePrice;

    @Id
    String pairId;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public BigDecimal getDiffVol() {
        return diffVol;
    }

    public void setDiffVol(BigDecimal diffVol) {
        this.diffVol = diffVol;
    }

    public BigDecimal getDiffTrx() {
        return diffTrx;
    }

    public void setDiffTrx(BigDecimal diffTrx) {
        this.diffTrx = diffTrx;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
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
