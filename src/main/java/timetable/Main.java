package timetable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final TimetableSystem   system    = new TimetableSystem();
    private static final TimetableGenerator generator = new TimetableGenerator();
    private static final Scanner           scanner   = new Scanner(System.in);

    // -----------------------------------------------------------------------
    // Last-used generate-timetable settings (spec: "remembered when returning")
    // -----------------------------------------------------------------------
    private static String       lastSemester           = "S2";
    private static List<String> lastSelectedTopics     = new ArrayList<>();
    private static List<String> lastSelectedCampuses   = new ArrayList<>();
    private static boolean      lastAllowLectureOverlap = false;
    private static List<String> lastPreferences        = new ArrayList<>();
    private static int          autoNameCounter        = 1;   // for auto-generated names

    // ===================================================================
    // Entry point
    // ===================================================================

    public static void main(String[] args) {
        ConsoleFormatter.printTitle();
        mainMenu();
        scanner.close();
    }

    // ===================================================================
    // Main Menu
    // ===================================================================

    private static void mainMenu() {
        while (true) {
            ConsoleFormatter.printHeader("MAIN MENU");
            ConsoleFormatter.printMenuItem(1, "Import CSV");
            ConsoleFormatter.printMenuItem(2, "Browse Classes");
            ConsoleFormatter.printMenuItem(3, "View Classes");
            ConsoleFormatter.printMenuItem(4, "Search Classes");
            ConsoleFormatter.printMenuItem(5, "Generate Timetable");
            ConsoleFormatter.printMenuItem(6, "Browse Timetables");
            ConsoleFormatter.printMenuItem(7, "Exit");
            ConsoleFormatter.printSeparator();

            int choice = readChoice(1, 7);
            switch (choice) {
                case 1: importCSV();         break;
                case 2: browseClasses();     break;
                case 3: viewClasses();       break;
                case 4: searchClasses();     break;
                case 5: generateTimetable(); break;
                case 6: browseTimetables();  break;
                case 7:
                    System.out.println();
                    ConsoleFormatter.printInfo("Goodbye!");
                    return;
            }
        }
    }

    // ===================================================================
    // Option 1 — Import CSV
    // ===================================================================

    private static void importCSV() {
        ConsoleFormatter.printHeader("IMPORT CSV");
        System.out.print(ConsoleFormatter.CYAN + "  File path: " + ConsoleFormatter.RESET);
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            ConsoleFormatter.printError("No file path entered.");
            return;
        }
        int[] result = CSVHandler.importFromCSV(path, system);
        if (result != null) {
            ConsoleFormatter.printSuccess("Imported:  " + result[0] + " new class(es).");
            ConsoleFormatter.printSuccess("Updated:   " + result[1] + " existing class(es).");
            ConsoleFormatter.printInfo("Total in system: " + system.getClasses().size() + " class(es).");
        }
    }

    // ===================================================================
    // Option 2 — Browse Classes
    //   Shows combined view (no Date column) — user picks one to open details.
    // ===================================================================

    private static void browseClasses() {
        ConsoleFormatter.printHeader("BROWSE CLASSES");
        List<ClassSchedule> classes = system.getClasses();
        if (classes.isEmpty()) {
            ConsoleFormatter.printInfo("No classes loaded. Use Import CSV first.");
            return;
        }
        ConsoleFormatter.printInfo(classes.size() + " class(es) loaded.");
        selectAndOpenClass(classes);
    }

    // ===================================================================
    // Option 3 — View Classes  (spec: shows ALL fields including Date)
    // ===================================================================

    private static void viewClasses() {
        ConsoleFormatter.printHeader("VIEW CLASSES");
        List<ClassSchedule> classes = system.getClasses();
        if (classes.isEmpty()) {
            ConsoleFormatter.printInfo("No classes loaded. Use Import CSV first.");
            return;
        }
        ConsoleFormatter.printInfo(classes.size() + " class(es) — full detail view.");
        ConsoleFormatter.printSeparator();
        for (int i = 0; i < classes.size(); i++) {
            System.out.println(ConsoleFormatter.CYAN + "  [" + (i + 1) + "]" + ConsoleFormatter.RESET);
            ConsoleFormatter.printClassDetails(classes.get(i));
        }
        ConsoleFormatter.printSeparator();
        System.out.print(ConsoleFormatter.CYAN + "  Select a class to edit/delete (0 to return): "
                + ConsoleFormatter.RESET);
        int choice = readChoice(0, classes.size());
        if (choice == 0) return;
        classDetails(classes.get(choice - 1), classes);
    }

    // ===================================================================
    // Option 4 — Search Classes
    // ===================================================================

    private static void searchClasses() {
        ConsoleFormatter.printHeader("SEARCH CLASSES");
        if (system.getClasses().isEmpty()) {
            ConsoleFormatter.printInfo("No classes loaded. Use Import CSV first.");
            return;
        }

        ConsoleFormatter.printInfo("Enter search criteria. Leave blank to skip a field.");
        ConsoleFormatter.printSeparator();

        String topic         = prompt("  Topic          : ");
        String availability  = prompt("  Availability   : ");
        String className     = prompt("  Class          : ");
        String classInstance = prompt("  Class Instance : ");
        String date          = prompt("  Date           : ");
        String day           = prompt("  Day            : ");
        String time          = prompt("  Time           : ");
        String location      = prompt("  Location       : ");

        ConsoleFormatter.printSeparator();

        List<ClassSchedule> results = new ArrayList<>();
        for (ClassSchedule cs : system.getClasses()) {
            if (matches(cs.getTopic(),         topic)
                    && matches(cs.getAvailability(),  availability)
                    && matches(cs.getClassName(),     className)
                    && matches(cs.getClassInstance(), classInstance)
                    && matches(cs.getDate(),          date)
                    && matches(cs.getDay(),           day)
                    && matches(cs.getTime(),          time)
                    && matches(cs.getLocation(),      location)) {
                results.add(cs);
            }
        }

        if (results.isEmpty()) {
            ConsoleFormatter.printInfo("No classes matched the search criteria.");
            return;
        }

        ConsoleFormatter.printSuccess("Found " + results.size() + " result(s).");
        selectAndOpenClass(results);
    }

    private static boolean matches(String value, String criterion) {
        if (criterion.isEmpty()) return true;
        return value.toLowerCase().contains(criterion.toLowerCase());
    }

    private static String prompt(String label) {
        System.out.print(ConsoleFormatter.CYAN + label + ConsoleFormatter.RESET);
        return scanner.nextLine().trim();
    }

    // ===================================================================
    // Class list → selection → Class Details
    // ===================================================================

    private static void selectAndOpenClass(List<ClassSchedule> classes) {
        while (true) {
            ConsoleFormatter.printClassTable(classes);
            ConsoleFormatter.printReturnItem();
            ConsoleFormatter.printSeparator();

            int choice = readChoice(0, classes.size());
            if (choice == 0) return;

            boolean deleted = classDetails(classes.get(choice - 1), classes);
            if (deleted && classes.isEmpty()) return;
        }
    }

    // ===================================================================
    // Class Details screen
    // ===================================================================

    private static boolean classDetails(ClassSchedule cs, List<ClassSchedule> classList) {
        while (true) {
            ConsoleFormatter.printHeader("CLASS DETAILS");
            ConsoleFormatter.printClassDetails(cs);
            ConsoleFormatter.printMenuItem(1, "Edit Class");
            ConsoleFormatter.printMenuItem(2, "Delete Class");
            ConsoleFormatter.printMenuItem(3, "Return");
            ConsoleFormatter.printSeparator();

            int choice = readChoice(1, 3);
            switch (choice) {
                case 1:
                    editClass(cs);
                    break;
                case 2:
                    if (deleteClass(cs, classList)) return true;
                    break;
                case 3:
                    return false;
            }
        }
    }

    private static void editClass(ClassSchedule cs) {
        ConsoleFormatter.printHeader("EDIT CLASS");
        ConsoleFormatter.printInfo("Enter a new value, or leave blank to keep the current value.");
        ConsoleFormatter.printSeparator();

        String newTopic         = editField("Topic         ", cs.getTopic());
        String newAvailability  = editField("Availability  ", cs.getAvailability());
        String newClassName     = editField("Class         ", cs.getClassName());
        String newClassInstance = editField("Class Instance", cs.getClassInstance());
        String newDate          = editField("Date          ", cs.getDate());
        String newDay           = editField("Day           ", cs.getDay());
        String newTime          = editField("Time          ", cs.getTime());
        String newLocation      = editField("Location      ", cs.getLocation());

        boolean changed = !newTopic.equals(cs.getTopic())
                || !newAvailability.equals(cs.getAvailability())
                || !newClassName.equals(cs.getClassName())
                || !newClassInstance.equals(cs.getClassInstance())
                || !newDate.equals(cs.getDate())
                || !newDay.equals(cs.getDay())
                || !newTime.equals(cs.getTime())
                || !newLocation.equals(cs.getLocation());

        if (!changed) {
            ConsoleFormatter.printInfo("No changes made.");
            return;
        }

        ConsoleFormatter.printSeparator();
        System.out.println(ConsoleFormatter.BOLD + "  Proposed changes:" + ConsoleFormatter.RESET);
        printChange("Topic         ", cs.getTopic(),         newTopic);
        printChange("Availability  ", cs.getAvailability(),  newAvailability);
        printChange("Class         ", cs.getClassName(),     newClassName);
        printChange("Class Instance", cs.getClassInstance(), newClassInstance);
        printChange("Date          ", cs.getDate(),          newDate);
        printChange("Day           ", cs.getDay(),           newDay);
        printChange("Time          ", cs.getTime(),          newTime);
        printChange("Location      ", cs.getLocation(),      newLocation);
        ConsoleFormatter.printSeparator();

        System.out.print(ConsoleFormatter.CYAN + "  Save changes? (Y/N): " + ConsoleFormatter.RESET);
        String answer = scanner.nextLine().trim();

        if (answer.equalsIgnoreCase("Y")) {
            cs.setTopic(newTopic);
            cs.setAvailability(newAvailability);
            cs.setClassName(newClassName);
            cs.setClassInstance(newClassInstance);
            cs.setDate(newDate);
            cs.setDay(newDay);
            cs.setTime(newTime);
            cs.setLocation(newLocation);
            ConsoleFormatter.printSuccess("Class updated.");
        } else {
            ConsoleFormatter.printInfo("Edit cancelled. No changes saved.");
        }
    }

    private static void printChange(String label, String oldVal, String newVal) {
        if (!oldVal.equals(newVal)) {
            System.out.println("  " + label + " : " + oldVal + " -> " + newVal);
        }
    }

    private static String editField(String label, String current) {
        System.out.print(ConsoleFormatter.CYAN + "  " + label + " [" + current + "]: "
                + ConsoleFormatter.RESET);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? current : input;
    }

    private static boolean deleteClass(ClassSchedule cs, List<ClassSchedule> classList) {
        ConsoleFormatter.printHeader("DELETE CLASS");
        ConsoleFormatter.printClassDetails(cs);

        System.out.print(ConsoleFormatter.CYAN + "  Delete this class? (Y/N): " + ConsoleFormatter.RESET);
        String answer = scanner.nextLine().trim();

        if (answer.equalsIgnoreCase("Y")) {
            system.getClasses().remove(cs);
            classList.remove(cs);
            ConsoleFormatter.printSuccess("Class deleted.");
            return true;
        }

        ConsoleFormatter.printInfo("Deletion cancelled.");
        return false;
    }

    // ===================================================================
    // Option 5 — Generate Timetable
    // ===================================================================

    private static void generateTimetable() {
        ConsoleFormatter.printHeader("GENERATE TIMETABLE");

        if (system.getClasses().isEmpty()) {
            ConsoleFormatter.printInfo("No classes loaded. Use Import CSV first.");
            return;
        }

        // --- Name (blank = auto-generate; must be unique) ---
        System.out.print(ConsoleFormatter.CYAN
                + "  Timetable name (blank = auto-generate): " + ConsoleFormatter.RESET);
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            // Auto-generate a unique name
            do {
                name = "Timetable " + autoNameCounter++;
            } while (system.getTimetableByName(name) != null);
            ConsoleFormatter.printInfo("Auto-generated name: \"" + name + "\"");
        } else if (system.getTimetableByName(name) != null) {
            ConsoleFormatter.printError("A timetable named \"" + name + "\" already exists.");
            return;
        }

        Timetable timetable = new Timetable(name);

        // --- Semester (show last-used as default) ---
        ConsoleFormatter.printSeparator();
        System.out.println(ConsoleFormatter.BOLD + "  Select semester:"
                + ConsoleFormatter.RESET + "  (last: " + lastSemester + ")");
        ConsoleFormatter.printMenuItem(1, "S1");
        ConsoleFormatter.printMenuItem(2, "S2");
        ConsoleFormatter.printMenuItem(3, "Both");
        int semChoice = readChoice(1, 3);
        String semester = semChoice == 1 ? "S1" : semChoice == 2 ? "S2" : "Both";
        timetable.setSemester(semester);
        lastSemester = semester;

        // --- Topics (at least one required) ---
        List<String> availableTopics = system.getAvailableTopics();
        ConsoleFormatter.printSeparator();
        System.out.println(ConsoleFormatter.BOLD + "  Available topics:" + ConsoleFormatter.RESET);
        for (int i = 0; i < availableTopics.size(); i++) {
            String marker = lastSelectedTopics.contains(availableTopics.get(i)) ? " *" : "";
            ConsoleFormatter.printMenuItem(i + 1, availableTopics.get(i) + marker);
        }
        if (!lastSelectedTopics.isEmpty()) {
            ConsoleFormatter.printInfo("* = previously selected");
        }
        List<String> selectedTopics = selectItems(availableTopics, true);
        if (selectedTopics == null) return;
        timetable.setSelectedTopics(selectedTopics);
        lastSelectedTopics = new ArrayList<>(selectedTopics);

        // --- Campuses ---
        List<String> availableCampuses = system.getAvailableCampuses();
        ConsoleFormatter.printSeparator();
        System.out.println(ConsoleFormatter.BOLD + "  Available campuses:" + ConsoleFormatter.RESET);
        for (int i = 0; i < availableCampuses.size(); i++) {
            String marker = lastSelectedCampuses.contains(availableCampuses.get(i)) ? " *" : "";
            ConsoleFormatter.printMenuItem(i + 1, availableCampuses.get(i) + marker);
        }
        ConsoleFormatter.printInfo("Enter numbers (e.g. 1,2), or blank / \"all\" for all campuses.");
        ConsoleFormatter.printInfo("Note: City campus classes cannot be mixed with Bedford Park/Tonsley for the same topic.");
        List<String> selectedCampuses = selectItems(availableCampuses, false);
        if (selectedCampuses == null || selectedCampuses.isEmpty()) {
            timetable.getSelectedCampuses().add("all");
        } else {
            timetable.setSelectedCampuses(selectedCampuses);
        }
        lastSelectedCampuses = new ArrayList<>(timetable.getSelectedCampuses());

        // --- Lecture overlap ---
        ConsoleFormatter.printSeparator();
        String lastOverlapHint = lastAllowLectureOverlap ? "Y" : "N";
        System.out.print(ConsoleFormatter.CYAN
                + "  Allow overlapping lecture times? (Y/N) [last: " + lastOverlapHint + "]: "
                + ConsoleFormatter.RESET);
        String overlapAnswer = scanner.nextLine().trim();
        boolean allowOverlap = overlapAnswer.isEmpty()
                ? lastAllowLectureOverlap
                : overlapAnswer.equalsIgnoreCase("Y");
        timetable.setAllowLectureOverlap(allowOverlap);
        lastAllowLectureOverlap = allowOverlap;

        // --- Preferences (numbered list, user picks in priority order) ---
        ConsoleFormatter.printSeparator();
        System.out.println(ConsoleFormatter.BOLD + "  Preference options:" + ConsoleFormatter.RESET);
        String[] prefOptions = {
                "Bedford Park",
                "Tonsley",
                "Flinders City Campus",
                "All at same campus",
                "Mornings",
                "Afternoons",
                "Mondays",
                "Tuesdays",
                "Wednesdays",
                "Thursdays",
                "Fridays",
                "Evenly spread",
                "Compact"
        };
        for (int i = 0; i < prefOptions.length; i++) {
            final String prefOpt = prefOptions[i];
            boolean wasLast = lastPreferences.stream()
                    .anyMatch(p -> p.equalsIgnoreCase(prefOpt));
            ConsoleFormatter.printMenuItem(i + 1, prefOptions[i] + (wasLast ? " *" : ""));
        }
        if (!lastPreferences.isEmpty()) {
            ConsoleFormatter.printInfo("* = previously selected  |  Last order: "
                    + String.join(", ", lastPreferences));
        }
        System.out.println(ConsoleFormatter.CYAN
                + "  Enter numbers in priority order (e.g. 5,4,1), or blank for none:"
                + ConsoleFormatter.RESET);
        System.out.print(ConsoleFormatter.MAGENTA + "  > " + ConsoleFormatter.RESET);
        String prefsInput = scanner.nextLine().trim();

        List<String> selectedPrefs = new ArrayList<>();
        if (!prefsInput.isEmpty()) {
            for (String part : prefsInput.split(",")) {
                try {
                    int idx = Integer.parseInt(part.trim()) - 1;
                    if (idx >= 0 && idx < prefOptions.length) {
                        selectedPrefs.add(prefOptions[idx]);
                    }
                } catch (NumberFormatException e) {
                    // skip non-numeric tokens
                }
            }
        }
        timetable.setPreferences(selectedPrefs);
        lastPreferences = new ArrayList<>(selectedPrefs);

        // --- Filter candidates ---
        List<ClassSchedule> candidates = generator.getCandidates(timetable, system);
        if (candidates.isEmpty()) {
            ConsoleFormatter.printInfo("No classes match the selected parameters.");
            return;
        }

        // --- Interactive class selection loop ---
        List<ClassSchedule> scheduled = new ArrayList<>();

        while (true) {
            List<ClassSchedule> eligible = new ArrayList<>();
            for (ClassSchedule cs : candidates) {
                if (!scheduled.contains(cs)
                        && !generator.conflictsWithScheduled(cs, scheduled, timetable.isAllowLectureOverlap())) {
                    eligible.add(cs);
                }
            }

            if (eligible.isEmpty()) {
                ConsoleFormatter.printInfo("No more classes can be added without conflicts.");
                break;
            }

            ConsoleFormatter.printSeparator();
            ConsoleFormatter.printInfo("Select a class to add, or 0 to finish.");
            ConsoleFormatter.printClassTable(eligible);
            ConsoleFormatter.printReturnItem();
            ConsoleFormatter.printSeparator();

            int pick = readChoice(0, eligible.size());
            if (pick == 0) break;

            ClassSchedule picked = eligible.get(pick - 1);
            scheduled.add(picked);
            ConsoleFormatter.printSuccess("Added: " + picked.getTopic()
                    + " — " + picked.getClassName()
                    + " (" + picked.getDay() + " " + picked.getTime() + ")");
        }

        timetable.setScheduledClasses(scheduled);
        system.addTimetable(timetable);
        int count = timetable.getScheduledClasses().size();

        ConsoleFormatter.printSeparator();
        ConsoleFormatter.printSuccess(
                "Timetable \"" + name + "\" saved with " + count + " class(es).");

        if (count > 0) {
            ConsoleFormatter.printClassTable(timetable.getScheduledClasses());
        } else {
            ConsoleFormatter.printInfo("No classes were selected.");
        }
    }

    private static List<String> selectItems(List<String> options, boolean requireAtLeastOne) {
        ConsoleFormatter.printInfo("Enter numbers (e.g. 1,2,3)"
                + (requireAtLeastOne ? " — at least one required." : ", or blank for all."));
        System.out.print(ConsoleFormatter.CYAN + "  > " + ConsoleFormatter.RESET);
        String input = scanner.nextLine().trim();

        if (input.isEmpty() || input.equalsIgnoreCase("all")) {
            if (requireAtLeastOne && options.isEmpty()) {
                ConsoleFormatter.printError("No options available.");
                return null;
            }
            return new ArrayList<>(options);
        }

        List<String> selected = new ArrayList<>();
        for (String part : input.split(",")) {
            try {
                int idx = Integer.parseInt(part.trim()) - 1;
                if (idx >= 0 && idx < options.size()) {
                    String item = options.get(idx);
                    if (!selected.contains(item)) selected.add(item);
                }
            } catch (NumberFormatException e) {
                // skip non-numeric tokens
            }
        }

        if (requireAtLeastOne && selected.isEmpty()) {
            ConsoleFormatter.printError("At least one selection is required.");
            return null;
        }
        return selected;
    }

    // ===================================================================
    // Option 6 — Browse Timetables
    // ===================================================================

    private static void browseTimetables() {
        ConsoleFormatter.printHeader("BROWSE TIMETABLES");

        while (true) {
            List<Timetable> timetables = system.getTimetables();
            if (timetables.isEmpty()) {
                ConsoleFormatter.printInfo("No timetables yet. Use Generate Timetable first.");
                return;
            }

            ConsoleFormatter.printSeparator();
            for (int i = 0; i < timetables.size(); i++) {
                ConsoleFormatter.printTimetableSummary(i + 1, timetables.get(i));
            }
            ConsoleFormatter.printReturnItem();
            ConsoleFormatter.printSeparator();

            int choice = readChoice(0, timetables.size());
            if (choice == 0) return;

            timetableDetails(timetables.get(choice - 1));
        }
    }

    // ===================================================================
    // Timetable Details screen
    // ===================================================================

    private static void timetableDetails(Timetable t) {
        while (true) {
            ConsoleFormatter.printHeader("TIMETABLE DETAILS");
            ConsoleFormatter.printTimetableDetails(t);

            List<String> warnings = generator.detectConflicts(
                    t.getScheduledClasses(), t.isAllowLectureOverlap());
            if (!warnings.isEmpty()) {
                System.out.println();
                System.out.println(ConsoleFormatter.RED + ConsoleFormatter.BOLD
                        + "  Warnings:" + ConsoleFormatter.RESET);
                for (String w : warnings) {
                    ConsoleFormatter.printError(w);
                }
                System.out.println();
            }

            ConsoleFormatter.printMenuItem(1, "Edit Timetable");
            ConsoleFormatter.printMenuItem(2, "Delete Timetable");
            ConsoleFormatter.printMenuItem(3, "Export Timetable");
            ConsoleFormatter.printMenuItem(4, "Return");
            ConsoleFormatter.printSeparator();

            int choice = readChoice(1, 4);
            switch (choice) {
                case 1: editTimetable(t);           break;
                case 2: if (deleteTimetable(t)) return; break;
                case 3: exportTimetable(t);         break;
                case 4: return;
            }
        }
    }

    private static void editTimetable(Timetable t) {
        ConsoleFormatter.printHeader("EDIT TIMETABLE");

        List<ClassSchedule> scheduled = t.getScheduledClasses();
        if (scheduled.isEmpty()) {
            ConsoleFormatter.printInfo("No classes in this timetable to edit.");
            return;
        }

        ConsoleFormatter.printMenuItem(1, "Swap a class");
        ConsoleFormatter.printMenuItem(2, "Remove a class");
        ConsoleFormatter.printReturnItem();
        ConsoleFormatter.printSeparator();
        int action = readChoice(0, 2);
        if (action == 0) return;
        if (action == 2) { removeTimetableClass(t); return; }

        // --- Select the class to replace ---
        ConsoleFormatter.printInfo("Select a class to replace, or 0 to cancel.");
        ConsoleFormatter.printClassTable(scheduled);
        ConsoleFormatter.printReturnItem();
        ConsoleFormatter.printSeparator();
        int pick = readChoice(0, scheduled.size());
        if (pick == 0) return;
        ClassSchedule toReplace = scheduled.get(pick - 1);

        // --- Find alternatives: same Topic AND same Class name, different instance ---
        List<ClassSchedule> alternatives = new ArrayList<>();
        for (ClassSchedule cs : system.getClasses()) {
            if (cs != toReplace
                    && cs.getTopic().equalsIgnoreCase(toReplace.getTopic())
                    && cs.getClassName().equalsIgnoreCase(toReplace.getClassName())) {
                alternatives.add(cs);
            }
        }

        if (alternatives.isEmpty()) {
            ConsoleFormatter.printInfo("No alternatives found for: "
                    + toReplace.getTopic() + " " + toReplace.getClassName());
            return;
        }

        // --- Select the replacement ---
        ConsoleFormatter.printSeparator();
        ConsoleFormatter.printInfo("Replacing: " + toReplace.getTopic()
                + " " + toReplace.getClassName()
                + "  (" + toReplace.getDay() + " " + toReplace.getTime() + ")");
        ConsoleFormatter.printInfo("Choose a replacement, or 0 to cancel.");
        ConsoleFormatter.printClassTable(alternatives);
        ConsoleFormatter.printReturnItem();
        ConsoleFormatter.printSeparator();
        int replacePick = readChoice(0, alternatives.size());
        if (replacePick == 0) return;
        ClassSchedule replacement = alternatives.get(replacePick - 1);

        // --- Conflict check against the rest of the timetable ---
        List<ClassSchedule> others = new ArrayList<>(scheduled);
        others.remove(toReplace);
        boolean hasConflict = generator.conflictsWithScheduled(
                replacement, others, t.isAllowLectureOverlap());

        // --- Summary ---
        ConsoleFormatter.printSeparator();
        System.out.println(ConsoleFormatter.BOLD + "  Replace:" + ConsoleFormatter.RESET);
        System.out.println("    " + toReplace.getTopic() + " " + toReplace.getClassName()
                + "  —  " + toReplace.getDay() + " " + toReplace.getTime());
        System.out.println(ConsoleFormatter.BOLD + "  With:" + ConsoleFormatter.RESET);
        System.out.println("    " + replacement.getTopic() + " " + replacement.getClassName()
                + "  —  " + replacement.getDay() + " " + replacement.getTime());

        if (hasConflict) {
            ConsoleFormatter.printSeparator();
            ConsoleFormatter.printError(
                    "Warning: the replacement conflicts with another class in this timetable.");
        }

        // --- Confirm (required by spec) ---
        ConsoleFormatter.printSeparator();
        String confirmPrompt = hasConflict ? "  Save anyway? (Y/N): " : "  Save replacement? (Y/N): ";
        System.out.print(ConsoleFormatter.CYAN + confirmPrompt + ConsoleFormatter.RESET);
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("Y")) {
            scheduled.set(scheduled.indexOf(toReplace), replacement);
            ConsoleFormatter.printSuccess("Class replaced.");
        } else {
            ConsoleFormatter.printInfo("Edit cancelled.");
        }
    }

    private static void removeTimetableClass(Timetable t) {
        ConsoleFormatter.printHeader("REMOVE CLASS FROM TIMETABLE");

        List<ClassSchedule> scheduled = t.getScheduledClasses();

        ConsoleFormatter.printInfo("Select a class to remove, or 0 to cancel.");
        ConsoleFormatter.printClassTable(scheduled);
        ConsoleFormatter.printReturnItem();
        ConsoleFormatter.printSeparator();
        int pick = readChoice(0, scheduled.size());
        if (pick == 0) return;

        ClassSchedule toRemove = scheduled.get(pick - 1);

        ConsoleFormatter.printSeparator();
        System.out.println(ConsoleFormatter.BOLD + "  Remove:" + ConsoleFormatter.RESET);
        System.out.println("    " + toRemove.getTopic() + " " + toRemove.getClassName()
                + "  —  " + toRemove.getDay() + " " + toRemove.getTime());
        ConsoleFormatter.printSeparator();
        System.out.print(ConsoleFormatter.CYAN + "  Confirm removal? (Y/N): " + ConsoleFormatter.RESET);
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("Y")) {
            scheduled.remove(toRemove);
            ConsoleFormatter.printSuccess("Class removed from timetable.");
        } else {
            ConsoleFormatter.printInfo("Removal cancelled.");
        }
    }

    private static boolean deleteTimetable(Timetable t) {
        ConsoleFormatter.printHeader("DELETE TIMETABLE");
        ConsoleFormatter.printInfo("\"" + t.getTimetableName() + "\""
                + "  (" + t.getScheduledClasses().size() + " class(es))");
        System.out.print(ConsoleFormatter.CYAN
                + "  Delete this timetable? (Y/N): " + ConsoleFormatter.RESET);
        String answer = scanner.nextLine().trim();
        if (answer.equalsIgnoreCase("Y")) {
            system.removeTimetable(t);
            ConsoleFormatter.printSuccess("Timetable deleted.");
            return true;
        }
        ConsoleFormatter.printInfo("Deletion cancelled.");
        return false;
    }

    private static void exportTimetable(Timetable t) {
        ConsoleFormatter.printHeader("EXPORT TIMETABLE");

        String defaultName = t.getTimetableName().replaceAll("\\s+", "_") + ".csv";
        System.out.print(ConsoleFormatter.CYAN
                + "  Output filename (blank = " + defaultName + "): " + ConsoleFormatter.RESET);
        String filename = scanner.nextLine().trim();
        if (filename.isEmpty()) filename = defaultName;

        ConsoleFormatter.printInfo("Exporting " + t.getScheduledClasses().size()
                + " class(es) to: " + filename);

        if (CSVHandler.saveTimetableToCSV(filename, t)) {
            ConsoleFormatter.printSuccess("Timetable exported successfully to " + filename);
        } else {
            ConsoleFormatter.printError("Failed to write file: " + filename);
        }
    }

    // ===================================================================
    // Shared helpers
    // ===================================================================

    private static int readChoice(int min, int max) {
        while (true) {
            System.out.print(ConsoleFormatter.MAGENTA + "  > " + ConsoleFormatter.RESET);
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                // fall through to error
            }
            ConsoleFormatter.printError(
                    "Invalid input. Enter a number between " + min + " and " + max + ".");
        }
    }

    private static List<String> splitAndTrim(String input) {
        List<String> result = new ArrayList<>();
        for (String part : input.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
