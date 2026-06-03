import timetable.ConsoleFormatter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

//    @Tag("Thomas")
//    @Tag("Core")
//    @DisplayName("Title Print Test")
//    @Test
}
