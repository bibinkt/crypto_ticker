package crypto.controller;

import crypto.model.PairInfo;
import crypto.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by bibiKT .
 */
@RestController
@RequestMapping("/crypto")
public class SosController {


    @Autowired
    CryptoService cryptoService;


    @GetMapping("/info")
    public ResponseEntity<PairInfo> getTickerInfo() {

        System.out.print("Hi reached ,myserver");
        //PairInfo pairInfo = sosRepository.findOne(noteId);
        PairInfo pairInfo = cryptoService.activateCoinPair();

        return ResponseEntity.ok().body(pairInfo);
    }
}
