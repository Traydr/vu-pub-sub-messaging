package server.util;

import static shared.util.Styling.Colors.*;


public enum Templates {

    request__startup (

      "Enter " + BLUE_UNDERLINED + "run" + RESET + " if you wish to start the server",
      "or " + BLUE_UNDERLINED + "exit" + RESET + " to terminate the program"

    ),

    request__binding (

      "Enter " + BLUE_UNDERLINED + "auto" + RESET + " if you wish to automatically assign server",
      "port numbers or " + BLUE_UNDERLINED + "manual" + RESET + " to pick specific ones"

    ),

    request__ports_amount (

      PURPLE + "How many ports should the server use? " + GRAY + "(0 for default)"

    ),

    request__ports (

      PURPLE + "Enter server ports separated by commas",
      GRAY + '\'' + CYAN + "port1" + GRAY + ", " + CYAN + "port2" + GRAY + ", " +
        BLACK_BRIGHT + "..." + GRAY + "' (0 for auto assignment)",
      ""

    ),

    error__startup (

      "Only 'run' or 'exit' is permitted as input"

    ),

    error__binding (

      "Only 'auto' or 'manual' is permitted as input"

    ),

    error__ports_amount (

      "Illegal value, you are only allowed to enter numbers in range 0 - 8"

    ),

    error__port_range (

      "Invalid input, enter port numbers in range 0 - 65345 separated by commas"

    ),

    error__port_taken (

      "Port %d is taken by another process"

    ),

    error__unbind (

      "There was a problem while closing previously opened port"

    ),

    error__transmission (

      "There was a problem during communication, server state is invalid"

    ),

    message__starting (

      GRAY + "Server starting ..."

    ),

    message__stopping (

      GRAY + "Server stopping ..."

    ),

    message__channels_cleared (

      GRAY + "Channels cleared."

    ),

    message__enter_to_stop (

      GRAY_BACKGROUND + ' ' + GRAY_BRIGHT + BLUE_BACKGROUND + " Press " + BLACK_BACKGROUND_BRIGHT + " ENTER " +
        GRAY_BRIGHT + BLUE_BACKGROUND + " at any moment to stop the server " + GRAY_BACKGROUND + ' '

    ),


    message__network_info (

      PURPLE + "Network address" + RESET + ": " + BLUE + "%s" + GRAY + ','

    ),

    message__ports_info (

      PURPLE + "Listening on port" + PURPLE_BRIGHT + '(' + PURPLE+ 's' + PURPLE_BRIGHT + ')' + RESET + ": "

    ),

    message__waiting (

      BLUE_BOLD + "  %s" + GRAY + " Waiting for %s " + BLACK_BRIGHT + " %s  "

    ),

    message__new_connection (

      GRAY + '[' + YELLOW + "%d" + GRAY + ']' + RESET + ": " +
        PURPLE + "New connection from " + BLUE + "%s" + BLACK_BRIGHT + ':' + BLUE + "%d" + GRAY + '.'

    ),

    message__connection_unsuccessful (

      GRAY + "Unknown host accessed socket,",
      GRAY + "but the connection was dropped ..."

    ),

    message__connection_terminated (

      GRAY + '[' + YELLOW + "%d" + GRAY + ']' + RESET + ": " +
        PURPLE + "MessagingClient " + BLUE + "%s" + BLACK_BRIGHT + ':' + BLUE + "%d" +
        PURPLE + " has disconnected" + GRAY + '.'

    ),

    message__connection_dropped (

      GRAY + '[' + YELLOW + "%d" + GRAY + ']' + RESET + ": " +
        PURPLE + "Connection with client " + BLUE + "%s" + BLACK_BRIGHT + ':' + BLUE + "%d" +
        PURPLE + " was dropped" + GRAY + '.'

    ),

    port_format (

      RESET + "%d" + GRAY + ", "

    ),

    program_title (

      GRAY + ":".repeat(17),
      GRAY + ":: " + BLUE_BOLD + "MQTT BROKER" + GRAY + " ::",
      GRAY + ":".repeat(17)

    );

    public final String val;
    public final String[] data;

    Templates(String... data) {
        val = data.length == 1 ? data[0] : null;
        this.data = data;
    }

}