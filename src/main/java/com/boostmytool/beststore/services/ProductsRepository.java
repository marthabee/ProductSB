package com.boostmytool.beststore.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.boostmytool.beststore.models.Products;

public interface ProductsRepository extends JpaRepository<Products, Long>, JpaSpecificationExecutor<Products> {
}
