import timetable.CSVHandler;
import timetable.TimetableSystem;
import timetable.ClassSchedule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)

public class CSVHandlerTest {

    private static final String test_data = "src/test/resources/";
    private TimetableSystem tsystem;

    @BeforeEach
    void setUp() {
        tsystem = new TimetableSystem();
    }

    @AfterEach
    void tearDown() {
        tsystem.clearClasses();
        tsystem = null;
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - Valid CSV Import Test")
    void ImportValidTest() {
        int[] result = CSVHandler.importFromCSV(
                test_data + "COMP1702 Fundamentals of Software Engineering.csv", tsystem);
                assertNotNull(result);}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - Class Count Update Test")
    void ClassCountUpdateTest() {
        int[] result = CSVHandler.importFromCSV(
                test_data + "COMP1702 Fundamentals of Software Engineering.csv", tsystem);
        assumeTrue(result != null);
        assertAll(() -> assertTrue(result[0] > 0),() -> assertEquals(0, result[1]));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - Import Class Population Test")
    void ClassImportTest() {
        CSVHandler.importFromCSV(
                test_data + "COMP1702 Fundamentals of Software Engineering.csv", tsystem);
        assertFalse(tsystem.getClasses().isEmpty());}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - Import Field Population Test")
    void FieldImportTest() {
        CSVHandler.importFromCSV(
                test_data + "COMP1702 Fundamentals of Software Engineering.csv", tsystem);
        assumeTrue(!tsystem.getClasses().isEmpty());
        ClassSchedule first = tsystem.getClasses().get(0);
        assertAll(
                () -> assertNotNull(first.getTopic()),
                () -> assertFalse(first.getTopic().isEmpty()),
                () -> assertNotNull(first.getAvailability()),
                () -> assertNotNull(first.getClassName()),
                () -> assertNotNull(first.getClassInstance()),
                () -> assertNotNull(first.getDate()),
                () -> assertNotNull(first.getDay()),
                () -> assertNotNull(first.getTime()),
                () -> assertNotNull(first.getLocation()));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("CSV Import No-Null Test")
    void CSVImportTest() {
        int[] result = CSVHandler.importFromCSV(
                test_data + "ENGR1762 Networks and Cybersecurity.csv", tsystem);
        assertNotNull(result);}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.01 - Multiple CSV Import Test")
    void MultImportValidTest() {
        CSVHandler.importFromCSV(
                test_data + "COMP1702 Fundamentals of Software Engineering.csv", tsystem);
        int afterFirst = tsystem.getClasses().size();
        CSVHandler.importFromCSV(
                test_data + "ENGR1762 Networks and Cybersecurity.csv", tsystem);
        assertTrue(tsystem.getClasses().size() > afterFirst);}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.04 - Missing Column Import Test")
    void MissingColumnTest() {
        int[] result = CSVHandler.importFromCSV(
                test_data + "COMP1701 Game Design.csv", tsystem);
        assertNull(result);}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.04 - Missing Column Import No-Populate Test")
    void NoPopultionTest() {
        CSVHandler.importFromCSV(test_data + "COMP1701 Game Design.csv", tsystem);
        assertTrue(tsystem.getClasses().isEmpty());}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.04 - Invalid File Path Test")
    void InvalidImportPathTest() {
        int[] result = CSVHandler.importFromCSV(test_data + "DoesNotExist.csv", tsystem);
        assertNull(result);}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.04 - Empty CSV Import Test")
    void EmptyCSVImportTest() {
        int[] result = CSVHandler.importFromCSV(test_data + "Empty.csv", tsystem);
        assertNull(result);}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.04 - Header Only CSV Import Test")
    void HeaderOnlyImportTest() {
        int[] result = CSVHandler.importFromCSV(test_data + "NoClasses.csv", tsystem);
        assertAll(
                () -> assertNotNull(result),() -> assertEquals(0, result[0]),() -> assertEquals(0, result[1]));}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.04 - Invalid File-Type Test")
    void InvalidImportTest() {
        int[] result = CSVHandler.importFromCSV(test_data + "Not a CSV file.txt", tsystem);
        assertNull(result);}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.04 - Invalid CSV Format Import Test")
    void InvalidDataFormatTest() {
        int[] result = CSVHandler.importFromCSV(test_data + "Test.csv", tsystem);
        assertNull(result);}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("2.09 - Unsafe Import Rejection Test")
    void UnsafeImportTest() {
        int[] result = CSVHandler.importFromCSV("../../sensitive.csv", tsystem);
        assertNull(result);}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("2.09 - Null Filename Exception Test")
    void NullFileExceptionTest() {
        assertThrows(Exception.class, () -> CSVHandler.importFromCSV(null, tsystem));}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("2.09 - Empty String Null Test")
    void EmptyStrNullTest() {
        int[] result = CSVHandler.importFromCSV("", tsystem);
        assertNull(result);}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - Imported String Split Test")
    void ImportStringTest() {
        String[] fields = CSVHandler.parseCSVLine("will,it,blend");
        assertAll(
                () -> assertEquals(3, fields.length),
                () -> assertEquals("will", fields[0]),
                () -> assertEquals("it",  fields[1]),
                () -> assertEquals("blend", fields[2]));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - CSV Field Comma Handling Test")
    void CommaHandingTest() {
        String[] fields = CSVHandler.parseCSVLine("\"bright, is testing\",second,third");
        assertAll(
                () -> assertEquals(3, fields.length),
                () -> assertEquals("bright, is testing", fields[0]),
                () -> assertEquals("second", fields[1]));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - CSV Field Quote Handling Test")
    void QuoteHandlingTest() {
        String[] fields = CSVHandler.parseCSVLine("\"say \"\"hi\"\"\",next");
        assertAll(
                () -> assertEquals(2, fields.length),
                () -> assertEquals("say \"hi\"", fields[0]));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01- Blank Field Handling Test")
    void BlankHandlingTest() {
        String[] fields = CSVHandler.parseCSVLine("");
        assertEquals(1, fields.length);}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - Multi-Comma Handling Test")
    void MultiCommaTest() {
        String[] fields = CSVHandler.parseCSVLine(",,,");
        assertEquals(4, fields.length);}

    @ParameterizedTest(name = "''{0}'' → {1} fields")
    @CsvSource(delimiter = '|', value = {
            "a,b,c                  | 3",
            "x,y                    | 2",
            "single                 | 1",
            "one,two,three,four,five| 5"})
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("CSV Data Unusual Entries Test")
    void UnusualEntriesTest(String csvLine, int expectedCount) {
        assertEquals(expectedCount, CSVHandler.parseCSVLine(csvLine.trim()).length);}

    @ParameterizedTest(name = "COMP1702 row {index}: topic={0}, day={5}")
    @CsvFileSource(resources = "/COMP1702 Fundamentals of Software Engineering.csv", numLinesToSkip = 1)
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - CSV Row Validation Test")
    void ImportRowValidTest(
            String topic, String availability, String className,
            String classInstance, String date, String day, String time, String location) {
        assertAll(
                () -> assertEquals("COMP1702 Fundamentals of Software Engineering", topic),
                () -> assertNotNull(day),
                () -> assertFalse(day.isEmpty()));}

    @RepeatedTest(3)
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.01 - Multi-Import Test")
    void MultiImportTest() {
        TimetableSystem freshSystem = new TimetableSystem();
        int[] result = CSVHandler.importFromCSV(
                test_data + "ENGR1762 Networks and Cybersecurity.csv", freshSystem);
        assumingThat(result != null,
                () -> assertTrue(result[0] + result[1] > 0));}
}