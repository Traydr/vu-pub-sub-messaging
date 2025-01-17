package shared.util;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.out;

import shared.Config;

import static shared.util.Styling.Colors.*;
import static shared.util.Styling.Templates.*;


public class Styling {

    // ### Instance ### :

    private static final AtomicBoolean isNewSection = new AtomicBoolean(true);
    private static final AtomicBoolean holdLastLine = new AtomicBoolean(false);
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static  byte spinAnimationIndex = 0, waveAnimationIndex = 0;

    private Styling() {}

    // ### Text Output ### :

    private static synchronized void print(
      String oldSectionPrefix, String newSectionPrefix, String interlineInsert,
      String linePrefix, String lastLinePrefix, String... lines
    ) {
        var output = new StringBuilder();
        if (lines.length > 0)
            output
              .append(isNewSection.getAndSet(false) ? newSectionPrefix : oldSectionPrefix)
              .append(lines.length == 1 ? lastLinePrefix : linePrefix)
              .append(lines[0])
              .append(Colors.RESET);
        for (var i = 1; i < lines.length; i++)
            output
              .append(interlineInsert)
              .append(i == lines.length - 1 ? lastLinePrefix : linePrefix)
              .append(lines[i])
              .append(Colors.RESET);
        if (!holdLastLine.getAndSet(false))
            output.append(lineFeed);
        out.print(output);
    }

    public static void print(String... lines) {
        print(carriageReset, lineFeed, lineFeed, "", "", lines);
    }

    public static void printText(String... lines) {
        print(alignedLargePadding, newlineLargePadding, newlineLargePadding, "", "", lines);
    }

    private static void printSigned(String sign, String... lines) {
        print(alignedPadding, sectionPadding, newlinePadding, paragraphSign, sign, lines);
    }

    public static void printMessage(String... lines) {
        printSigned(messageSign, lines);
    }

    public static synchronized void printError(String message) {
        out.print(newlineLargePadding + Colors.RED + message + '.');
        if (holdLastLine.getAndSet(false)) return;
        out.print(lineFeed);
        isNewSection.set(true);
    }

    public static void printSeparator() {
        isNewSection.set(true);
    }

    public static void preventLastFeed() {
        holdLastLine.set(true);
    }

    // ### Data Input ### :

    @FunctionalInterface
    public interface InterruptibleFunction<T, R> {
        R apply(T func) throws Exception;
    }

    @FunctionalInterface
    public interface InterruptibleConsumer<T> {
        void accept(T func) throws Exception;
    }

    private static String readLine() throws InterruptedException, IOException {
        synchronized (reader) {
            for (;;) {
                if (reader.ready())
                    return reader.readLine();
                Thread.sleep(Config.INPUT_REFRESH_MILLIS);
            }
        }
    }

    public static synchronized String requestInput(String... messageLines) throws InterruptedException {
        preventLastFeed();
        printSigned(inputSign, messageLines.length > 0 ? messageLines : new String[]{""});
        out.print(Colors.RESET + ": " + Colors.BLACK_BRIGHT);
        try {
            var length = 0;
            if (messageLines.length > 0)
                length = messageLines[messageLines.length - 1]
                  .replaceAll("(\\x9B|\\x1B\\[)[0-?]*[ -/]*[@-~]", "").length();
            String input, padding = alignedLargePadding + " ".repeat(length + 2);
            for (;;)
                if ((input = readLine()).isBlank())
                    out.print(padding);
                else return input.trim();
        } catch (IOException e) {
            return "";
        } catch (InterruptedException e) {
            out.println();
            throw e;
        } finally {
            out.print(carriageReset);
        }
    }

    public static <T> T requestInput(
      InterruptibleFunction<String, T> inputParser, String errorMessage, String... messageLines
    ) throws InterruptedException {
        for (;;) {
            var input = requestInput(messageLines);
            try {
                return inputParser.apply(input);
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                printSeparator();
                if (errorMessage.isBlank() && (e.getMessage() == null || e.getMessage().isBlank())) continue;
                printError(errorMessage.isBlank() ? e.getMessage() : errorMessage);
            }
        }
    }

    public static void requestAndHandleInput(
      InterruptibleConsumer<String> inputHandler, String errorMessage, String... messageLines
    ) throws InterruptedException {
        for (;;) {
            var input = requestInput(messageLines);
            try {
                inputHandler.accept(input);
                break;
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                printSeparator();
                if (errorMessage.isBlank() && (e.getMessage() == null || e.getMessage().isBlank())) continue;
                printError(errorMessage.isBlank() ? e.getMessage() : errorMessage);
            }
        }
    }

    public static void blockUntilEnter() throws InterruptedException {
        try {
            readLine();
        } catch (IOException ignored) {}
    }

