package crypto.service;


import com.fasterxml.jackson.databind.util.JSONPObject;
import crypto.model.*;
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
import java.math.RoundingMode;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
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
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public  BigDecimal percFinder(BigDecimal a,BigDecimal b){

        BigDecimal c = b;
        BigDecimal d = a.subtract(b);
        BigDecimal e= d.divide(c,2, RoundingMode.HALF_UP);
        BigDecimal f = e.multiply(ONE_HUNDRED);
        return f;
    }
    public static BigDecimal getPercenVal(BigDecimal a,int perc){

        double aa = a.doubleValue();
        BigDecimal f = a.multiply(new BigDecimal(perc));
        BigDecimal e= f.divide(ONE_HUNDRED,10, RoundingMode.HALF_UP);
        return e;
    }
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

    //@Scheduled(fixedDelay = 60000)
    public void runAnalizerBotOnYobit() {
        System.out.println("Hey i am going to run . Current time is --> "+System.currentTimeMillis());
        System.out.println("***** Got ticker update . Now running algorithm ");

        PairInfo pp = getPairInfo("1");
        List<String> coinPairList = getTickerUpdate();//pp.getActivePairId();
        List<String> coinNotificationList = new ArrayList<String>();
        TickerStore baseTicker = null;
        TickerStore currentTicker = null;

        List<NotifiedCoin> ponziList = new ArrayList<>();
        boolean sendEmail = false;
        boolean systemRest = false;
        String text = "";
        text =text+"\n Hey @ Below coin is having cash pump currently , Make sure you invest as quickly as possible \n";
        text = text + "----------------------------------------------------------------------\n";
        int l =0;
       for(String coinPair:coinPairList){
           if(l>40){
               break;
           }
            baseTicker = redisRepository.findTicker("base@"+coinPair);
            currentTicker = redisRepository.findTicker("current@"+coinPair);
            if(baseTicker != null && currentTicker!=null){
                BigDecimal diffVol = currentTicker.getCurentVol().subtract(baseTicker.getCurentVol());
                BigDecimal diffTrx = currentTicker.getRecentTrxRate().subtract(baseTicker.getRecentTrxRate());

                if(currentTicker.getCurentVol().compareTo(baseTicker.getCurentVol())>=0
                        && currentTicker.getRecentTrxRate().compareTo(new BigDecimal("0"))>0
                        //&& currentTicker.getRecentTrxRate().compareTo(currentTicker.getHigh24H())>=0
                        && percFinder(currentTicker.getRecentTrxRate(),currentTicker.getLow24H()).compareTo(new BigDecimal("50"))<=0
                        && redisRepository.findNotifiedCoin(coinPair)==null
                        ){
                    l++;
                    NotifiedCoin nc = new NotifiedCoin();
                    nc.setPairId(coinPair);
                    nc.setDiffVol(diffVol);
                    nc.setDiffTrx(diffTrx);
                    nc.setPercentChange(percFinder(currentTicker.getRecentTrxRate(),currentTicker.getLow24H()));
                    nc.setOldVol(baseTicker.getCurentVol());
                    nc.setNewVol(currentTicker.getCurentVol());
                    NotifiedCoin ncFromRepo = redisRepository.findNotifiedCoin(nc.getPairId());
                    if(ncFromRepo!=null)
                    {
                        nc.setBasePrice(ncFromRepo.getBasePrice());
                        nc.setBaseUpdated(ncFromRepo.getBaseUpdated());
                    }else{
                        nc.setBasePrice(currentTicker.getLow24H());
                        nc.setBaseUpdated(baseTicker.getLastUpdated());
                    }
                    nc.setCurrentPrice(currentTicker.getRecentTrxRate());
                    nc.setLastUpdated(currentTicker.getLastUpdated());
                    ponziList.add(nc);
                }

                //RESET COIN BASE PRICE EVERY 24 HOUR
                long val = currentTicker.getLastUpdated();
                long val1 = baseTicker.getLastUpdated();
                Date date=new Date(val*1000);
                Date date1=new Date(val1*1000);
                int diffInDays = (int)( (date.getTime() - date1.getTime())
                        / (1000 * 60 * 60 * 24) );
                if(diffInDays>0) {
                    systemRest = true;
                    currentTicker.setPairId("base@" + coinPair);
                    redisRepository.addTicker(currentTicker);
                    redisRepository.deleteNotifiedCoin(coinPair);
                }
                //=========================================================
            }
        }

        if(!ponziList.isEmpty()) {

            assginRanking(ponziList);
            //Sorting
            ponziList.sort((h1, h2) -> h1.getRank()- h2.getRank());
            int i = ponziList.size()-1;
            int j=1;
            while (i>=0) {

                NotifiedCoin nCoin = ponziList.get(i);
                if(nCoin.getRank()>=20) {
                    sendEmail = true;
                    System.out.println("Hey man , I found out a spike on this coin ->" + nCoin.getPairId().replace("current@", ""));
                    text = text + "" + nCoin.getPairId().toUpperCase() + " has increased by " + nCoin.getPercentChange() +"%" ;
                    text = text + "\n";
                    text = text + "Rank ->" + j;
                    text = text + "\n";
                    text = text + "Total point given by Bot ->" + nCoin.getRank()+" out of 65";
                    text = text + "\n";
                    if (nCoin.getRank() >= 50) {
                        String botText = nCoin.isBotInvolved()?" PUMP BY BOT !!!":"";
                        text = text + "** THIS MIGHT GO 100% , BUY THIS AS QUICKLY AS POSSIBLE .. GOING CRAZY. "+botText;
                        text = text + "\n";
                    } else if (nCoin.getRank() >= 30 ) {
                        text = text + "** 20% FOR SURE ,BUY THIS AS QUICKLY AS POSSIBLE .. GOING CRAZY";
                        text = text + "\n";
                    } else {/*if (nCoin.getRank() >= 70) {*/
                        text = text + "** 10% FOR SURE ,BUY THIS AS QUICKLY AS POSSIBLE .";
                        text = text + "\n";
                    }
                    System.out.println("Old Value ->" + nCoin.getOldVol());
                    System.out.println("Current Value ->" + nCoin.getNewVol());
                    //text = text + "Old Volume ->" + nCoin.getOldVol();
                    //text = text + "\n";
                    // text = text + "Current Volume ->" + nCoin.getNewVol();
                    //text = text + "\n";
                    text = text + "BASE PRICE ->" + nCoin.getBasePrice();
                    text = text + "\n";
                    text = text + "BASE PRICE UPDATED ->" + new Date(nCoin.getBaseUpdated() * 1000);
                    text = text + "\n";
                    text = text + "CURRENT PRICE ->" + nCoin.getCurrentPrice();
                    text = text + "\n";
                    text = text + "CURRENT PRICE UPDATED ->" + new Date(nCoin.getLastUpdated() * 1000);
                    text = text + "\n";
                    text = text + "Total % Increased ->" + nCoin.getPercentChange();
                    text = text + "\n";
                    text = text + "----------------------------------------------------------------------\n";
                    j++;
                    redisRepository.addNotification(nCoin);
                }
                i--;
            }
            //text = text+" Complete List -->" +ponziList.toString()+ "\n";
            if (sendEmail) {
                System.out.println("Sending email with findings");
               emailService.sendMail("bibin.kt@gmail.com", "Yobit Exchange PONZI COIN updates", text.replace("@", "Bibin"));
               emailService.sendMail("haiprabeesh@gmail.com", "Yobit Exchange PONZI COIN updates", text.replace("@", "Prabeesh"));
                /*if(systemRest){
                    emailService.sendMail("bibin.kt@gmail.com", "CRYPTO BOT DATA RESET ", "Hello, i am resetting last 24H data");
                    emailService.sendMail("haiprabeesh@gmail.com", "CRYPTO BOT DATA RESET ", "Hello, i am resetting last 24H data");

                }*/
            }else {
                System.out.println("\nNo findings to sent !!!");
            }
        }else {
            System.out.println("\nNo findings to sent !!!");
        }
    }
    //public void assginRanking(List<NotifiedCoin> ponziCoin){

   // }
   // @Scheduled(fixedRate = 60000*5)
    public void assginRanking(List<NotifiedCoin> ponziCoin){

        ResponseEntity<String> response = null;
        JSONParser parser = new JSONParser();
        TradeInfoStore tStore = new TradeInfoStore();
        List<TradeInfo> infoList= new ArrayList<>();
        Map tradeInfoMap  = new HashMap<String,List<TradeInfo>>();
        String pairString = "";
        for(int i=0;i<ponziCoin.size();i++) {
            NotifiedCoin pair=ponziCoin.get(i);
            pairString = pairString.equalsIgnoreCase("")?pair.getPairId():pairString + "-" + pair.getPairId();
        }
        String url = "https://yobit.net/api/3/trades/"+pairString+"?limit=25";
        try {
            //System.out.print("ticker Url -->" + url);
            response = getYobitResponse(url);
            String body = response.getBody();
            if (response.getStatusCodeValue() == 200) {
                JSONObject tradeInfoList = (JSONObject) parser.parse(body);
                //System.out.print("Got response ===>" + tradeInfoList.toString());
                tStore.setId("1");
                for(NotifiedCoin nc:ponziCoin){
                    List<JSONObject> tradeInfo = (List<JSONObject>)tradeInfoList.get(nc.getPairId());
                   // System.out.print("Got response1 ===>" + tradeInfo.toString());
                    int rank=0;
                    int k=0;
                    int minuteTcount = 0;
                    BigDecimal temp = new BigDecimal("0");
                    for(JSONObject tradeInfoVal:tradeInfo){

                        Long lastupdate = new Long(tradeInfoVal.get("timestamp").toString());
                        long val1 = System.currentTimeMillis();;
                        Date date=new Date(lastupdate*1000);
                        Date sysDate=new Date(val1);

                        long diff = sysDate.getTime() - date.getTime();
                        long diffMinutes = diff / (60 * 1000) % 60;
                        long diffHours = diff / (60 * 60 * 1000) % 24;
                        long diffDays = diff / (24 * 60 * 60 * 1000);
                        if(diffDays==0 && diffHours==0) {

                            TradeInfo tInfo = new TradeInfo();
                            tInfo.setType(tradeInfoVal.get("type").toString());
                            tInfo.setPrice(new BigDecimal(tradeInfoVal.get("price").toString()));
                            tInfo.setAmount(new BigDecimal(tradeInfoVal.get("amount").toString()));
                            tInfo.setLastUpdated(lastupdate);


                            if (tInfo.getType().equalsIgnoreCase("bid")) {
                                if (diffMinutes <= 10) {
                                    minuteTcount++;
                                }
                                rank = rank + 1;

                                if (temp.compareTo(tInfo.getAmount()) != 0) {
                                    temp = tInfo.getAmount();
                                } else {
                                    k++;
                                }
                            } else {
                                rank = rank - 1;
                            }
                            infoList.add(tInfo);
                        }
                    }
                    nc.setTradeRank(rank);
                    nc.setRank(nc.getRank()+rank);
                    if(k>=20){
                        nc.setRank(nc.getRank()+20);
                        nc.setBotInvolved(true);
                    }/*else if(k>=30){
                        nc.setRank(nc.getRank()+30);
                        nc.setBotInvolved(true);
                    }else if(k>=15){
                        nc.setRank(nc.getRank()+20);
                        nc.setBotInvolved(true);
                    }*/

                    if(minuteTcount>=20){
                        nc.setRank(nc.getRank()+20);
                    }/*else if(minuteTcount>=30){
                        nc.setRank(nc.getRank()+30);
                    }else if(minuteTcount>=15){
                        nc.setRank(nc.getRank()+20);
                    }*/
                   // System.out.print("Final NotifiedCoin rank ===>" + nc.getRank());
                    //System.out.print("Final NotifiedCoin ===>" + nc.toString());
                    nc.setTradeInfos(infoList);
                    tradeInfoMap.put(nc.getPairId(),infoList);
                }
                tStore.setTradeInfo(tradeInfoMap);
                System.out.print(" tStore ===>" + tStore.toString());
                redisRepository.addTradeInfo(tStore);
            } else {
                System.out.print("****************No response received: " + response.getStatusCodeValue() + ".************************");
            }

        }catch (Exception e){
            System.out.print("****************No response received: " + response.getStatusCodeValue() + ".************************");
        }

    }

    public void cleanTicker(){
        redisRepository.truncate("ticker");
    }
    public Map<Object, NotifiedCoin> getAllNotification(){
        return redisRepository.findAllNotification();
    }

    public void cleanNotification(){
        redisRepository.truncate("notification");
    }
    public String getPairInfoDetail(String id){
        PairInfo pp = getPairInfo("1");
        List<String> coinPairList = pp.getActivePairId();
        for(String pair:coinPairList) {

            if(id.equalsIgnoreCase(pair)) {
                return "Found";
            }
        }
        return null;
    }

    public  TickerStore getTickerUpdateByCoin(String coinPair) {

        String url = "https://yobit.net/api/3/ticker/"+coinPair;
        ResponseEntity<String> response = null;
        JSONParser parser = new JSONParser();
        try {
            //System.out.print("ticker Url -->" + url);
            response = getYobitResponse(url);
            String body = response.getBody();
            if (response.getStatusCodeValue() == 200) {

                JSONObject coinInfosList = (JSONObject) parser.parse(body);
                Object obj = coinInfosList.get(coinPair);
                if(obj!=null) {
                    System.out.print("***Got response for --> ***" +coinPair);
                    JSONObject c = (JSONObject) obj;
                    TickerStore t = new TickerStore();
                    t.setPairCoin(coinPair);
                    t.setPrimaryCoin(coinPair.split("_")[0]);
                    t.setCurentVol(new BigDecimal(c.get("vol").toString()));
                    t.setRecentTrxRate(new BigDecimal(c.get("last").toString()));
                    t.setHigh24H(new BigDecimal(c.get("high").toString()));
                    t.setLow24H(new BigDecimal(c.get("low").toString()));
                    t.setLastUpdated(new Long(c.get("updated").toString()));

                    return t;
                }
            } else {
                System.out.print("****************No response received: " + response.getStatusCodeValue() + ".************************");
            }
        }catch (Exception e){
            System.out.print("****************No response received:");
        }
        return null;
    }
        //@Scheduled(fixedDelay = 30000)
    public  List<String> getTickerUpdate() {

        System.out.print("Getting the ticker update");
        ResponseEntity<String> response = null;
        PairInfo pp = getPairInfo("1");
        System.out.print("Payer info from cache -->"+pp.getActivePairId().size());
        String pairString ="";
        List<String> urlList = new ArrayList();
        List<String> coinList = new ArrayList();
        List<String> coinList1 = new ArrayList();

        try {
            List<String> coinPairList = pp.getActivePairId();
            System.out.println("coinPairList.size()-->"+coinPairList.size());
            System.out.println("pp.getReadEntry()-->"+pp.getReadEntry());

            int i=pp.getReadEntry();
            int j=0;
            if(pp.getReadEntry()>=coinPairList.size()){
                i=0;
            }
            System.out.println("i starts at-->"+i);
            int originalIvalue =i;
            for(;i<coinPairList.size();i++) {
                String pair=coinPairList.get(i);
                coinList1.add(pair);
                pairString = pairString.equalsIgnoreCase("")?pair:pairString + "-" + pair;
                if(((i+1)-50)==originalIvalue || (i+1)==coinPairList.size()) {
                    originalIvalue =i+1;
                    j=0;
                    String url = "https://yobit.net/api/3/ticker/"+pairString;
                    urlList.add(url);
                    pairString ="";
                    coinList.addAll(coinList1);
                    coinList1 = new ArrayList<>();
                    if(urlList.size()==10)break;
                }
            }
            pp.setReadEntry(i);
            System.out.println("Updating pp.getReadEntry() to-->"+pp.getReadEntry());
            System.out.println(" coinList Size --> "+coinList.size());
            System.out.println(" urlList Size --> "+urlList.size());
            redisRepository.addPairInfo(pp);


            JSONParser parser = new JSONParser();
            List<JSONObject> coinRespContainer = new ArrayList<>();
            for(String url:urlList) {
                try {
                    //System.out.print("ticker Url -->" + url);
                    response = getYobitResponse(url);
                    String body = response.getBody();
                    if (response.getStatusCodeValue() == 200) {
                        JSONObject coinInfosList = (JSONObject) parser.parse(body);
                        coinRespContainer.add(coinInfosList);
                        //System.out.print("Got response ===>" + body);
                    } else {
                        System.out.print("****************No response received: " + response.getStatusCodeValue() + ".************************");
                    }
                }catch (Exception e){

                }
            }
            System.out.println(" coinRespContainerSize --> "+coinRespContainer.size());



            i=0;j=0;
            JSONObject coinInfosList =null;
            for(String coinPair:coinList) {

                if(i==0) {
                    if(j>=coinRespContainer.size()){
                        break;
                    }
                    coinInfosList = coinRespContainer.get(j);
                    j++;
                }

                Object obj = coinInfosList.get(coinPair);
                if(obj!=null) {
                    JSONObject c = (JSONObject) obj;
                            TickerStore t = new TickerStore();
                            t.setPairCoin(coinPair);
                            TickerStore tFromRepo = redisRepository.findTicker("current@"+coinPair);
                            if(redisRepository.findTicker("base@"+coinPair)==null){
                                t.setPairId("base@"+coinPair);
                            }else{
                                t.setPairId("current@"+coinPair);
                            }
                            if(tFromRepo!=null){
                                t.setCount(tFromRepo.getCount()+1);
                            }else{
                                t.setCount(1);
                            }
                             //redisRepository.deleteTicker("current@"+coinPair);
                            t.setPrimaryCoin(coinPair.split("_")[0]);
                            t.setCurentVol(new BigDecimal(c.get("vol").toString()));
                            t.setRecentTrxRate(new BigDecimal(c.get("last").toString()));
                            t.setHigh24H(new BigDecimal(c.get("high").toString()));
                            t.setLow24H(new BigDecimal(c.get("low").toString()));
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
        return  coinList;
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
                    pairInfo.setId("1")
                    ;
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

                HttpHeaders headers = new HttpHeaders();
                headers.add("user-agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
                headers.add(":authority","yobit.net");
                headers.add(":path","/api/3/info");
                headers.add(":scheme","https");
                headers.add("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8" +
                       "accept-encoding:gzip, deflate, br");
                headers.add("accept-encoding","gzip, deflate, br");
                headers.add("accept-language","en-US,en;q=0.9");
                headers.add("accept-version","5");
                headers.add("upgrade-insecure-requests","1");
                headers.add(":method","GET");
                headers.add("cookie","__cfduid=d22d72d8fa9cfc430f4029bf1d40a19081515468258; locale=en; _ym_uid=1515468259980539455; Rfr=https%3A%2F%2Fyobit.net%2Fen%2F; wallet_check_hide_zero=0; marketbase=btc; locale_chat=en; PHPSESSID=akru72qukvp8rcaq15dt5ndkv1; 7deb57718f722b7472dd8e7795800504=1; s1=UamibG%2BxnBoUoX9ttp1adtzApiBMRnHzWCpc9viagEHanXX8dXuZdEpacAypHTzQe9HUzD8DAolgtbjcSoLuBGVNOE2IYkf%2B%2BTv4WyF1DPlRc1i10nzCa75UZnzWbk4MA8tqTrTShfJ5pe2jWx41DjpLVZdbyVekw5h31OVwF3D0QnFsr8hH3LGzAl5DYwyUb%2Bya5O0gEXJOWC8jdwLfEat4vpipK7fmZng60M8eMFGRjTvSZEsDp6zfFG46fdmIF7cbe5%2Fc%2Fjiyvmghw5KaPyVccEewChowf98X0x%2BRmShSniQf2o5cZe6BOVKg9BSNCoVr%2BQu0V2TbR3g39c9X4ILt5EUzQOWO%2FvzWnaOiZboIJ3nWfgzHUjp3S7T2U2GZ8oDIuPR8JuAR92ftAZlhHVk0TMVdjjWApOZi1DYo%2FTg78X5VkEmraKendyfRDV0K7OLBZxUUOmAB7ZFeBK5Z58Yv%2BRk1FRP2fn6%2BSZY1hHKGyTNUtH8rPGcL%2BOTDYYgDZuZQ4caltCMgL8cxquivsZZCDiyFF4MxGK5r0YqDDHFRb34ZpH%2BYdILsn5ZmjcjE4iE7lF2gtj4tyzBWb08vjBBz47SK6p8ZxILQAE9TanJWuZU7b6DVHo0dD2Z03gwizzLQBlqTST9A05xcWKb1UDkBefuuoz8%2Bd5se51Benc8%3D; s2=GYILE5a0jJHQ1bZmTiySBOUIBqBmF00t%2BU9fCsXs%2BuX6dmwhva2vQbtJuHFqZQUEXMio5vfknkjSlNUkdVwlaNDhMFhMABIkATcWJZZQet6PBuDz%2BYSsdaJQp1ko9wE1ZLn0cgj1GdpVQgaJsSKmbR54dGz8t8rG0XGuqqmn4gYymM8ASnu%2BA%2BR5647wXEk%2BO4rEWbQV9Ve2FYxksjcsngvq7Qf2zligJtjdtPFa5MBn9rMUMnhiF4HTxNGh0QntRosrpu4kUmfW%2FvbEtnr%2BrOjuGBjdtAj%2FRuG4P5tnkPd5i%2Fw2ahORv3MF0Ym69yUGMz6e6SA8IjvOWxLo%2BX7Fu5cklb58Szxt56NITC35lBQJ6qLNDis1zmCsQgUol%2BVjz8csyJ4dKkssO4ikVUJIAwnWIhbdktMsks9U8brrUnDid31XL3hrkYcVYt5Lxwnuctiu5fiLtc3gpgUAstaKvfEN6zpyFkcb3I3TRRVnWmGb4dJ%2BkJs7gej%2F%2B3z0QvIDnGix6NS36i8%2BnweWxgr%2BqbS30io5Y7j9YqOBm%2BDo%2B0sL53VR9szkq9CQIJUWvQia5qhmRwMzPLvZ0LAmdsOl2Zw1hPL1KcmlFb6ed9PMe0wsQ0bVc9Mk2axBSLB48%2F5t9CXuUaXWuqoFFcZp%2FtaeXbXToFueAiTtMVfn%2FBKCC4g%3D; _ym_isad=2; cf_clearance=c85381b5a5245e05c26e799213242a93c0300e24-1518846699-86400; chartmode=7D");
                headers.add("Sign", "d378b611697255b5d73e69a89fd463d4");
                headers.add("Key", "ED777FB7067EFAFD4FB9DCD03F2C442E");
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