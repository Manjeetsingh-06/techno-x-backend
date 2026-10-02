package com.technox.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventApprovalRequest {

    @NotNull(message = "Approval decision is required")
    private Boolean approved;

    private String reason;
}
