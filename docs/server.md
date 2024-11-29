# Server Protocol Documentation

The first message sent to the server must be either a empty `pub` command or a valid `sub` command

## Publishing

`pub [topic] [message]`

A publisher must always transmit their topic.
The topic may not contain spaces, recommendation is to name topics like `topic/subTopic/etc`.
Everything after the topic is considered part of the message.

## Subscribing

`sub [topic]`

Client can subscribe to multiple topics, they just have to send the sub message for each of them.

When receiving a message they will arrive in the form `topic:message`.
The server guarantees that topic does not contain the `:` character.

## List of Other Commands

- `disconnect`