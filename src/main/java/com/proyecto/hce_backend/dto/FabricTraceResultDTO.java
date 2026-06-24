package com.proyecto.hce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FabricTraceResultDTO {

    private Boolean success;
    private Boolean registeredInFabric;
    private String transactionId;
    private String channel;
    private String chaincode;
    private String errorMessage;
}
