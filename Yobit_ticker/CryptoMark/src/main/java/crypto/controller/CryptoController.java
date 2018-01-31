package crypto.controller;

import crypto.model.PairInfo;
import crypto.model.TickerStore;
import crypto.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by bibiKT .
 */
@RestController
@RequestMapping("/crypto")
public class CryptoController {


    @Autowired
    CryptoService cryptoService;


    @GetMapping("/info")
    public ResponseEntity<TickerStore> getTickerInfo() {

        System.out.print("Hi reached ,myserver");
        //PairInfo pairInfo = sosRepository.findOne(noteId);
       // PairInfo pairInfo = cryptoService.activateCoinPair();
       // cryptoService.updateCoinPairInfo();
        cryptoService.runAnalizerBotOnYobit();
       // System.out.print("Bibin got out put -->"+cryptoService.getValue("1").getActivePairId().size());
        return ResponseEntity.ok().body(cryptoService.getTickerUpdate("base@alc_btc"));
    }
}
