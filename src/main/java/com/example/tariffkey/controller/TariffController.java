package com.example.tariffkey.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tariffkey.model.Tariff;
import com.example.tariffkey.model.TariffApiRequest;
import com.example.tariffkey.model.TariffApiResponse;
import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
// import com.example.tariffkey.service.TariffService;
import com.example.tariffkey.service.TariffService2;
import com.example.tariffkey.service.DefaultQuoteService;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/tariff")
@CrossOrigin(origins = "http://localhost:5173")
public class TariffController {

    private final TariffService2 tariffService2;
    private final DefaultQuoteService tariffQuote;

    public TariffController(TariffService2 tariffService2, DefaultQuoteService tariffQuote) {
        this.tariffService2 = tariffService2;
        this.tariffQuote = tariffQuote;
    }

    @PostMapping("/calculate")
    public TariffResponse calculate(@RequestBody TariffRequest request) {
        System.out.println("Received response");
        return tariffService2.calculate(request);
    }
    
    @PostMapping("/quote")
    public TariffApiResponse quote(@RequestBody TariffApiRequest quote){
      System.out.println("Sending request to API...\n");
      return tariffQuote.fetchFromApi(quote);
    }
    

    // ====== Admin Tariff Management ======
    @GetMapping("/all")
    public List<Tariff> getAllTariffs() {
        return tariffService2.getAllTariffs();
    }

    @PostMapping("/add")
    public Tariff addTariff(@RequestBody Tariff tariff) {
        return tariffService2.addTariff(tariff);
    }

    @DeleteMapping("/{id}")
    public void deleteTariff(@PathVariable int id) {
        tariffService2.deleteTariff(id);
    }

    @GetMapping
    public String testEndpoint() {
        return "TariffController active";
    }
}
