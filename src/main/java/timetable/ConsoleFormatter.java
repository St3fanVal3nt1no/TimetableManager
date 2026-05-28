package timetable;

import java.util.ArrayList;
import java.util.List;

public class ConsoleFormatter {

    // -----------------------------------------------------------------------
    // ANSI escape codes
    // -----------------------------------------------------------------------

    public static final String RESET = "\u001b[0m";
    public static final String BOLD = "\u001b[1m";
    public static final String RED = "\u001b[31m";
    public static final String GREEN = "\u001b[32m";
    public static final String YELLOW = "\u001b[33m";
    public static final String CYAN = "\u001b[36m";
    public static final String MAGENTA = "\u001b[35m";

    // -----------------------------------------------------------------------
    // Layout
    // -----------------------------------------------------------------------

    public static final int WIDTH    = 56;
    private static final String H_BAR = "=".repeat(WIDTH);
    private static final String THIN  = "-".repeat(WIDTH);

    // -----------------------------------------------------------------------
    // ASCII art title  (figlet "standard" font)
    // -----------------------------------------------------------------------

    private static final String[] ASCII_TITLE = {
            " _____ _                _        _     _      ",
            "|_   _(_)_ __ ___   ___| |_ __ _| |__ | | ___ ",
            "  | | | | '_ ` _ \\ / _ \\ __/ _` | '_ \\| |/ _ \\",
            "  | | | | | | | | |  __/ || (_| | |_) | |  __/",
            "  |_| |_|_| |_| |_|\\___|\\__\\__,_|_.__/|_|\\___|",
            " __  __                                        ",
            "|  \\/  | __ _ _ __   __ _  __ _  ___ _ __     ",
            "| |\\/| |/ _` | '_ \\/ _` |/ _` |/ _ \\ '__|    ",
            "| |  | | (_| | | | | (_| | (_| |  __/ |       ",
            "|_|  |_|\\__,_|_| |_|\\__,_|\\__, |\\___|_|       ",
            "                          |___/              "
    };

    // -----------------------------------------------------------------------
    // Title & structural elements
    // -----------------------------------------------------------------------

    public static void printTitle() {
        System.out.println(CYAN + H_BAR + RESET);
        for (String line : ASCII_TITLE) {
            System.out.println(CYAN + line + RESET);
        }
        System.out.println(CYAN + H_BAR + RESET);
        System.out.println();
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println(YELLOW + H_BAR + RESET);
        int pad = (WIDTH - title.length()) / 2;
        System.out.println(YELLOW + " ".repeat(Math.max(0, pad)) + BOLD + title + RESET);
        System.out.println(YELLOW + H_BAR + RESET);
    }

    public static void printSeparator() {
        System.out.println(YELLOW + THIN + RESET);
    }

    // -----------------------------------------------------------------------
    // Menu items
    // -----------------------------------------------------------------------

    public static void printMenuItem(int number, String label) {
        System.out.println("  " + CYAN + "[" + number + "]" + RESET + " " + label);
    }

    public static void printReturnItem() {
        System.out.println("  " + CYAN + "[0]" + RESET + " Return");
    }

    // -----------------------------------------------------------------------
    // Status messages
    // -----------------------------------------------------------------------

    public static void printSuccess(String message) {
        System.out.println(GREEN + "  [OK] " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "  [!]  " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(CYAN + "  [i]  " + message + RESET);
    }

    // -----------------------------------------------------------------------
    // Class table
    // -----------------------------------------------------------------------

    // Column widths (characters of visible content, excluding borders/padding).
    private static final int[] COL_W = { 4, 32, 28, 12, 5, 9, 13, 30 };

    private static final String[] COL_HEADERS = {
            "#", "Topic", "Availability", "Class", "Inst", "Day", "Time", "Location"
    };

    public static void printClassTable(List<ClassSchedule> classes) {
        String divider = buildDivider(COL_W);

        System.out.println(divider);
        System.out.println(BOLD + buildRow(COL_HEADERS, COL_W) + RESET);
        System.out.println(divider);

        for (int i = 0; i < classes.size(); i++) {
            ClassSchedule cs = classes.get(i);
            String[] values = {
                    String.valueOf(i + 1),
                    cs.getTopic(),
                    cs.getAvailability(),
                    cs.getClassName(),
                    cs.getClassInstance(),
                    cs.getDay(),
                    cs.getTime(),
                    cs.getLocation()
            };
            for (String line : buildWrappedRows(values, COL_W)) {
                System.out.println(line);
            }
            System.out.println(divider);
        }
    }

    // Builds a +----+--------+ divider matching the given column widths.
    private static String buildDivider(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            for (int i = 0; i < w + 2; i++) sb.append('-');
            sb.append('+');
        }
        return sb.toString();
    }

