package com.chiradev.book.feedback;

import jakarta.validation.constraints.*;

public record FeedbackRequest(
        @NotNull(message = "200") // Ensures the field is not null
        @Min(value = 0, message = "201") // Minimum value is 0
        @Max(value = 5, message = "202") // Maximum value is 5
        Double note,

        @NotNull(message = "203") // Ensures the field is not null
        @NotEmpty(message = "203") // Ensures the string is not empty
        String comment,

        @NotNull(message = "204") // Ensures the field is not null
        Integer bookId
) {
}
