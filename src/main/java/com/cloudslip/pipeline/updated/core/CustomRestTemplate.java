package com.cloudslip.pipeline.updated.core;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class CustomRestTemplate extends RestTemplate {

    @Nullable
    public <T> T putForObject(String url, @Nullable Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        RequestCallback requestCallback = this.httpEntityCallback(request, responseType);
        ResponseExtractor<ResponseEntity<T>> responseExtractor = this.responseEntityExtractor(responseType);
        return this.execute(url, HttpMethod.PUT, requestCallback, (ResponseExtractor<T>) responseExtractor, (Object[])uriVariables);
    }

    @Nullable
    public <T> T deleteForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
        ResponseExtractor<ResponseEntity<T>> responseExtractor = this.responseEntityExtractor(responseType);
        return this.execute(url, HttpMethod.DELETE, (RequestCallback)null, (ResponseExtractor<T>) responseExtractor, (Object[])uriVariables);
    }

}
