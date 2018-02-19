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
import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by bthiru on 1/9/2018.
 */
@Component
public class CryptoServiceV1 extends CryptoService {

    @Autowired
    RedisRepositoryImpl redisRepository;

    @Autowired
    EmailService emailService;

    CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .build();
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    private RestTemplate restTemplate = null;

    @Scheduled(fixedDelay = 30000)
    public void analizerBotOnYobit() {
        System.out.println("Running the Analizer bot on yobit --> "+System.currentTimeMillis());
        List<NotifiedCoin> ponziList = findCashPump();
        boolean sendEmail = false;
        String text = "";
        text =text+"\n Hey @ Below coin is having cash pump currently , Make sure you invest as quickly as possible \n";
        text = text + "----------------------------------------------------------------------\n";
        int l =0;

        if(!ponziList.isEmpty()) {

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
                }
                i--;
            }
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

    @Scheduled(fixedDelay = 2*3600000)
    public void sendNotifiedCoinAnalysis() {
        System.out.println("Running the Analizer bot on yobit --> " + System.currentTimeMillis());

        updateNotifiedCoinHistory();

        Map<Object, NotifiedCoinHistory> ponziList = redisRepository.findAllNotificationH();
        Set<Object> keys = ponziList.keySet();

        System.out.println("Current NotifiedCoinHistory list -->" + ((keys==null||keys.isEmpty())?0:keys.size()));
        boolean sendEmail = false;
        String text = "";
        text = text + "<html><body> \n Hi @ \n <p> Here is my current status of the coins on which i gave you the alert . " +
                "This will help me to achieve even more accuracy level and to automate the " +
                "coin trading</p> \n <Table style=\"width:100%\">";
        text = text + "<tr bgcolor=\"#A9A9A9\">" +
                "    <th>ID</th>" +
                "    <th>BASE VOLUME[time of alert]</th>" +
                "    <th>BASE PRICE[time of alert]</th>" +
                "    <th>CURRENT PRICE[time of alert]</th>" +
                "    <th>DATE/TIME[time of alert]</th>" +
                "    <th>GIVEN RANK</th>" +
                "    <th>BOT PRESENCE</th>" +
                "    <th>PERCENTAGE[time of alert]</th>" +
                "    <th>MAX PRICE ACHIEVED[After alert]</th>" +
                "    <th>MAX PERCENTAGE ACHIEVED[After alert]</th>" +
                "    <th>CURRENT PRICE</th>" +
                        "    <th>CURRENT VOLUME</th>" +
                "    <th>DATE[updated date]</th>" +
                "  </tr>";
        if (keys!=null && !keys.isEmpty()) {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
            String todaysKey = dt.format(new Date(System.currentTimeMillis()));
            System.out.println("Notification history total size -->"+ponziList.size());
            System.out.println("Todays key -->"+todaysKey);
            for (Object obj : keys) {
                String key = (String) obj;
                NotifiedCoinHistory nCoin = ponziList.get(key);
                if(nCoin.isError())continue;
                String array[] = key.split("-");
                if(!todaysKey.equalsIgnoreCase(array[1]+"-"+array[2]+"-"+array[3])) {
                    continue;
                }
                sendEmail = true;
                String color = "";
                BigDecimal diffPerc = nCoin.getMaxPercentageChangeLast24h().subtract(nCoin.getPercentageOnAlert());
                if(diffPerc.compareTo(new BigDecimal("10"))>=0){
                    color = "bgcolor=\"#7CFC00\"";
                }else if(diffPerc.compareTo(new BigDecimal("2"))>=0){
                    color = "bgcolor=\"#F4A460\"";
                }else if(diffPerc.compareTo(new BigDecimal("0"))<=0){
                    color = "bgcolor=\"\t#FF0000\"";
                }

                text = text + "<tr "+color+">" +
                        "    <td>" + nCoin.getPairId() + "</td>" +
                        "    <td>" + nCoin.getBaseVol() + "</td>" +
                        "    <td>" + nCoin.getBasePrice() + "</td>" +
                        "    <td>" + nCoin.getCurrentEprice() + "</td>" +
                        "    <td>" + new Date(nCoin.getBaseUpdated()*1000) + "</td>" +
                        "    <td>" + nCoin.getRank() + "</td>" +
                        "    <td>" + nCoin.isBotInvolved() + "</td>" +
                        "    <td>" + nCoin.getPercentageOnAlert() + "</td>" +
                        "    <td>" + nCoin.getMaxPriceLast24h() + "</td>" +
                        "    <td>" + nCoin.getMaxPercentageChangeLast24h() + "</td>" +
                        "    <td>" + nCoin.getCurrentPrice() + "</td>" +
                        "    <td>" + nCoin.getCurrentVol() + "</td>" +
                        "    <td>" + new Date(nCoin.getLastUpdated()==null?System.currentTimeMillis():nCoin.getLastUpdated()*1000) + "</td>" +
                        "  </tr>";
            }

            text = text + "</Table></body></html>";

            if (sendEmail) {

                System.out.println("Sending email with History analysis");
                emailService.sendMailH("bibin.kt@gmail.com", "PONZI COIN Alert analysis Report", text.replace("@", "Bibin"));
                emailService.sendMailH("haiprabeesh@gmail.com", "PONZI COIN Alert analysis Report", text.replace("@", "Prabeesh"));

            } else {
                System.out.println("\nNo Report to sent !!!");
            }
        }else {
            System.out.println("\nNo Report to sent || coin history is empty !!!!!");
        }

    }


