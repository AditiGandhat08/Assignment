package Assignment;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;



public class EmployeeTimeAnalysisCSV {
	    public static void main(String[] args) {
	        String filePath = "C:\\Users\\Aditi Gandhat\\Downloads\\Assignment\\Assignment_Timecard.xlsx - Sheet1.csv";     
	  
	                try {
	                    List<Employee> employees = readEmployeesFromFile(filePath);

	                    // Analysis
	                    findEmployeesWorkedConsecutiveDays(employees, 7);
	                    findEmployeesShortBreaks(employees, 1, 10);
	                    findEmployeesLongShifts(employees, 14);

	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }

	            private static List<Employee> readEmployeesFromFile(String filePath) throws IOException, CsvValidationException {
	                List<Employee> employees = new ArrayList<>();
	                CSVReader csvReader = null;

	                try {
	                    csvReader = new CSVReader(new FileReader(filePath));

	                    String[] header = csvReader.readNext();

	                    String[] line;
	                    while ((line = csvReader.readNext()) != null) {

	                        String positionID = getValueAtIndex(line, 0);
	                        String positionStatus = getValueAtIndex(line, 1);
	                        String time = getValueAtIndex(line, 2);
	                        String timeOut = getValueAtIndex(line, 3);

	                        double timecardHours = parseTimeToHours(getValueAtIndex(line, 4));

	                        String payCycleStartDateString = getValueAtIndex(line, 5);
	                        String payCycleEndDateString = getValueAtIndex(line, 6);
	                        String employeeName = getValueAtIndex(line, 7);
	                        String fileNumber = getValueAtIndex(line, 8);

	                        Date payCycleStartDate = parseDate(payCycleStartDateString);
	                        Date payCycleEndDate = parseDate(payCycleEndDateString);

	                        employees.add(new Employee(positionID, positionStatus, time, timeOut, timecardHours,
	                                payCycleStartDate, payCycleEndDate, employeeName, fileNumber));
	                    }
	                } finally {
	                    if (csvReader != null) {
	                        csvReader.close();
	                    }
	                }

	                return employees;
	            }

	            private static void findEmployeesWorkedConsecutiveDays(List<Employee> employees, int consecutiveDays) {
	                Set<String> processedEmployees = new HashSet<>(); // To track processed employees

	                for (Employee employee : employees) {
	                    if (!processedEmployees.contains(employee.employeeName)) {
	                        List<Date> workDays = getWorkDays(employee.payCycleStartDate, employee.payCycleEndDate);

	                        if (hasConsecutiveDays(workDays, consecutiveDays)) {
	                            System.out.println("Employee " + employee.employeeName +
	                                    " worked for " + consecutiveDays + " consecutive days.");
	                        }

	                        processedEmployees.add(employee.employeeName);
	                    }
	                }
	            }

	            private static void findEmployeesShortBreaks(List<Employee> employees, int minBreak, int maxBreak) {
	                for (Employee employee : employees) {
	                    Set<Double> breaks = getBreaks(employee.timeOut, employee.time);

	                    for (Double breakDuration : breaks) {
	                        if (breakDuration > minBreak && breakDuration < maxBreak) {
	                            System.out.println("Employee " + employee.employeeName +
	                                    " had a break between shifts of " + breakDuration + " hours.");
	                        }
	                    }
	                }
	            }

	            private static void findEmployeesLongShifts(List<Employee> employees, int maxShiftHours) {
	                for (Employee employee : employees) {
	                    if (employee.timecardHours > maxShiftHours) {
	                        System.out.println("Employee " + employee.employeeName +
	                                " worked for more than " + maxShiftHours + " hours in a single shift.");
	                    }
	                }
	            }

	            private static List<Date> getWorkDays(Date startDate, Date endDate) {
	                List<Date> workDays = new ArrayList<>();

	                if (startDate != null && endDate != null) {
	                  // For simplicity, assuming all days between startDate and endDate are workdays
	                    long startTime = startDate.getTime();
	                    long endTime = endDate.getTime();
	                    long currentTime = startTime;

	                    while (currentTime <= endTime) {
	                        workDays.add(new Date(currentTime));
	                        currentTime += 24 * 60 * 60 * 1000; // Adding one day in milliseconds
	                    }
	                }

	                return workDays;
	            }

	            private static boolean hasConsecutiveDays(List<Date> dates, int consecutiveDays) {
	                for (int i = 0; i < dates.size() - consecutiveDays + 1; i++) {
	                    boolean consecutive = true;
	                    for (int j = 0; j < consecutiveDays - 1; j++) {
	                        long currentDay = dates.get(i + j).getTime();
	                        long nextDay = dates.get(i + j + 1).getTime();

	                        
	                        if (currentDay + 24 * 60 * 60 * 1000 != nextDay) {
	                            consecutive = false;
	                            break;
	                        }
	                    }
	                    if (consecutive) {
	                        return true;
	                    }
	                }
	                return false;
	            }

	            private static Set<Double> getBreaks(String timeOut, String timeIn) {
	                Set<Double> breaks = new HashSet<>();

	                if (!timeOut.isEmpty() && !timeIn.isEmpty()) {
	                    double timeOutHours = parseTimeToHours(timeOut);
	                    double timeInHours = parseTimeToHours(timeIn);

	                    double breakDuration = timeInHours - timeOutHours;
	                    if (breakDuration > 0) {
	                        breaks.add(breakDuration);
	                    }
	                }

	                return breaks;
	            }

	            private static Date parseDate(String dateString) {
	                if (dateString.isEmpty()) {
	                    return null;  // Return null for an empty date string
	                }

	                try {
	                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	                    TemporalAccessor temporalAccessor = formatter.parse(dateString);

	                    int year = temporalAccessor.get(ChronoField.YEAR);
	                    int month = temporalAccessor.get(ChronoField.MONTH_OF_YEAR);
	                    int day = temporalAccessor.get(ChronoField.DAY_OF_MONTH);

	                    return new Date(year - 1900, month - 1, day);
	                } catch (DateTimeParseException e) {
	                    e.printStackTrace();
	                    return null;
	                }
	            }

	            private static double parseTimeToHours(String time) {
	                if (time.isEmpty()) {
	                    return 0.0;
	                }

	                List<String> patterns = Arrays.asList(
	                        "hh:mm a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a", "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "MM/dd/yyyy hh:mm:ss a", "MM/dd/yyyy h:mm:ss a", "MM/dd/yyyy hh:mm a", "MM/dd/yyyy h:mm a",
	                        "hh:mm a", "h:mm a", "HH:mm", "H:mm", "HH:mm:ss", "H:mm:ss"
	                );

	                for (String pattern : patterns) {
	                    try {
	                        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
	                                .parseCaseInsensitive()
	                                .appendPattern(pattern)
	                                .toFormatter();

	                        TemporalAccessor temporalAccessor = formatter.parse(time);

	                        // Extracting hour and minute components
	                        int hours = temporalAccessor.get(ChronoField.HOUR_OF_DAY);
	                        int minutes = temporalAccessor.get(ChronoField.MINUTE_OF_HOUR);

	                        return hours + (minutes / 60.0);
	                    } catch (DateTimeParseException ignored) {
	                        // Ignore and try the next pattern
	                    }
	                }

	                // Print a warning if none of the patterns match
	                System.err.println("Failed to parse time: " + time + ". No matching pattern found.");
	                return 0.0;
	            }

	            private static String getValueAtIndex(String[] array, int index) {
	                return (array != null && index >= 0 && index < array.length) ? array[index] : "";
	            }

	            private static class Employee {
	                private String positionID;
	                private String positionStatus;
	                private String time;
	                private String timeOut;
	                private double timecardHours;
	                private Date payCycleStartDate;
	                private Date payCycleEndDate;
	                private String employeeName;
	                private String fileNumber;

	                public Employee(String positionID, String positionStatus, String time, String timeOut, double timecardHours,
	                                Date payCycleStartDate, Date payCycleEndDate, String employeeName, String fileNumber) {
	                    this.positionID = positionID;
	                    this.positionStatus = positionStatus;
	                    this.time = time;
	                    this.timeOut = timeOut;
	                    this.timecardHours = timecardHours;
	                    this.payCycleStartDate = payCycleStartDate;
	                    this.payCycleEndDate = payCycleEndDate;
	                    this.employeeName = employeeName;
	                    this.fileNumber = fileNumber;
	                }
	            }
	        }

