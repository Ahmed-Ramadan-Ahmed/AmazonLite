package com.amazonlite.product.query.repository;

import com.amazonlite.product.query.document.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDocumentRepository extends MongoRepository<ProductDocument, String> {

    // For customers browsing a specific seller's store
    List<ProductDocument> findBySellerIdAndActiveTrue(String sellerId);

    // For basic keyword searches
    List<ProductDocument> findByTitleContainingIgnoreCaseAndActiveTrue(String title);
}