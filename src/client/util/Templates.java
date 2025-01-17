package client.util;

import static shared.util.Styling.Colors.*;


public enum Templates {

    request__startup (

      "Enter " + BLUE_UNDERLINED + "run" + RESET + " if you wish to start the client",
      "or " + BLUE_UNDERLINED + "exit" + RESET + " to terminate the program"

    ),

    request__join (

      "Enter " + BLUE_UNDERLINED + "stop" + RESET + " if you wish to stop the client",
      "or " + BLUE_UNDERLINED + "join" + RESET + " to connect to the messaging server"

    ),

    request__address (

      PURPLE + "Enter server " + PURPLE_BOLD + "IP address"

    ),

    request__port (

      PURPLE + "Enter application " + PURPLE_BOLD + "port"

    ),

    error__startup (

      "Only 'run' or 'exit' is permitted as input"

    ),

    error__join (

      "Only 'stop' or 'join' is permitted as input"

    ),

    error__port_range (

      "Invalid input, port must be a number in range 0 - 65345"

    ),

    message__starting (

      GRAY + "Client starting ..."

    ),

    message__stopping (

      GRAY + "Client stopping ... "

    ),

    program_title (

      GRAY + ":".repeat(17),
      GRAY + ":: " + BLUE_BOLD + "MQTT CLIENT" + GRAY + " ::",
      GRAY + ":".repeat(17)

    );

    public final String val;
    public final String[] data;

    Templates(String... data) {
        val = data.length == 1 ? data[0] : null;
        this.data = data;
    }
}