    public void updateNotifiedCoinHistory(){

        System.out.println("**Updating the Notification history ***");
        //redisRepository.truncate("notification_h");
        Map<Object,NotifiedCoin> ponziList = redisRepository.findAllNotification();
        System.out.println("Current NotifiedCoin list Size -->"+redisRepository.findAllNotification().size());
        Set<Object> keys = ponziList.keySet();

        Collection ponCollection = ponziList.values();
        List ponList = Arrays.asList(ponCollection);
        System.out.println("Current NotifiedCoin list -->"+((ponList==null|| ponList.isEmpty())?0:ponList.size()));

        NotifiedCoinHistory nH = null;

        if(keys!=null) {
            if(keys.isEmpty())
                System.out.println("**No  Notification Coin found ***");

            for (Object ob : keys) {
                String key = (String)ob;
                NotifiedCoin nC = ponziList.get(key);
                nH = new NotifiedCoinHistory();
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
                System.out.println("setting id as  -->  -->" + nC.getPairId() + "-" + dt.format(new Date(System.currentTimeMillis())));
                nH.setPairId(nC.getPairId() + "-" + dt.format(new Date(System.currentTimeMillis())));
                nH.setDate(new Date(System.currentTimeMillis()));

                TickerStore baseT = redisRepository.findTicker("base@" + nC.getPairId());
                if (baseT != null) {
                    nH.setBaseVol(baseT.getCurentVol());
                    nH.setBasePrice(baseT.getLow24H());
                    nH.setCurrentEprice(nC.getCurrentPrice());
                    nH.setBaseUpdated(nC.getLastUpdated());
                }
                nH.setRank(nC.getRank());
                nH.setBotInvolved(nC.isBotInvolved());
                nH.setPercentageOnAlert(nC.getPercentChange());
                nH.setTradeInfoOnalert(nC.getTradeInfos());

                System.out.println("\nTriggering live ticker call !!!");
                TickerStore curentT = getTickerUpdateByCoin(nC.getPairId());
                if (curentT != null) {
                    System.out.println("\nlive ticker call Success!!!");
                    BigDecimal maxPrice24h = curentT.getHigh24H();
                    nH.setMaxPriceLast24h(maxPrice24h);
                    nH.setMaxPercentageChangeLast24h(percFinder(maxPrice24h, baseT.getLow24H()));
                    nH.setCurrentPrice(curentT.getRecentTrxRate());
                    nH.setCurrentPercentageChangeLast24h(percFinder(curentT.getRecentTrxRate(), baseT.getLow24H()));
                    nH.setCurrentVol(curentT.getCurentVol());
                    nH.setLastUpdated(curentT.getLastUpdated());
                }else{
                    nH.setError(true);
                }
                redisRepository.addNotificationH(nH);
            }
        }else {
            System.out.println("**No  Notification Coin found ***");
        }

    }

