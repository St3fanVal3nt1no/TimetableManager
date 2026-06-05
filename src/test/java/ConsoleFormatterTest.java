import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import timetable.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.DisplayName.class)
public class ConsoleFormatterTest {
    private ByteArrayOutputStream outContent;
    private PrintStream            originalOut;

    @BeforeEach
    void redirectOutput() {
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));}

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
        outContent = null;}

    private String captured() {
        return outContent.toString();}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - Success Marker Test")
    void SuccessTest() {
        ConsoleFormatter.printSuccess("Operation complete");
        assertTrue(captured().contains("[OK]"));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - Successful Import Message Test")
    void SuccessImportTest() {
        ConsoleFormatter.printSuccess("Import successful");
        assertTrue(captured().contains("Import successful"));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - Successful Export Message Test")
    void SuccessExportTest() {
        ConsoleFormatter.printSuccess("Timetable exported successfully");
        String output = captured();
        assertAll(() -> assertTrue(output.contains("[OK]")),() -> assertTrue(output.contains("Timetable exported successfully")));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - Error Marker Test")
    void ErrorTest() {
        ConsoleFormatter.printError("Something went wrong");
        assertTrue(captured().contains("[!]"));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - Error Message Test")
    void ErrorMessageTest() {
        ConsoleFormatter.printError("File not found: test.csv");
        assertTrue(captured().contains("File not found: test.csv"));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - No File Path Error Test")
    void NoPathTest() {
        ConsoleFormatter.printError("No file path entered.");
        String output = captured();
        assertAll(() -> assertTrue(output.contains("[!]")),() -> assertTrue(output.contains("No file path entered.")));}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.07 - Missing Column Error Test")
    void NoColumnTest() {
        ConsoleFormatter.printError("Missing required column(s): location");
        String output = captured();
        assertAll(() -> assertTrue(output.contains("[!]")),() -> assertTrue(output.contains("Missing required column(s): location")));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - Info Marker Test")
    void InfoMarkerTest() {
        ConsoleFormatter.printInfo("3 classes loaded.");
        assertTrue(captured().contains("[i]"));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - Exit Message Test")
    void ExitMessageTest() {
        ConsoleFormatter.printInfo("Goodbye!");
        assertTrue(captured().contains("Goodbye!"));}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.07 - Message Test")
    void MessageTest() {
        ConsoleFormatter.printInfo("Total in system: 5 class(es).");
        String output = captured();
        assertAll(() -> assertTrue(output.contains("[i]")),() -> assertTrue(output.contains("Total in system: 5 class(es).")));}

    @ParameterizedTest(name = "Error marker present for: {0}")
    @ValueSource(strings = {"File not found: test.csv","Missing required column(s): location",
            "Invalid input. Enter a number between 1 and 7.","No file path entered.",
            "Error reading file: stream closed"})
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.07 - Various Error Messages Test")
    void MultiErrorTest(String errorMessage) {
        ConsoleFormatter.printError(errorMessage);
        String output = captured();
        assertAll(() -> assertTrue(output.contains("[!]")),
                () -> assertTrue(output.contains(errorMessage)));}

    @RepeatedTest(3)
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Multiple Success Test")
    void MultiMessageTest() {
        ConsoleFormatter.printSuccess("Consistent message");
        String output = captured();
        assertAll(() -> assertTrue(output.contains("[OK]")),() -> assertTrue(output.contains("Consistent message")));}


    @Tag("Thomas")
    @Tag("Core")
    @DisplayName("Title Print Test")
    @Test
    void printTitleTest() {
        String[] rawTitle = {
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

        String formattedTitle = "\u001b[36m" + String.join("\u001b[0m\n\u001b[36m", rawTitle) + "\u001b[0m";

        String expectedTitle = "\u001b[36m" + "=".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m" + "\n" +
                formattedTitle + "\n" +
                "\u001b[36m" + "=".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m" + "\n\n";


        ConsoleFormatter.printTitle();
        String output = captured();
        assertEquals(expectedTitle, output);
    }

    @Tag("Thomas")
    @Tag("Core")
    @DisplayName("Header Print Test")
    @ParameterizedTest()
    @ValueSource(strings = {"Title 1", "", "aekjfnrgveasjbfrg", "\u001b[45m"})
    void printHeaderTest(String title) {
        String expectedHeader = "\n\u001b[33m" + "=".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m\n" +
                "\u001b[33m" + " ".repeat(Math.max(0, (ConsoleFormatter.WIDTH - title.length()) / 2)) + "\u001b[1m" + title + "\u001b[0m\n" +
                "\u001b[33m" + "=".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m\n";

        ConsoleFormatter.printHeader(title);
        String output = captured();
        assertEquals(expectedHeader, output);
    }

    @Tag("Thomas")
    @Tag("Core")
    @DisplayName("Separator Print Test")
    @Test
    void printSeparatorTest() {
        String expected = "\u001b[33m" + "-".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m\n";
        ConsoleFormatter.printSeparator();
        String output = captured();
        assertEquals(expected, output);
    }

    @Tag("Thomas")
    @Tag("Core")
    @DisplayName("Return Item Print Test")
    @Test
    void printReturnItemTest() {
        String expected = "  " + "\u001b[36m" + "[0]" + "\u001b[0m" + " Return\n";
        ConsoleFormatter.printReturnItem();
        String output = captured();
        assertEquals(expected, output);
    }

    private static Stream<Arguments> ClassScheduleGenerator() {
        return Stream.of(Arguments.of(new ClassSchedule("TopicName", "In Person", "Workshop", "1", "01 Jan - 31 Dec", "Saturday", "01:00 - 03:00", "City Campus")),
                Arguments.of(new ClassSchedule("TopicName", "In Person", "Lecture", "1", "01 Jan - 31 Dec", "Friday", "12:30 - 16:00", "Tonsley - G42 Lecture Room")));
    }

    @Tag("Thomas")
    @Tag("Core")
    @DisplayName("Class Details Print Test")
    @ParameterizedTest
    @MethodSource("ClassScheduleGenerator")
    void printClassDetailsTest(ClassSchedule cs) {
        StringBuilder expected = new StringBuilder("\u001b[33m" + "-".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m\n");
        expected.append("  \u001b[1mTopic         \u001b[0m : " + cs.getTopic() + "\n");
        expected.append("  \u001b[1mAvailability  \u001b[0m : " + cs.getAvailability() + "\n");
        expected.append("  \u001b[1mClass         \u001b[0m : " + cs.getClassName() + "\n");
        expected.append("  \u001b[1mClass Instance\u001b[0m : " + cs.getClassInstance() + "\n");
        expected.append("  \u001b[1mDate          \u001b[0m : " + cs.getDate() + "\n");
        expected.append("  \u001b[1mDay           \u001b[0m : " + cs.getDay() + "\n");
        expected.append("  \u001b[1mTime          \u001b[0m : " + cs.getTime() + "\n");
        expected.append("  \u001b[1mLocation      \u001b[0m : " + cs.getLocation() + "\n");

        expected.append("\u001b[33m" + "-".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m\n");


        ConsoleFormatter.printClassDetails(cs);
        String output = captured();
        assertEquals(expected.toString(), output);
    }

    private static Stream<Arguments> TestTimetableGenerator() {
        ClassSchedule[] classList1 = {new ClassSchedule("TopicName", "In Person", "Workshop", "1", "01 Jan - 31 Dec", "Saturday", "01:00 - 03:00", "City Campus"),
                new ClassSchedule("TopicName", "In Person", "Lecture", null, "01 Jan - 31 Dec", null, "12:30 - 16:00", "Tonsley - G42 Lecture Room"),
                new ClassSchedule("TopicName", "In Person", "Lecture", "1", "01 Jan - 31 Dec", "Friday", "12:30 - 16:00", "Tonsley - G42 Lecture Room")};

        Timetable timetable1 = new Timetable("Timetable 1");
        timetable1.setScheduledClasses(Arrays.stream(classList1).toList());
        timetable1.setSemester("1");
        ArrayList<String> topicList = new ArrayList<>();
        topicList.add("TopicName");
        timetable1.setSelectedTopics(topicList);

        ArrayList<String> campusList = new ArrayList<>();
        campusList.add("Tonsley");
        timetable1.setSelectedTopics(campusList);



        ClassSchedule[] classList2 = {new ClassSchedule("TopicName", "In Person", "Workshop", "1", "01 Jan - 31 Dec", "Saturday", "01:00 - 03:00", "City Campus"),
                new ClassSchedule("Super Very Long Topic Name So Many Characters QWERTYUIOP", "In Person", "Lecture", "1", "01 Jan - 31 Dec", "Friday", "12:30 - 16:00", "Tonsley - G42 Lecture Room")};

        Timetable timetable2 = new Timetable("Timetable 2");
        timetable2.setScheduledClasses(Arrays.stream(classList2).toList());

        Timetable emptyTimetable = new Timetable("Empty Timetable");
        Timetable blankTimetable = new Timetable();

        return Stream.of(Arguments.of(timetable1),
                Arguments.of(timetable2),
                Arguments.of(emptyTimetable),
                Arguments.of(blankTimetable));
    }

    @Tag("Thomas")
    @Tag("Core")
    @DisplayName("Timetable Print Test")
    @ParameterizedTest
    @MethodSource("TestTimetableGenerator")
    void printTimetableSummaryTest(Timetable timetable) {
        String expected = "  \u001b[36m[1]\u001b[0m \u001b[1m" + timetable.getTimetableName() + "\u001b[0m  |  " + timetable.getScheduledClasses().size() + " class(es)\n";
        ConsoleFormatter.printTimetableSummary(1, timetable);

        String output = captured();
        assertEquals(expected, output);
    }

    private static String pad(String value, int length) {
        return String.format(String.format("%%-%d", length) + "s", value);
    }

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
                String content = line < cellLines.size() ? cellLines.get(line) : " ".repeat(widths[col]);
                sb.append(' ').append(content).append(" |");
            }
            rows[line] = sb.toString();
        }
        return rows;
    }

    @Tag("Thomas")
    @Tag("Core")
    @DisplayName("Timetable Details Print Test")
    @ParameterizedTest
    @MethodSource("TestTimetableGenerator")
    void printTimetableDetailsTest(Timetable timetable) {
        // Header Part
        StringBuilder expected = new StringBuilder("\u001b[33m" + "-".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m\n");
        expected.append("  \u001b[1mName       \u001b[0m : " + timetable.getTimetableName() + "\n");
        expected.append("  \u001b[1mTopics     \u001b[0m : " + (timetable.getSelectedTopics().isEmpty() ? "(none)" : String.join(", ", timetable.getSelectedTopics())) + "\n");
        expected.append("  \u001b[1mCampuses   \u001b[0m : " + (timetable.getSelectedCampuses().isEmpty() ? "(none)" : String.join(", ", timetable.getSelectedCampuses())) + "\n");
        expected.append("  \u001b[1mPreferences\u001b[0m : " + (timetable.getPreferences().isEmpty() ? "(none)" : String.join(", ", timetable.getPreferences())) + "\n");
        expected.append("  \u001b[1mClasses    \u001b[0m : " + timetable.getScheduledClasses().size() + " scheduled\n");
        expected.append("\u001b[33m" + "-".repeat(ConsoleFormatter.WIDTH) + "\u001b[0m\n");

        if (!timetable.getScheduledClasses().isEmpty()) {
            expected.append("\u001b[1m  Scheduled classes:\u001b[0m\n");

            int[] columnWidths = { 4, 32, 28, 12, 5, 9, 13, 30 }; // Assuming this won't change
            String[] columnHeaders = {" #", " Topic", " Availability", " Class", " Inst", " Day", " Time", " Location"};

            StringBuilder dividerBuilder = new StringBuilder("+");
            for (int width: columnWidths) {
                dividerBuilder.append("-".repeat(width + 2)).append("+");
            }
            dividerBuilder.append("\n");


            StringBuilder headerBuilder = new StringBuilder("|");
            for (int i = 0; i < columnHeaders.length; ++i) {
                headerBuilder.append(String.format(String.format("%%-%d", columnWidths[i] + 2) + "s", columnHeaders[i])).append("|");
            }

            expected.append(dividerBuilder);
            expected.append("\u001b[1m" + headerBuilder + "\u001b[0m\n");
            expected.append(dividerBuilder);

            for (int i = 1; i <= timetable.getScheduledClasses().size(); i++) {
                ClassSchedule class_ = timetable.getScheduledClasses().get(i - 1);
                String[] values = {
                        String.valueOf(i),
                        class_.getTopic(),
                        class_.getAvailability(),
                        class_.getClassName(),
                        class_.getClassInstance(),
                        class_.getDay(),
                        class_.getTime(),
                        class_.getLocation()
                };

                for (String line : buildWrappedRows(values, columnWidths)) {
                    expected.append(line + "\n");
                }
                expected.append(dividerBuilder);
            }
        }

        ConsoleFormatter.printTimetableDetails(timetable);

        String output = captured();
        assertEquals(expected.toString(), output);
    }




}
