package crypto;

import com.fasterxml.jackson.databind.JsonNode;
import crypto.model.PairInfo;
import crypto.repository.CoinPairRepo;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

@SpringBootApplication
public class SosApplication {

	@Autowired
	CoinPairRepo coinPairRepo;
	/*static
	{
		System.setProperty("javax.net.ssl.trustStore","c:/apachekeys/client1.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "password");
		System.setProperty("javax.net.ssl.keyStore", "c:/apachekeys/client1.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
	}*/


	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack","true");
		SpringApplication.run(SosApplication.class, args);
		SosApplication ss = new SosApplication();
		ss.updateCoinPairInfo();
		System.setProperty("javax.net.ssl.trustStore", "C:\\keystor.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
		//getUrl();
		/*CloseableHttpClient httpClient
				= HttpClients.custom()
				.setSSLHostnameVerifier(new NoopHostnameVerifier())
				.build();
		HttpComponentsClientHttpRequestFactory requestFactory
				= new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		ResponseEntity<String> response
				= new RestTemplate(requestFactory).exchange(
				"https://yobit.net/api/2/ltc_btc/ticker", HttpMethod.GET, null, String.class);*/

	}

	public  void updateCoinPairInfo() {
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

			String url = "https://yobit.net/api/3/info";
			response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			String body =  response.getBody();
			if(response.getStatusCodeValue() == 200) {
				List<PairInfo> pList = new ArrayList<PairInfo>();
				List<String> activePairId= new ArrayList<String>();
				List<String> inActivePairId =new ArrayList<String>();
				//System.out.print("Success! Further processing based on the need"+body);
				if(body!=null)
				{
					JSONParser parser = new JSONParser();
					JSONObject coinInfosList = (JSONObject) parser.parse(body);
					JSONObject coinInfos = (JSONObject)coinInfosList.get("pairs");
					Set<String>keyset= coinInfos.keySet();
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
					coinPairRepo.save(pairInfo);
				}
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
