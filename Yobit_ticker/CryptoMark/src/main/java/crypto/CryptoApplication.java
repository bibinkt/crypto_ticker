package crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoApplication {

	/*static
	{
		System.setProperty("javax.net.ssl.trustStore","c:/apachekeys/client1.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "password");
		System.setProperty("javax.net.ssl.keyStore", "c:/apachekeys/client1.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
	}*/


	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack","true");
		SpringApplication.run(CryptoApplication.class, args);
		CryptoApplication ss = new CryptoApplication();
		//ss.updateCoinPairInfo();
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


}
