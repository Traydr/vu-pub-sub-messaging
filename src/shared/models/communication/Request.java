package shared.models.communication;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import shared.models.data.Credentials;
import shared.models.generics.Pair;


public class Request implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private final RequestType type;
    private final Object payload;

    private static final Map<RequestType, Class> allowedPayloads = Map.of(
      RequestType.Login,       Credentials.class,
      RequestType.Register,    Credentials.class,
      RequestType.Subscribe,   String.class,
      RequestType.Publish,     Pair.class
    );

    public Request(RequestType type) throws IllegalArgumentException {
        if (type != RequestType.Ping && type != RequestType.Disconnect)
            throw new IllegalArgumentException("This request requires a payload");
        this.type = type;
        this.payload = null;
    }

    public Request(RequestType type, Object payload) throws IllegalArgumentException {
        if (type == RequestType.Ping || type == RequestType.Disconnect)
            throw new IllegalArgumentException("This request type cannot have a payload");
        if (allowedPayloads.get(type) != payload.getClass())
            throw new IllegalArgumentException("Such payload is not allowed for this request type");
        this.type = type;
        this.payload = payload;
    }

    public RequestType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

}