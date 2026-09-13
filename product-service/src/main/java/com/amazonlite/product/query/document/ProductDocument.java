package com.amazonlite.product.query.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products_view")
public class ProductDocument {

    @Id
    private String id; // Matches the UUID from PostgreSQL

    @Indexed
    private String sellerId;

    // We can index the title for basic text searches, though PgVector will handle complex semantic searches later
    @Indexed
    private String title;

    private String description;

    private BigDecimal price;

    @Indexed
    private String sku;

    private boolean active;

    // MongoDB allows us to store arrays natively, perfect for quick filtering
    private List<String> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}