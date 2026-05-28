package timetable;

public class ClassSchedule {

    private String topic;
    private String availability;
    private String className;
    private String classInstance;
    private String date;
    private String day;
    private String time;
    private String location;

    public ClassSchedule(String topic, String availability, String className,
                         String classInstance, String date, String day,
                         String time, String location) {
        this.topic = topic;
        this.availability = availability;
        this.className = className;
        this.classInstance = classInstance;
        this.date = date;
        this.day = day;
        this.time = time;
        this.location = location;
    }

    public String getTopic() {
        return topic;
    }

    public String getAvailability() {
        return availability;
    }

    public String getClassName() {
        return className;
    }

    public String getClassInstance() {
        return classInstance;
    }

    public String getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setClassInstance(String classInstance) {
        this.classInstance = classInstance;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Topic: " + topic + "\n" +
                "Availability: " + availability + "\n" +
                "Class: " + className + "\n" +
                "Class Instance: " + classInstance + "\n" +
                "Date: " + date + "\n" +
                "Day: " + day + "\n" +
                "Time: " + time + "\n" +
                "Location: " + location;
    }
}