    // ### Loader ### :

    public synchronized static void tickLoader(String message) {
        var wave = waveLoader.substring(0, waveAnimationIndex++ / 3);
        preventLastFeed();
        print(
          BLUE_BOLD + "  " + padRight(wave, waveLoader.length()) +
            GRAY + ' ' + message +
            BLACK_BRIGHT + "  " + spinLoaderSequence.charAt(spinAnimationIndex++) + "  "
        );
        if (spinAnimationIndex == spinLoaderSequence.length())
            spinAnimationIndex = 0;
        if (waveAnimationIndex == waveLoader.length() * 4 - 1)
            waveAnimationIndex = 0;
    }

    // ### String Manipulation ### :

    public static String padLeft(String text, int totalLength) {
        return totalLength > 0 ? String.format("%" + totalLength + 's', text) : text;
    }

    public static String padRight(String text, int totalLength) {
        return totalLength > 0 ? String.format("%-" + totalLength + 's', text) : text;
    }

    // ### Templates ### :

    protected static class Templates {

        private static final String padding = " ".repeat(Config.CONSOLE_PADDING_SIZE);
        private static final String largePadding = padding.repeat(3);

        private static String generateSign(char sign) {
            return
              Colors.RESET + Colors.BLACK_BACKGROUND_BRIGHT + ' ' + sign + ' ' +
                Colors.RESET + padRight("", Config.CONSOLE_PADDING_SIZE * 2 - 3);
        }

        public static final String carriageReset = "\r" + Colors.RESET;
        public static final String alignedPadding = carriageReset + padding;
        public static final String alignedLargePadding = carriageReset + largePadding;
        public static final String lineFeed = '\n' + carriageReset;
        public static final String newlinePadding = lineFeed + padding;
        public static final String newlineLargePadding = lineFeed + largePadding;
        public static final String sectionPadding =
          lineFeed + Colors.GRAY + padRight(">", Config.CONSOLE_PADDING_SIZE) + Colors.RESET;
        public static final String paragraphSign = generateSign('|');
        public static final String messageSign = generateSign('!');
        public static final String inputSign = generateSign('#');
        public static final String waveLoader = "'!|";
        public static final String spinLoaderSequence = "|/-\\";

    }

    // ### ANSI ### :

    public static class Colors {

        public static final String RESET = "\033[0m";

        // Regular
        public static final String BLACK = "\033[0;30m";   // BLACK
        public static final String RED = "\033[0;31m";     // RED
        public static final String GREEN = "\033[0;32m";   // GREEN
        public static final String YELLOW = "\033[0;33m";  // YELLOW
        public static final String BLUE = "\033[0;34m";    // BLUE
        public static final String PURPLE = "\033[0;35m";  // PURPLE
        public static final String CYAN = "\033[0;36m";    // CYAN
        public static final String GRAY = "\033[0;37m";    // GRAY

        // Bold
        public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
        public static final String RED_BOLD = "\033[1;31m";    // RED
        public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
        public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
        public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
        public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
        public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
        public static final String GRAY_BOLD = "\033[1;37m";   // GRAY

        // Underlined
        public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
        public static final String RED_UNDERLINED = "\033[4;31m";    // RED
        public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
        public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
        public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
        public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
        public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
        public static final String GRAY_UNDERLINED = "\033[4;37m";   // GRAY

        // Background
        public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
        public static final String RED_BACKGROUND = "\033[41m";    // RED
        public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
        public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
        public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
        public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
        public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
        public static final String GRAY_BACKGROUND = "\033[47m";   // GRAY

        // High Intensity
        public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
        public static final String RED_BRIGHT = "\033[0;91m";    // RED
        public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
        public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
        public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
        public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
        public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
        public static final String GRAY_BRIGHT = "\033[0;97m";   // GRAY

        // Bold High Intensity
        public static final String BLACK_BOLD_BRIGHT = "\033[1;90m";  // BLACK
        public static final String RED_BOLD_BRIGHT = "\033[1;91m";    // RED
        public static final String GREEN_BOLD_BRIGHT = "\033[1;92m";  // GREEN
        public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m"; // YELLOW
        public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";   // BLUE
        public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m"; // PURPLE
        public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";   // CYAN
        public static final String GRAY_BOLD_BRIGHT = "\033[1;97m";   // GRAY

        // High Intensity Background
        public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";  // BLACK
        public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";    // RED
        public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";  // GREEN
        public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m"; // YELLOW
        public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";   // BLUE
        public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
        public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";   // CYAN
        public static final String GRAY_BACKGROUND_BRIGHT = "\033[0;107m";   // GRAY

    }

}