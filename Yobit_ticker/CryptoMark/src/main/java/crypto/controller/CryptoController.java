package crypto.controller;

import crypto.model.NotifiedCoin;
import crypto.model.PairInfo;
import crypto.model.TickerStore;
import crypto.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by bibiKT .
 */
@RestController
@RequestMapping("/crypto")
public class CryptoController {

    @Autowired
    CryptoService cryptoService;


    @GetMapping("/info/{id}")
    public ResponseEntity<TickerStore> getTickerInfo(@PathVariable(value = "id") String pair) {

        System.out.print("Hi reached ,myserver -->"+"base@"+pair+"_btc");
        //PairInfo pairInfo = sosRepository.findOne(noteId);
       // PairInfo pairInfo = cryptoService.activateCoinPair();
       cryptoService.getTickerUpdate();
       //cryptoService.runAnalizerBotOnYobit();
       // System.out.print("Bibin got out put -->"+cryptoService.getValue("1").getActivePairId().size());
        return ResponseEntity.ok().body(cryptoService.getTickerUpdate("base@"+pair+"_btc"));
        //return ResponseEntity.ok().body(cryptoService.getPairInfoDetail(pair+"_btc"));

    }
    @GetMapping("/allNotification")
    public ResponseEntity<Map<Object,NotifiedCoin>> getAllNotification() {

        return ResponseEntity.ok().body(cryptoService.getAllNotification());

    }

    @GetMapping("/clean")
    public ResponseEntity<String> clean() {

        //PairInfo pairInfo = sosRepository.findOne(noteId);
        // PairInfo pairInfo = cryptoService.activateCoinPair();
        // cryptoService.updateCoinPairInfo();
        cryptoService.cleanTicker();
        cryptoService.cleanNotification();
        // System.out.print("Bibin got out put -->"+cryptoService.getValue("1").getActivePairId().size());
        //return ResponseEntity.ok().body(cryptoService.getTickerUpdate("base@"+pair+"_btc"));
        return ResponseEntity.ok().body("Truncated Ticker & Notification");

    }
    @GetMapping("/bot")
    public ResponseEntity<String> callBot() {

        //PairInfo pairInfo = sosRepository.findOne(noteId);
        // PairInfo pairInfo = cryptoService.activateCoinPair();
        // cryptoService.updateCoinPairInfo();
        cryptoService.runAnalizerBotOnYobit();
        // System.out.print("Bibin got out put -->"+cryptoService.getValue("1").getActivePairId().size());
        //return ResponseEntity.ok().body(cryptoService.getTickerUpdate("base@"+pair+"_btc"));
        return ResponseEntity.ok().body("Check your email");

    }
}
