package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    List<Product> findByName(String name);

    List<Product> findByCategory(String category);

    @Query("{\"match\": {\"name\": {\"query\": \"?0\", \"analyzer\": \"ik_max_word\"}}}")
    List<Product> searchByName(String keyword);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
