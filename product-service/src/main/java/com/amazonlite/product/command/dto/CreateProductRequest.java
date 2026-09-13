package com.amazonlite.product.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Title is required")
    @Length(min = 1, max = 100)
    private String title;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be strictly positive")
    private BigDecimal price;

    @NotBlank(message = "SKU is required")
    private String sku;
}