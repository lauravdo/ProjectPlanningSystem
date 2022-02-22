package utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class Calendar {
    public static final int WORKING_HOURS_PER_DAY = 8;

    /**
     * calculate the number of working days (mondays - fridays)
     * between firstDay and the lastDay, both inclusive
     * @param firstDay
     * @param lastDay
     * @return
     */
    public static int getNumWorkingDays(LocalDate firstDay, LocalDate lastDay) {
        if (firstDay.isAfter(lastDay)) {
            return 0;
        }

        // count the calendar days between the dates, inclusive
        int calendarDays =
            lastDay.getDayOfYear() - firstDay.getDayOfYear() + 1;
        for (int year = firstDay.getYear(); year < lastDay.getYear(); year++) {
            calendarDays += LocalDate.ofYearDay(year,1).lengthOfYear();
        }

        // calculate the working days (removing weekend days)
        int workingDays = 5 * (calendarDays/7);
        int extraDays = calendarDays % 7;

        // add the workdays of the partial week
        int firstWeekDay = firstDay.getDayOfWeek().getValue();
        for (int dayNr = 0; dayNr < extraDays; dayNr++) {
            // exclude the saturday and sunday
            if (firstWeekDay+dayNr != DayOfWeek.SATURDAY.getValue() &&
                    firstWeekDay+dayNr != DayOfWeek.SUNDAY.getValue() &&
                    firstWeekDay+dayNr != DayOfWeek.SATURDAY.getValue()+7 &&
                    firstWeekDay+dayNr != DayOfWeek.SUNDAY.getValue()+7) {
                workingDays++;
            }
        }

        return workingDays;
    }

    /**
     * Calculate the set of dates representing all working days (mondays - fridays)
     * between firstDay and lastDay, both inclusive
     * @param firstDay
     * @param lastDay
     * @return
     */
    public static Set<LocalDate> getWorkingDays(LocalDate firstDay, LocalDate lastDay) {

        if (firstDay.isAfter(lastDay)) {
            return Set.of();
        }
        return firstDay.datesUntil(lastDay.plusDays(1))
                .filter(d ->
                        d.getDayOfWeek().compareTo(DayOfWeek.MONDAY) >= 0 &&
                        d.getDayOfWeek().compareTo(DayOfWeek.FRIDAY) <= 0)
                .collect(Collectors.toSet());
    }

    public static LocalDate firstWorkingDayFrom(LocalDate date) {
        if (date.getDayOfWeek().compareTo(DayOfWeek.SATURDAY) == 0) return date.plusDays(2);
        else if (date.getDayOfWeek().compareTo(DayOfWeek.SUNDAY) == 0) return date.plusDays(1);
        return date;
    }

    public static LocalDate lastWorkingDayUntil(LocalDate date) {
        if (date.getDayOfWeek().compareTo(DayOfWeek.SATURDAY) == 0) return date.minusDays(1);
        else if (date.getDayOfWeek().compareTo(DayOfWeek.SUNDAY) == 0) return date.minusDays(2);
        return date;
    }
}