    public   List<NotifiedCoin> findCashPump() {

        System.out.print("Info : run findCashPump");
        ResponseEntity<String> response = null;
        PairInfo pp = getPairInfo("1");
        System.out.print("Payer info from cache -->"+pp.getActivePairId().size());
        String pairString ="";
        List<String> urlList = new ArrayList();
        List<String> coinList = new ArrayList();
        List<String> coinList1 = new ArrayList();
        List<TradeInfo> infoList= null;
        List<String> exclusionList= new ArrayList<>();
       // exclusionList.add("trx_btc");
       // exclusionList.add("xrp_btc");

        List<NotifiedCoin> ponziCoinList = new ArrayList<>();
        Map tradeInfoMap  = new HashMap<String,List<TradeInfo>>();
        int coinCount=0;

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
                    String url = "https://yobit.net/api/3/trades/"+pairString+"?limit=25";
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

                List<JSONObject> c = (List<JSONObject>)coinInfosList.get(coinPair);
                if(c!=null) {
                   // System.out.println("c Value --> -- >"+c.toString());
                    coinCount++;
                    int rank=0;
                    int k=0;
                    int minuteTcount = 0;
                    BigDecimal temp = new BigDecimal("0");
                    TickerStore   baseTicker = redisRepository.findTicker("base@"+coinPair);
                    NotifiedCoin nc = new NotifiedCoin();
                    infoList = new ArrayList<>();
                    nc.setPairId(coinPair);
                    if(baseTicker!=null) {
                        nc.setBasePrice(baseTicker.getLow24H());
                        nc.setBaseUpdated(baseTicker.getLastUpdated());
                    }
                    BigDecimal lastPrice = null;
                    if(redisRepository.findNotifiedCoin(coinPair)==null
                            ) {
                        for (JSONObject tradeInfoVal : c) {

                            Long lastupdate = new Long(tradeInfoVal.get("timestamp").toString());
                            long val1 = System.currentTimeMillis();
                            ;
                            Date date = new Date(lastupdate * 1000);
                            Date sysDate = new Date(val1);

                            long diff = sysDate.getTime() - date.getTime();
                            long diffMinutes = diff / (60 * 1000) % 60;
                            long diffHours = diff / (60 * 60 * 1000) % 24;
                            long diffDays = diff / (24 * 60 * 60 * 1000);
                            if (diffDays == 0 && diffHours == 0) {

                                TradeInfo tInfo = new TradeInfo();
                                tInfo.setType(tradeInfoVal.get("type").toString());
                                tInfo.setPrice(new BigDecimal(tradeInfoVal.get("price").toString()));
                                tInfo.setAmount(new BigDecimal(tradeInfoVal.get("amount").toString()));
                                tInfo.setLastUpdated(lastupdate);
                                if (lastPrice == null) {
                                    lastPrice = tInfo.getPrice();
                                    nc.setCurrentPrice(tInfo.getPrice());
                                    nc.setLastUpdated(tInfo.getLastUpdated());
                                }

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
                    }
                    nc.setTradeRank(rank);
                    nc.setRank(nc.getRank()+rank);
                    if(k>=20){
                        nc.setRank(nc.getRank()+20);
                        nc.setBotInvolved(true);
                    }
                    if(minuteTcount>=20){
                        nc.setRank(nc.getRank()+20);
                    }
                    nc.setTradeInfos(infoList);
                    tradeInfoMap.put(nc.getPairId(),infoList);
                    if(nc.getRank()>=20 && nc.getCurrentPrice().compareTo(baseTicker.getHigh24H())>=0) {
                        nc.setPercentChange(percFinder(nc.getCurrentPrice(),baseTicker.getLow24H()));
                        ponziCoinList.add(nc);
                        redisRepository.addNotification(nc);
                        //baseTicker.setRecentTrxRate(nc.getCurrentPrice());
                       // redisRepository.addTicker(baseTicker);
                        System.out.println("Found some spike -- >"+nc.getPairId());
                    }
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
        System.out.println(" coinList Size --> "+coinList.size());
        System.out.println(" coin Count Size --> "+coinCount);

        System.out.println("Total notification info stored -->"+redisRepository.findAllNotification().size());
        return  ponziCoinList;
    }

    @Scheduled(cron = "0 0 0 * * *",zone = "CST")
    public void cleanDailyRecords()throws Exception{
        System.out.println("SYSTEM REBOOTS THE DATA .");
        System.out.println("Deleting the notified coins list........................");
        cleanNotification();
        System.out.println("Deleting the Ticker updates for every coin........................");
        cleanTicker();
        System.out.println("Initializing the fresh Ticker........................");
        getTickerUpdate();Thread.sleep(2000);
        getTickerUpdate();Thread.sleep(2000);
        getTickerUpdate();Thread.sleep(2000);
    }

}