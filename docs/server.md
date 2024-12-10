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

### Registration

`register [USERNAME] [PASSWORD]`

- `[USERNAME]`: The desired username of the client.
- `[PASSWORD]`: The password associated with the username.

Upon successful registration, the user's details (username and hashed password) are stored in the 
server's in-memory database.

**Notes**:
- The database starts empty each time the server is started and persists only for the duration of 
the server's runtime.
- Passwords are securely hashed using the SHA-256 algorithm before being stored in memory.

**Server Responses**:
- `"Registration successful! You can now log in."` (If registration is successful).
- `"Registration failed, username already exists."` (If the username is already taken).

### Logging in 

`login [USERNAME] [PASSWORD]`

- `[USERNAME]`: The username previously registered with the server.
- `[PASSWORD]`: The password associated with the username.


The `login` command is used to authenticate a user with their credentials stored in the server's 
in-memory database. Only authenticated users can proceed to publish or subscribe to topics.

**Behavior:**

The server checks the provided username and password against the in-memory database.
Passwords are hashed using SHA-256 before comparison to ensure security.
Server Responses:

- `Login successful!` - If the provided username and password are valid.
- `Login failed, invalid username or password.` - If the credentials do not match any entry in 
the database.

### Security
- Passwords are hashed using SHA-256 before being stored in memory.
- Plain-text passwords are never stored or sent to the client.


## List of Other Commands

- `disconnect`