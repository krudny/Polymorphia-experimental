package com.agh.polymorphia_backend.service.task.remote_client;

import com.agh.polymorphia_backend.dto.request.task.RemoteExecutionRequestDto;
import com.agh.polymorphia_backend.dto.response.task.RemoteExecutionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CodeExecutorClient {
    private final RestClient codeExecutorRestClient;

    public RemoteExecutionResponseDto executeSync(RemoteExecutionRequestDto request) {
        try {
            return codeExecutorRestClient
                    .post()
                    .uri("/executions/sync")
                    .body(request)
                    .retrieve()
                    .body(RemoteExecutionResponseDto.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Moduł wykonywania kodu nie jest dostępny", exception);
        }
    }
}
