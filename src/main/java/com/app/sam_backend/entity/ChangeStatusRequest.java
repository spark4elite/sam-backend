package com.app.sam_backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeStatusRequest {
    @Schema(description = "The ID of the Redirect entry", example = "1", required = true)
    private Long id;

    @Schema(description = "The new status of the Redirect entry", example = "true", required = true, type = "boolean", allowableValues = {"true", "false"})
    private Boolean status;
}
