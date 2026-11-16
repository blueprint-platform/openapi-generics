package example;

import example.generated.api.GreetingApi;
import example.generated.model.Greeting;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;

public final class UseClient {
    public ServiceResponse<Greeting> fetch(GreetingApi api) {
        return api.getGreeting();
    }
}
