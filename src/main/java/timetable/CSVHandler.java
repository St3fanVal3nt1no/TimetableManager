package timetable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVHandler {

    // -----------------------------------------------------------------------
    // Required column names (matched case-insensitively)
    // "Class" maps to className internally.
    // -----------------------------------------------------------------------

    private static final String COL_TOPIC     = "topic";
    private static final String COL_AVAIL     = "availability";
    private static final String COL_CLASS     = "class";
    private static final String COL_INSTANCE  = "class instance";
    private static final String COL_DATE      = "date";
    private static final String COL_DAY       = "day";
    private static final String COL_TIME      = "time";
    private static final String COL_LOCATION  = "location";

    private static final String[] REQUIRED_COLUMNS = {
            COL_TOPIC, COL_AVAIL, COL_CLASS, COL_INSTANCE,
            COL_DATE, COL_DAY, COL_TIME, COL_LOCATION
    };

    // Header written when exporting
    private static final String EXPORT_HEADER =
            "Topic,Availability,Class,Class instance,Date,Day,Time,Location";

    // -----------------------------------------------------------------------
    // Import
    // Returns int[] { newCount, updatedCount }, or null on a fatal error.
    // -----------------------------------------------------------------------

    public static int[] importFromCSV(String filename, TimetableSystem system) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

            // --- Read header row ---
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                System.out.println("  File is empty: " + filename);
                return null;
            }

            // Strip UTF-8 BOM if present (silently added by Excel and many Windows editors)
            if (headerLine.startsWith("")) {
                headerLine = headerLine.substring(1);
            }

            // Build a map: lowercase column name → column index
            Map<String, Integer> colIndex = buildColumnIndex(parseCSVLine(headerLine));

            // --- Validate required columns ---
            List<String> missing = findMissingColumns(colIndex);
            if (!missing.isEmpty()) {
                System.out.println("  Missing required column(s): " + String.join(", ", missing));
                return null;
            }

            // Resolve the indexes we need once, up front
            int iTopic    = colIndex.get(COL_TOPIC);
            int iAvail    = colIndex.get(COL_AVAIL);
            int iClass    = colIndex.get(COL_CLASS);
            int iInstance = colIndex.get(COL_INSTANCE);
            int iDate     = colIndex.get(COL_DATE);
            int iDay      = colIndex.get(COL_DAY);
            int iTime     = colIndex.get(COL_TIME);
            int iLocation = colIndex.get(COL_LOCATION);

            // --- Parse data rows ---
            int newCount     = 0;
            int updatedCount = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;   // ignore blank lines

                String[] fields = parseCSVLine(line);

                String topic         = getField(fields, iTopic);
                String availability  = getField(fields, iAvail);
                String className     = getField(fields, iClass);
                String classInstance = getField(fields, iInstance);
                String date          = getField(fields, iDate);
                String day           = getField(fields, iDay);
                String time          = getField(fields, iTime);
                String location      = getField(fields, iLocation);

                // Check for a duplicate record
                ClassSchedule existing = findDuplicate(
                        system, topic, availability, className, classInstance, date, day
                );

                if (existing != null) {
                    // Duplicate found — update mutable fields only
                    existing.setTime(time);
                    existing.setLocation(location);
                    updatedCount++;
                } else {
                    system.addClass(new ClassSchedule(
                            topic, availability, className, classInstance,
                            date, day, time, location
                    ));
                    newCount++;
                }
            }

            return new int[]{newCount, updatedCount};

        } catch (FileNotFoundException e) {
            System.out.println("  File not found: " + filename);
            return null;
        } catch (IOException e) {
            System.out.println("  Error reading file: " + e.getMessage());
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Export helpers
    // -----------------------------------------------------------------------

    public static void saveClassesToCSV(String filename, List<ClassSchedule> classes) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(EXPORT_HEADER);
            for (ClassSchedule cs : classes) {
                writer.println(toCSVLine(cs));
            }
            System.out.println("  Saved " + classes.size() + " class(es) to " + filename);
        } catch (IOException e) {
            System.out.println("  Error writing file: " + e.getMessage());
        }
    }

    // Returns true on success, false on failure.
    public static boolean saveTimetableToCSV(String filename, Timetable timetable) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("# Timetable: " + timetable.getTimetableName());
            writer.println("# Semester: "   + timetable.getSemester());
            writer.println("# Topics: "     + String.join("; ", timetable.getSelectedTopics()));
            writer.println("# Campuses: "   + String.join("; ", timetable.getSelectedCampuses()));
            writer.println("# Preferences: "+ String.join("; ", timetable.getPreferences()));
            writer.println(EXPORT_HEADER);
            for (ClassSchedule cs : timetable.getScheduledClasses()) {
                writer.println(toCSVLine(cs));
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // CSV parsing  — quote-aware, no split(",")
    // Handles: commas inside quoted fields, "" as an escaped quote.
    // -----------------------------------------------------------------------

    public static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped double-quote inside a quoted field ("" → ")
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());  // last field

        return fields.toArray(new String[0]);
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    // Builds a lowercase-keyed column name → index map from the header fields.
    private static Map<String, Integer> buildColumnIndex(String[] headerFields) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerFields.length; i++) {
            map.put(headerFields[i].trim().toLowerCase(), i);
        }
        return map;
    }

    // Returns the names of any required columns absent from the index map.
    private static List<String> findMissingColumns(Map<String, Integer> colIndex) {
        List<String> missing = new ArrayList<>();
        for (String required : REQUIRED_COLUMNS) {
            if (!colIndex.containsKey(required)) {
                missing.add(required);
            }
        }
        return missing;
    }

    // Safely retrieves a trimmed field value, returning "" for out-of-range indexes.
    private static String getField(String[] fields, int index) {
        if (index < 0 || index >= fields.length) return "";
        return fields[index].trim();
    }

    // Looks for an existing record that shares all six identity fields.
    private static ClassSchedule findDuplicate(TimetableSystem system,
                                               String topic, String availability,
                                               String className, String classInstance,
                                               String date, String day) {
        for (ClassSchedule cs : system.getClasses()) {
            if (cs.getTopic().equals(topic)
                    && cs.getAvailability().equals(availability)
                    && cs.getClassName().equals(className)
                    && cs.getClassInstance().equals(classInstance)
                    && cs.getDate().equals(date)
                    && cs.getDay().equals(day)) {
                return cs;
            }
        }
        return null;
    }

    // Formats a ClassSchedule as a single CSV row.
    private static String toCSVLine(ClassSchedule cs) {
        return escapeCSV(cs.getTopic())         + "," +
                escapeCSV(cs.getAvailability())  + "," +
                escapeCSV(cs.getClassName())     + "," +
                escapeCSV(cs.getClassInstance()) + "," +
                escapeCSV(cs.getDate())          + "," +
                escapeCSV(cs.getDay())           + "," +
                escapeCSV(cs.getTime())          + "," +
                escapeCSV(cs.getLocation());
    }

    // Wraps a field in double-quotes if it contains a comma, quote, or newline.
   public static String escapeCSV(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}

