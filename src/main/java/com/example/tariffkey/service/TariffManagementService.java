package com.example.tariffkey.service;

import com.example.tariffkey.model.Tariff;
import com.example.tariffkey.repository.TariffRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TariffManagementService {

    private final TariffRepository tariffRepository;

    public TariffManagementService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    public List<Tariff> getAllTariffs() {
        return tariffRepository.findAll();
    }

    public Tariff addTariff(Tariff tariff) {
        return tariffRepository.save(tariff);
    }

    public void deleteTariff(long id) {
        tariffRepository.deleteById(id);
    }
}