    // Builds a | val | val | row, padding and truncating each cell.
    private static String buildRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String val = (i < values.length) ? values[i] : "";
            sb.append(' ').append(cell(val, widths[i])).append(" |");
        }
        return sb.toString();
    }

    // Builds one or more pipe-bordered row strings for a data row, wrapping long cells.
    private static String[] buildWrappedRows(String[] values, int[] widths) {
        List<List<String>> cells = new ArrayList<>();
        int maxLines = 1;
        for (int i = 0; i < widths.length; i++) {
            String val = (i < values.length && values[i] != null) ? values[i] : "";
            List<String> lines = wrapText(val, widths[i]);
            cells.add(lines);
            if (lines.size() > maxLines) maxLines = lines.size();
        }
        String[] rows = new String[maxLines];
        for (int line = 0; line < maxLines; line++) {
            StringBuilder sb = new StringBuilder("|");
            for (int col = 0; col < widths.length; col++) {
                List<String> cellLines = cells.get(col);
                String content = line < cellLines.size() ? cellLines.get(line) : pad("", widths[col]);
                sb.append(' ').append(content).append(" |");
            }
            rows[line] = sb.toString();
        }
        return rows;
    }

    // Wraps text into lines that fit within width. Each line is right-padded to width.
    private static List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        if (text == null) text = "";
        while (text.length() > width) {
            int breakAt = width;
            // Prefer breaking at a space in the latter half of the segment
            for (int i = width - 1; i > width / 2; i--) {
                if (text.charAt(i) == ' ') { breakAt = i; break; }
            }
            lines.add(pad(text.substring(0, breakAt), width));
            text = text.substring(breakAt).trim();
        }
        lines.add(pad(text, width));
        return lines;
    }

    // Right-pads a string to exactly width characters.
    private static String pad(String value, int width) {
        if (value == null) value = "";
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    // Pads the header cell (truncation acceptable — headers are always short).
    private static String cell(String value, int width) {
        if (value == null) value = "";
        if (value.length() > width) value = value.substring(0, width - 2) + "..";
        return pad(value, width);
    }

    // -----------------------------------------------------------------------
    // Class details (full 8-field view)
    // -----------------------------------------------------------------------

    public static void printClassDetails(ClassSchedule cs) {
        printSeparator();
        field("Topic         ", cs.getTopic());
        field("Availability  ", cs.getAvailability());
        field("Class         ", cs.getClassName());
        field("Class Instance", cs.getClassInstance());
        field("Date          ", cs.getDate());
        field("Day           ", cs.getDay());
        field("Time          ", cs.getTime());
        field("Location      ", cs.getLocation());
        printSeparator();
    }

    // -----------------------------------------------------------------------
    // Timetable display
    // -----------------------------------------------------------------------

    public static void printTimetableSummary(int index, Timetable t) {
        System.out.println(
                "  " + CYAN + "[" + index + "]" + RESET
                        + " " + BOLD + t.getTimetableName() + RESET
                        + "  |  " + t.getScheduledClasses().size() + " class(es)"
        );
    }

    public static void printTimetableDetails(Timetable t) {
        printSeparator();
        field("Name       ", t.getTimetableName());
        field("Topics     ", listOrNone(t.getSelectedTopics()));
        field("Campuses   ", listOrNone(t.getSelectedCampuses()));
        field("Preferences", listOrNone(t.getPreferences()));
        field("Classes    ", t.getScheduledClasses().size() + " scheduled");
        printSeparator();

        List<ClassSchedule> scheduled = t.getScheduledClasses();
        if (!scheduled.isEmpty()) {
            System.out.println(BOLD + "  Scheduled classes:" + RESET);
            printClassTable(scheduled);
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private static void field(String label, String value) {
        System.out.println("  " + BOLD + label + RESET + " : " + value);
    }

    private static String listOrNone(List<String> list) {
        return list.isEmpty() ? "(none)" : String.join(", ", list);
    }
}

