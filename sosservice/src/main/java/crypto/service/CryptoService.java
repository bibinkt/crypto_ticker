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
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.math.BigDecimal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/**
 * Created by bthiru on 1/9/2018.
 */
@Component
public class CryptoService {

    @Autowired
    RedisRepositoryImpl redisRepository;

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
    /*public List<TickerStore> getTickerUpdate() {
        List<CryptoUpdate> c = new ArrayList<CryptoUpdate>();
        List<MediaType> accept = new ArrayList<>();
        accept.add(MediaType.APPLICATION_JSON_UTF8);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(accept);
        HttpEntity<CryptoUpdate[]> requestEntity = new HttpEntity<CryptoUpdate[]>(headers);
        ResponseEntity<CryptoUpdate[]> response = restTemplate.exchange("https://api.coinmarketcap.com/v1/ticker/?limit=1000", HttpMethod.GET, requestEntity, CryptoUpdate[].class);

        return Arrays.asList(response.getBody());
    }*/
    public  void getTickerUpdate() {
        System.out.print("Entering to updateCoinPairInfo call");
        ResponseEntity<String> response = null;
        PairInfo pp = getPairInfo("1");
        System.out.print("Payer info from cache -->"+pp.getActivePairId().size());
        String pairString ="";
        List<String> urlList = new ArrayList();
        List<String> coinList = new ArrayList();

        try {
            List<String> coinPairList = pp.getActivePairId();
            int i=0;
            int j=0;
            for(String pair:coinPairList) {
                coinList.add(pair);
                if(j==0) {
                    pairString = pair;
                }else{
                    pairString = pairString + "-" + pair;
                }

                if(j==50) {
                    j=-1;
                    String url = "https://yobit.net/api/3/ticker/"+pairString+"-error_pair?ignore_invalid=1";
                    System.out.print("ticker Url -->"+url);
                    urlList.add(url);
                }
                i++;
                j++;
                if(i==501)break;
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
            System.out.println("Size --> "+coinRespContainer.size());
            i=0;j=0;
            JSONObject coinInfosList =null;
            for(String coinPair:coinList) {

                if(i==0) {
                    coinInfosList = coinRespContainer.get(j);
                    j++;
                }
                TickerStore t = new TickerStore();
                JSONObject c = (JSONObject)coinInfosList.get(coinPair);
                t.setPairCoin(coinPair);
                t.setPairId(coinPair);
                t.setPrimaryCoin(coinPair.split("_")[0]);
                t.setCurentVol(new BigDecimal(c.get("vol_cur").toString()));
                t.setLastUpdated(new Long(c.get("vol_cur").toString()));
                redisRepository.addTicker(t);
                i++;
                if(i==50){
                    i=0;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.print("Exception"+e);
        }
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

    public ResponseEntity getYobitResponse(String url){
            System.out.print("Entering to updateCoinPairInfo call");
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