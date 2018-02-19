package crypto.repository;

import crypto.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

@Repository
public class RedisRepositoryImpl {
    private static final String KEY = "pairInfo";
    private static final String KEY_Ticker = "ticker";
    private static final String KEY_NOTOFICATION = "notification";
    private static final String KEY_NOTOFICATION_H = "notification_h";
    private static final String KEY_TRADE_INFO= "tradeinfo";

    private RedisTemplate<String, Object> redisTemplate;
    private HashOperations hashOperations;
    
    @Autowired
    public RedisRepositoryImpl(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init(){
        hashOperations = redisTemplate.opsForHash();
    }

    public void addTradeInfo(final TradeInfoStore coin) {

        hashOperations.put(KEY_TRADE_INFO, coin.getId(), coin);
    }
    public void addNotification(final NotifiedCoin coin) {

        hashOperations.put(KEY_NOTOFICATION, coin.getPairId(), coin);
    }
    public void addNotificationH(final NotifiedCoinHistory coin) {

        hashOperations.put(KEY_NOTOFICATION_H, coin.getPairId(), coin);
    }
    public void addPairInfo(final PairInfo movie) {

        hashOperations.put(KEY, movie.getId(), movie);
    }
    public void addTicker(final TickerStore ticker) {

        hashOperations.put(KEY_Ticker, ticker.getPairId(), ticker);
    }


    public TradeInfoStore findTradeInfo(final String id){
        return (TradeInfoStore) hashOperations.get(KEY_TRADE_INFO, id);
    }
    public PairInfo findPairInfo(final String id){
        return (PairInfo) hashOperations.get(KEY, id);
    }
    public TickerStore findTicker(final String id){
        return (TickerStore) hashOperations.get(KEY_Ticker, id);
    }
    public NotifiedCoin findNotifiedCoin(final String id){
        return (NotifiedCoin) hashOperations.get(KEY_NOTOFICATION, id);
    }

    public Map<Object, Object> findAllPairInfo(){
        return hashOperations.entries(KEY);
    }
    public Map<Object, Object> findAllTicker(){
        return hashOperations.entries(KEY_Ticker);
    }
    public Map<Object, NotifiedCoin> findAllNotification(){
        return hashOperations.entries(KEY_NOTOFICATION);
    }
    public Map<Object, NotifiedCoinHistory> findAllNotificationH(){
        return hashOperations.entries(KEY_NOTOFICATION_H);
    }
    public  void deleteTicker(String key){
        hashOperations.delete(KEY_Ticker, key);
    }
    public  void deletePairInfo(String key){
        hashOperations.delete(KEY, key);
    }

    public void truncate(String key){
        redisTemplate.delete(key);
    }

    public void deleteNotifiedCoin(final String id){
         hashOperations.delete(KEY_NOTOFICATION, id);
    }

}
