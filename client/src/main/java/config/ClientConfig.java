package config;

import client.api.BackendRestClient;

public class ClientConfig {

    public static BackendRestClient createBackendRestClient() {
        String backendUrl = System.getenv()
                .getOrDefault(
                        "BACKEND_URL",
                        "http://localhost:8082"
                );

        return new BackendRestClient(backendUrl);
    }
}
