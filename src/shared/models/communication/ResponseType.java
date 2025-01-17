package shared.models.communication;

public enum ResponseType {
    Echo,
    AccessDenied,
    UnknownCommand,
    InvalidCommand,
    InvalidCredentials,
    UsernameTaken,
    RegistrationSuccess,
    AuthorizationSuccess,
    UnknownTopic,
    TopicSubscribed,
    TopicUnsubscribed,
    PublishedMessage,
    NewPublication
}