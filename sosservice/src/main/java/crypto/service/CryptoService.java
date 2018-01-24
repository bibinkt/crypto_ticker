package crypto.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import crypto.model.PairInfo;
import crypto.repository.CoinPairRepo;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by bthiru on 1/9/2018.
 */
@Component
public class CryptoService {

    @Autowired
    CoinPairRepo coinPairRepo;

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


    public void updateCoinPairInfo() {
        System.out.print("Entering to updateCoinPairInfo call");
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext;
        ResponseEntity<JsonNode> response = null;
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

            String url = "https://yobit.net/api/3/info";
            response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            JsonNode body =  response.getBody();
            if(response.getStatusCodeValue() == 200) {
                System.out.print("Success! Further processing based on the need"+body);
            } else {
                System.out.print("****************Status code received: " + response.getStatusCodeValue() + ".************************");
            }

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
    }
}