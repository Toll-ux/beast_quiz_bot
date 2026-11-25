package config;

import client.api.BackendRestClient;

public class ClientConfig {

    public static BackendRestClient createBackendRestClient(String baseUrl) {
        return new BackendRestClient(baseUrl);
    }
}
