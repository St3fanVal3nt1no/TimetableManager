import timetable.Main;
import timetable.ConsoleFormatter;
import timetable.TimetableSystem;
import timetable.TimetableGenerator;
import timetable.CSVHandler;
import timetable.ClassSchedule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)

public class MainTest {
    private static InputStream originalIn;
    private static PrintStream originalOut;
    private static ByteArrayOutputStream suppressedOut;

    @BeforeAll
    static void redirectStreamsBeforeMainClassIsLoaded() {
        originalIn     = System.in;
        originalOut    = System.out;
        suppressedOut  = new ByteArrayOutputStream();
        System.setIn(new ByteArrayInputStream("7\n7\n7\n".getBytes()));
        System.setOut(new PrintStream(suppressedOut));}

    @AfterAll
    static void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);}

    @BeforeEach
    void clearSuppressedOutput() {
        suppressedOut.reset();
    }

    @AfterEach
    void afterEach() {
    }


    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - ConsoleFormatter Message Marker Test")
    void MesageMarkerTest() {
        ByteArrayOutputStream buf   = new ByteArrayOutputStream();
        PrintStream           saved = System.out;
        System.setOut(new PrintStream(buf));
        try {
            ConsoleFormatter.printSuccess("ok");
            ConsoleFormatter.printError("err");
            ConsoleFormatter.printInfo("info");}
        finally {System.setOut(saved);}
        String out = buf.toString();
        assertAll(
                () -> assertTrue(out.contains("[OK]")),
                () -> assertTrue(out.contains("[!]")),
                () -> assertTrue(out.contains("[i]")));}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.09 - Invalid Import Population Test")
    void InvalidNullPopulateTest() {
        TimetableSystem sys = new TimetableSystem();
        int[] result = CSVHandler.importFromCSV("no_such_file_ever.csv", sys);
        assertAll(
                () -> assertNull(result),
                () -> assertTrue(sys.getClasses().isEmpty()));}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.09 - Missing Column Import Test MAIN")
    void MissingColumNullTest() {
        TimetableSystem sys = new TimetableSystem();
        int[] result = CSVHandler.importFromCSV(
                "src/test/resources/COMP1701 Game Design.csv", sys);
        assertNull(result);}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Null Path Error Test")
    void NullPathTest() {
        TimetableSystem sys = new TimetableSystem();
        assertThrows(Exception.class,
                () -> CSVHandler.importFromCSV(null, sys));}

    @ParameterizedTest(name = "printError(''{0}'') always contains [!] marker")
    @ValueSource(strings = {
            "File not found: missing.csv",
            "Missing required column(s): location",
            "Invalid input. Enter a number between 1 and 7.",
            "No file path entered."})
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Error Marker Test MAIN")
    void ErrorMarkerTest(String errorMessage) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream saved = System.out;
        System.setOut(new PrintStream(buf));
        try {ConsoleFormatter.printError(errorMessage);
        } finally {System.setOut(saved);}
        String output = buf.toString();
        assertAll(() -> assertTrue(output.contains("[!]")),() -> assertTrue(output.contains(errorMessage)));}

    @ParameterizedTest(name = "{0} prints marker ''{1}''")
    @CsvSource({
            "printSuccess, [OK]",
            "printError,   [!]",
            "printInfo,    [i]"})
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Output Marker Test MAIN")
    void OutputMarkerTest(String methodName, String expectedMarker) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream saved = System.out;
        System.setOut(new PrintStream(buf));
        try {switch (methodName.trim()) {
                case "printSuccess" -> ConsoleFormatter.printSuccess("test");
                case "printError"   -> ConsoleFormatter.printError("test");
                case "printInfo"    -> ConsoleFormatter.printInfo("test");}
        } finally {System.setOut(saved);}
        assertTrue(buf.toString().contains(expectedMarker.trim()));}
}