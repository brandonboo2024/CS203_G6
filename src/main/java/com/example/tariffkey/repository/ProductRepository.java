package com.example.tariffkey.repository;

import com.example.tariffkey.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);

    List<Product> findByHsCodeIn(Collection<String> hsCodes);
}
