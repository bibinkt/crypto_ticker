package crypto.repository;

import crypto.model.PairInfo;
import crypto.model.TickerStore;
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
    
    public void addPairInfo(final PairInfo movie) {

        hashOperations.put(KEY, movie.getId(), movie);
    }
    public void addTicker(final TickerStore ticker) {

        hashOperations.put(KEY_Ticker, ticker.getPairId(), ticker);
    }
    
    public PairInfo findPairInfo(final String id){
        return (PairInfo) hashOperations.get(KEY, id);
    }
    public TickerStore findTicker(final String id){
        return (TickerStore) hashOperations.get(KEY_Ticker, id);
    }

    public Map<Object, Object> findAllPairInfo(){
        return hashOperations.entries(KEY);
    }
    public Map<Object, Object> findAllTicker(){
        return hashOperations.entries(KEY_Ticker);
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

}
