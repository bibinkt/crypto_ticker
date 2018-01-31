package crypto.service;


import com.fasterxml.jackson.databind.util.JSONPObject;
import crypto.model.PairInfo;
import crypto.model.TickerStore;
import crypto.repository.RedisRepositoryImpl;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.math.BigDecimal;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created by bthiru on 1/9/2018.
 */
@Component
public class CryptoService {

    @Autowired
    RedisRepositoryImpl redisRepository;

    @Autowired
    EmailService emailService;

    CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .build();
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    private RestTemplate restTemplate = null;

    public PairInfo activateCoinPair() {
        requestFactory.setHttpClient(httpClient);
        restTemplate =  new RestTemplate(requestFactory);
        PairInfo pairInfo = new PairInfo();
        JSONPObject obj = null;
        List<JSONPObject> pairs = null;
        obj = restTemplate.getForObject("https://yobit.net/api/3/info", JSONPObject.class);
        //pairs = (List<JSONPObject>) obj.getValue();
        List<MediaType> accept = new ArrayList<>();
        accept.add(MediaType.APPLICATION_JSON_UTF8);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(accept);
       // headers.add("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        HttpEntity<JSONPObject> requestEntity = new HttpEntity<JSONPObject>(headers);
       // ResponseEntity<JSONPObject> response1 = restTemplate.exchange("https://yobit.net/api/3/ticker/zmc_btc", HttpMethod.GET, requestEntity,JSONPObject.class);
        System.out.print("Bibin -->"+obj);
        return pairInfo;
    }

