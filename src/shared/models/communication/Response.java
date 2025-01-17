package shared.models.communication;

import java.io.Serial;
import java.io.Serializable;
//import java.util.Map;

//import shared.models.data.Message;


public class Response implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private final ResponseType type;
    private final Object payload;

//    public static final Map<ResponseType, Class> allowedPayloads = Map.of(
//      ResponseType.AccessDenied,         String.class,
//      ResponseType.UnknownCommand,       String.class,
//      ResponseType.InvalidArguments,     String.class,
//      ResponseType.InvalidCredentials,   String.class,
//      ResponseType.UsernameTaken,        String.class,
//      ResponseType.Authorized,           String.class,
//      ResponseType.UnknownTopic,         String.class,
//      ResponseType.TopicSubscribed,      String.class,
//      ResponseType.TopicUnsubscribed,    String.class,
//      ResponseType.PublishedMessage,
//      ResponseType.NewPublication,       Message.class
//    );

    public Response(ResponseType type, Object payload) {//throws IllegalArgumentException {
       // if (allowedPayloads.get(type) != payload.getClass())
      //      throw new IllegalArgumentException("Such payload is not allowed for this request type");
        this.type = type;
        this.payload = payload;
    }

    public ResponseType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

}