package com.example.tariffkey.service;

import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
import org.springframework.stereotype.Service;

@Service
public class TariffService {

    public TariffResponse calculate(TariffRequest request) {
        double tariffAmount = request.getItemPrice() * request.getTariffRate();
        double total = request.getItemPrice() + tariffAmount;

        TariffResponse response = new TariffResponse();
        response.setItemPrice(request.getItemPrice());
        response.setTariffRate(request.getTariffRate());
        response.setTotalPrice(total);
        return response;
    }
}