    @Scheduled(fixedRate = 60000*60)  // every 1 hour
    public void runAnalizerBotOnYobit() {
        System.out.println("Hey i am going to run . Current time is --> "+System.currentTimeMillis());
        getTickerUpdate();
        PairInfo pp = getPairInfo("1");
        List<String> coinPairList = pp.getActivePairId();
        List<String> coinNotificationList = new ArrayList<String>();
        TickerStore baseTicker = null;
        TickerStore currentTicker = null;
        boolean sendEmail = false;
        String text = "\n";
        text =text+" \n\n Hey @ Again me crypto Bot :-) . Below are some of the coin that i found a spike. " +
                "Am still improving myself to identify the ponzi coin for you \n " +
                "So do not buy anything with out doing your own analysis :-) \n\n";
        for(String coinPair:coinPairList){
            baseTicker = redisRepository.findTicker("base@"+coinPair);
            currentTicker = redisRepository.findTicker("current@"+coinPair);
            if(baseTicker != null && currentTicker!=null){
                BigDecimal diff = currentTicker.getCurentVol().subtract(baseTicker.getCurentVol());
                if(diff.compareTo(new BigDecimal(1.05))>0){
                    currentTicker.setPairId("base@"+coinPair);
                    redisRepository.addTicker(currentTicker);
                    sendEmail = true;
                    System.out.println("Hey man , I found out a spike on this coin ->"+coinPair);
                     text = text+ ""+coinPair.toUpperCase() +" has increased by "+diff;
                    text =text+"\n";
                    System.out.println("Old Value ->"+baseTicker.getCurentVol());
                    text =text+"Old Value ->"+baseTicker.getCurentVol();
                    text =text+"\n";
                    System.out.println("Current Value ->"+currentTicker.getCurentVol());
                    text =text+"Current Value ->"+currentTicker.getCurentVol();
                    text =text+"\n";
                    text =text+"----------------------------------------------------------------------\n";
                }
            }
        }
        if(sendEmail){
            System.out.println("Sending email with findings");
            emailService.sendMail("bibin.kt@gmail.com","Yobit Exchange PONZI COIN updates",text.replace("@","Bibin"));
            emailService.sendMail("haiprabeesh@gmail.com","Yobit Exchange PONZI COIN updates",text.replace("@","Prabeesh"));

        }
    }
    public  void getTickerUpdate() {
        //redisRepository.truncate("ticker");
        System.out.print("Getting the ticker update");
        ResponseEntity<String> response = null;
        PairInfo pp = getPairInfo("1");
        System.out.print("Payer info from cache -->"+pp.getActivePairId().size());
        String pairString ="";
        List<String> urlList = new ArrayList();
        List<String> coinList = new ArrayList();

        try {
            List<String> coinPairList = pp.getActivePairId();
            int i=1;
            int j=0;
            for(String pair:coinPairList) {
                coinList.add(pair);
                if(j==0) {
                    pairString = pair;
                }else{
                    pairString = pairString + "-" + pair;
                }
                j++;
                if(i%50==0) {
                    j=0;
                    String url = "https://yobit.net/api/3/ticker/"+pairString+"-error_pair?ignore_invalid=1";
                    //System.out.print("ticker Url -->"+url);
                    urlList.add(url);
                }
                i++;
                if(i==501){
                    //coinList.remove(pair);
                    break;
                }
            }

            JSONParser parser = new JSONParser();
            List<JSONObject> coinRespContainer = new ArrayList<>();
            for(String url:urlList) {
                response = getYobitResponse(url);
                String body = response.getBody();
                if (response.getStatusCodeValue() == 200) {
                    JSONObject coinInfosList = (JSONObject) parser.parse(body);
                    coinRespContainer.add(coinInfosList);
                    //System.out.print("Got response ===>" + body);
                } else {
                    System.out.print("****************No response received: " + response.getStatusCodeValue() + ".************************");
                }
            }
            System.out.println(" coinRespContainerSize --> "+coinRespContainer.size());
            System.out.println(" coinList Size --> "+coinList.size());
            System.out.println(" urlList Size --> "+urlList.size());


            i=0;j=0;
            JSONObject coinInfosList =null;
            for(String coinPair:coinList) {

                if(i==0) {
                    coinInfosList = coinRespContainer.get(j);
                    j++;
                }

                Object obj = coinInfosList.get(coinPair);
                if(obj!=null) {
                    JSONObject c = (JSONObject) obj;
                            TickerStore t = new TickerStore();
                            t.setPairCoin(coinPair);
                            if(redisRepository.findTicker("base@"+coinPair)==null){
                                t.setPairId("base@"+coinPair);
                            }else{
                                t.setPairId("current@"+coinPair);
                            }
                             //redisRepository.deleteTicker("current@"+coinPair);
                            t.setPrimaryCoin(coinPair.split("_")[0]);
                            t.setCurentVol(new BigDecimal(c.get("vol").toString()));
                            t.setLastUpdated(new Long(c.get("updated").toString()));
                            redisRepository.addTicker(t);
                }
                i++;
                if(i==50){
                    i=0;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.print("Exception"+e);
        }
        System.out.println("Total ticker info stored -->"+redisRepository.findAllTicker().size());
        System.out.println("Available ticker info keys -->"+redisRepository.findAllTicker().keySet().toString());

    }
    public  void updateCoinPairInfo() {
        System.out.print("Entering to updateCoinPairInfo call");
        ResponseEntity<String> response = null;
        try {
            String url = "https://yobit.net/api/3/info";
            response = getYobitResponse(url);
            String body =  response.getBody();
            if(response.getStatusCodeValue() == 200) {
                List<PairInfo> pList = new ArrayList<PairInfo>();
                List<String> activePairId= new ArrayList<String>();
                List<String> inActivePairId =new ArrayList<String>();
                if(body!=null)
                {
                    JSONParser parser = new JSONParser();
                    JSONObject coinInfosList = (JSONObject) parser.parse(body);
                    JSONObject coinInfos = (JSONObject)coinInfosList.get("pairs");
                    Set<String> keyset= coinInfos.keySet();
                    PairInfo pairInfo = new PairInfo();
                    for( String coinKey :keyset){
                        if(coinKey.contains("_btc")){
                            System.out.print("-->"+coinKey);
                            activePairId.add(coinKey);
                        }else{
                            inActivePairId.add(coinKey);
                        }
                    }
                    pairInfo.setId("1");
                    pairInfo.setActivePairId(activePairId);
                    pairInfo.setInActivePairId(inActivePairId);
                    redisRepository.addPairInfo(pairInfo);
                }
            } else {
                System.out.print("****************Status code received: " + response.getStatusCodeValue() + ".************************");
            }

        }catch (Exception e){
            e.printStackTrace();
            System.out.print("Exception"+e);
        }
    }

//---------------------------------------------------
    public  PairInfo getPairInfo(String id) {
        return redisRepository.findPairInfo(id);
    }
    public  TickerStore getTickerUpdate(String coin) {
        return redisRepository.findTicker(coin);
    }
    public Map<Object,Object> getAllTicker(){return  redisRepository.findAllTicker();}

    public ResponseEntity getYobitResponse(String url){
           // System.out.print("Entering to updateCoinPairInfo call");
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            SSLContext sslContext;
            ResponseEntity<String> response = null;
            try {
                sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                        .build();

                SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

                HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

                requestFactory.setHttpClient(httpClient);
                CloseableHttpClient httpClient1 = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

                HttpComponentsClientHttpRequestFactory requestFactory1 = new HttpComponentsClientHttpRequestFactory();
                requestFactory1.setHttpClient(httpClient1);

                RestTemplate restTemplate = new RestTemplate(requestFactory1);

                StringBuffer plainCreds = new StringBuffer();
                plainCreds.append("username");
                plainCreds.append(":");
                plainCreds.append("password");
                byte[] plainCredsBytes = plainCreds.toString().getBytes();
                byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
                String userBase64Credentials = new String(base64CredsBytes);

                HttpHeaders headers = new HttpHeaders();
                headers.add("user-agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
                headers.add(":authority","yobit.net");
                headers.add(":path","/api/3/info");
                headers.add(":scheme","https");
                headers.add("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" +
                        "accept-encoding:gzip, deflate, br");
                headers.add("accept-encoding","gzip, deflate, br");
                headers.add("accept-language","en-US,en;q=0.9");
                headers.add(":method","GET");
                headers.add("Sign", "47bb5a232290e8d747912f951d63b334");
                headers.add("Key", "BFEDC87760E4F75C68BE64EAFE7BCD1D");
                HttpEntity entity = new HttpEntity<>("parameters",headers);
                response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            } catch(HttpClientErrorException e) {
                if(e.getRawStatusCode() == 403) {
                    System.out.print("****************Status code received: " + e.getRawStatusCode() + ". You do not have access to the requested resource.************************");

                } else if(e.getRawStatusCode() == 404) {
                    System.out.print("****************Status code received: " + e.getRawStatusCode() + ". Resource does not exist(or) the service is not up.************************");

                } else if(e.getRawStatusCode() == 400) {
                    System.out.print("****************Status code received: " + e.getRawStatusCode() + ". Bad Request.************************");

                } else {
                    System.out.print("****************Status code received: " + e.getRawStatusCode() + ".************************");

                }
                e.printStackTrace();
                System.out.print("****************Response body: " + e.getResponseBodyAsString() + "************************");
            }catch (Exception e){
                e.printStackTrace();
                System.out.print("Exception"+e);
            }
        return response;
    }
}