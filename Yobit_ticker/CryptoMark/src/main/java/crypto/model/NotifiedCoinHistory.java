package crypto.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by bthiru on 1/21/2018.
 */
public class NotifiedCoinHistory implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    String pairId;
    Date date;
    BigDecimal baseVol;
    BigDecimal basePrice;
    Long BaseUpdated;
    int Rank;
    boolean botInvolved;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    boolean error;
    BigDecimal percentageOnAlert;
    List<TradeInfo> tradeInfoOnalert;

    BigDecimal maxPriceLast24h;
    BigDecimal maxPercentageChangeLast24h;

    BigDecimal currentPrice;
    BigDecimal currentEprice;

    public BigDecimal getCurrentEprice() {
        return currentEprice;
    }

    public void setCurrentEprice(BigDecimal currentEprice) {
        this.currentEprice = currentEprice;
    }

    BigDecimal currentPercentageChangeLast24h;
    BigDecimal currentVol;
    Long lastUpdated;

    String currency = "BTC";

    public String getPairId() {
        return pairId;
    }

    public void setPairId(String pairId) {
        this.pairId = pairId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getBaseVol() {
        return baseVol;
    }

    public void setBaseVol(BigDecimal baseVol) {
        this.baseVol = baseVol;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Long getBaseUpdated() {
        return BaseUpdated;
    }

    public void setBaseUpdated(Long baseUpdated) {
        BaseUpdated = baseUpdated;
    }

    public int getRank() {
        return Rank;
    }

    public void setRank(int rank) {
        Rank = rank;
    }

    public boolean isBotInvolved() {
        return botInvolved;
    }

    public void setBotInvolved(boolean botInvolved) {
        this.botInvolved = botInvolved;
    }

    public BigDecimal getPercentageOnAlert() {
        return percentageOnAlert;
    }

    public void setPercentageOnAlert(BigDecimal percentageOnAlert) {
        this.percentageOnAlert = percentageOnAlert;
    }

    public List<TradeInfo> getTradeInfoOnalert() {
        return tradeInfoOnalert;
    }

    public void setTradeInfoOnalert(List<TradeInfo> tradeInfoOnalert) {
        this.tradeInfoOnalert = tradeInfoOnalert;
    }

    public BigDecimal getMaxPriceLast24h() {
        return maxPriceLast24h;
    }

    public void setMaxPriceLast24h(BigDecimal maxPriceLast24h) {
        this.maxPriceLast24h = maxPriceLast24h;
    }

    public BigDecimal getMaxPercentageChangeLast24h() {
        return maxPercentageChangeLast24h;
    }

    public void setMaxPercentageChangeLast24h(BigDecimal maxPercentageChangeLast24h) {
        this.maxPercentageChangeLast24h = maxPercentageChangeLast24h;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCurrentPercentageChangeLast24h() {
        return currentPercentageChangeLast24h;
    }

    public void setCurrentPercentageChangeLast24h(BigDecimal currentPercentageChangeLast24h) {
        this.currentPercentageChangeLast24h = currentPercentageChangeLast24h;
    }

    public BigDecimal getCurrentVol() {
        return currentVol;
    }

    public void setCurrentVol(BigDecimal currentVol) {
        this.currentVol = currentVol;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
