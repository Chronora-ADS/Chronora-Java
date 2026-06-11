package br.com.senai.model.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDTO {

    @NotNull(message = "A nota e obrigatoria.")
    @Min(value = 1, message = "A nota minima e 1.")
    @Max(value = 5, message = "A nota maxima e 5.")
    private Integer rating;
}
