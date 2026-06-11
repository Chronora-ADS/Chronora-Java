package br.com.senai.model.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDTO {

    @NotNull(message = "A nota e obrigatoria.")
    @DecimalMin(value = "0.5", message = "A nota minima e 0.5.")
    @DecimalMax(value = "5.0", message = "A nota maxima e 5.")
    private Double rating;
}
