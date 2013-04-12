package org.threeten.extra.chrono;

import java.io.*;

import org.threeten.bp.*;
import org.threeten.bp.chrono.*;
import org.threeten.bp.jdk8.*;
import org.threeten.bp.temporal.*;

/**
 * A date in the <a href="http://en.wikipedia.org/wiki/Discordian_calendar">Discordian Calendar</a> system.
 * <p/>
 * Since a Discordian year is the same length and uses the same leap-year pattern as an ISO year this class is simply a
 * wrapper around {@link org.threeten.bp.LocalDate}.
 * <p/>
 * Note that since <em>St. Tib's Day</em> is considered "outside" the Discordian season and week the methods
 * {@link #getDayOfWeek()}, {@link #getDayOfSeason()} and {@link #getSeason()} will return <code>-1</code> for an
 * instance of this class that represents the leap day (the 60th day of a leap year).
 * <p/>
 * Hail Eris! All hail Discordia!
 */
public final class DiscordianDate
        extends DefaultInterfaceChronoLocalDate<DiscordianDate>
        implements ChronoLocalDate<DiscordianDate>, Serializable {

    public static final int ST_TIBS_DAY = 60;

    private static final long serialVersionUID = 5856281505361396284L;

    /**
     * The underlying ISO local date.
     */
    private final LocalDate isoDate;

    /**
     * Obtains an instance of {@code DiscordianDate} from the year-of-era,
     * season-of-year and day-of-season.
     * <p/>
     * Note it is not possible to construct an instance representing the leap-day using this method as
     * the leap day falls "outside" of the Discordian season. Instead use {@link #leapDayOf(int)}.
     *
     * @param year        the year-of-era to represent.
     * @param season      the season-of-year to represent, from 1 to 5.
     * @param dayOfSeason the day-of-season to represent, from 1 to 73.
     * @return the Discordian date, never null.
     * @throws org.threeten.bp.DateTimeException
     *          if the value of any field is out of range.
     */
    public static DiscordianDate of(int year, int season, int dayOfSeason) {
        DiscordianChronology chronology = DiscordianChronology.INSTANCE;
        chronology.checkValidSeason(season);
        chronology.checkValidDayOfSeason(dayOfSeason);
        int isoYear = chronology.discordianToIsoYear(year);
        int dayOfYear = dayOfDiscordianYear(year, season, dayOfSeason);
        return new DiscordianDate(LocalDate.ofYearDay(isoYear, dayOfYear));
    }

    /**
     * Obtains an instance of {@code DiscordianDate} from the year-of-era and day-of-year.
     *
     * @param year      the year-of-era to represent.
     * @param dayOfYear the day-of-year to represent, from 1 to 365 (366 if {@code year} is a leap year).
     * @return the Discordian date, never null.
     * @throws org.threeten.bp.DateTimeException
     *          if the value of any field is out of range.
     */
    public static DiscordianDate of(int year, int dayOfYear) {
        int isoYear = DiscordianChronology.INSTANCE.discordianToIsoYear(year);
        return new DiscordianDate(LocalDate.ofYearDay(isoYear, dayOfYear));
    }

    /**
     * Obtains an instance of {@code DiscordianDate} representing the leap day in the specified year.
     * This method does <em>not</em> validate input so if {@code year} is not a leap year in the
     * Discordian calendar the instance returned will represent Chaos 60.
     *
     * @param year the year-of-era to represent.
     * @return the Discordian date, never null.
     * @throws org.threeten.bp.DateTimeException
     *          if the value of any field is out of range.
     */
    public static DiscordianDate leapDayOf(int year) {
        return of(year, ST_TIBS_DAY);
    }

    private static int dayOfDiscordianYear(int prolepticYear, int season, int dayOfSeason) {
        int dayOfYear = ((season - 1) * DiscordianChronology.DAYS_PER_SEASON) + dayOfSeason;
        if (DiscordianChronology.INSTANCE.isLeapYear(prolepticYear) && dayOfYear >= ST_TIBS_DAY) {
            dayOfYear++;
        }
        return dayOfYear;
    }

    public DiscordianDate(LocalDate isoDate) {
        this.isoDate = isoDate;
    }

    /**
     * Gets the year in the Discordian era.
     */
    public int getYear() {
        return DiscordianChronology.INSTANCE.isoToDiscordianYear(isoDate.getYear());
    }

    /**
     * Gets the season represented by this instance in the range 1 to 5. If this instance represents
     * a leap day this method will return <em>-1</em>.
     */
    public int getSeason() {
        int dayOfYear = getLeapAdjustedDayOfYear();
        if (dayOfYear == -1) {
            return -1;
        }
        return ((dayOfYear - 1) / DiscordianChronology.DAYS_PER_SEASON) + 1;
    }

    /**
     * Gets the day-of-season represented by this instance in the range 1 to 5. If this instance represents
     * a leap day this method will return <em>-1</em>.
     */
    public int getDayOfSeason() {
        int dayOfYear = getLeapAdjustedDayOfYear();
        if (dayOfYear == -1) {
            return -1;
        }
        return ((dayOfYear - 1) % DiscordianChronology.DAYS_PER_SEASON) + 1;
    }

    /**
     * Gets the day-of-week represented by this instance in the range 1 to 5. If this instance represents
     * a leap day this method will return <em>-1</em>.
     */
    public int getDayOfWeek() {
        int dayOfYear = getLeapAdjustedDayOfYear();
        if (dayOfYear == -1) {
            return -1;
        }
        return ((dayOfYear - 1) % DiscordianChronology.DAYS_PER_WEEK) + 1;
    }

    public String getSeasonName() {
        return DiscordianChronology.INSTANCE.getSeasonName(getSeason());
    }

    public String getDayName() {
        return DiscordianChronology.INSTANCE.getDayName(getDayOfWeek());
    }

    private boolean isLeapDay() {
        return isoDate.isLeapYear() && isoDate.getDayOfYear() == ST_TIBS_DAY;
    }

    /**
     * @return the day of the year adjusted to remove the leap day if this is a leap year. The value is thus convertible
     *         to season, day of week or season regardless of whether this is a leap year.
     */
    private int getLeapAdjustedDayOfYear() {
        int dayOfYear = isoDate.getDayOfYear();
        if (isoDate.isLeapYear()) {
            if (dayOfYear == ST_TIBS_DAY) {
                return -1;
            } else if (dayOfYear > ST_TIBS_DAY) {
                return dayOfYear - 1;
            }
        }
        return dayOfYear;
    }

    @Override
    public ChronoLocalDate<DiscordianDate> plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit f = (ChronoUnit) unit;
            switch (f) {
                case WEEKS:
                    return plus(Jdk8Methods.safeMultiply(amountToAdd, DiscordianChronology.DAYS_PER_WEEK), ChronoUnit.DAYS);
                case MONTHS:
                    return plus(Jdk8Methods.safeMultiply(amountToAdd, DiscordianChronology.DAYS_PER_SEASON), ChronoUnit.DAYS);
                case ERAS:
                    throw new DateTimeException("Unable to add era, Discordian calendar system only has one era");
                default:
                    return new DiscordianDate(isoDate.plus(amountToAdd, unit));
            }
        }
        return unit.addTo(this, amountToAdd);
    }

    @Override
    public Period periodUntil(ChronoLocalDate<?> chronoLocalDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long periodUntil(Temporal endDateTime, TemporalUnit unit) {
        if (!ChronoLocalDate.class.isAssignableFrom(endDateTime.getClass())) {
            throw new DateTimeException("Unable to calculate period between objects of two different types");
        }
        ChronoLocalDate<?> end = (ChronoLocalDate<?>) endDateTime;
        if (end.getChronology() != getChronology()) {
            throw new DateTimeException("Unable to calculate period between two different chronologies");
        }

        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
                case WEEKS:
                    return isoDate.periodUntil(LocalDate.from(end), ChronoUnit.DAYS) / DiscordianChronology.DAYS_PER_WEEK;
                case MONTHS:
                    return isoDate.periodUntil(LocalDate.from(end), ChronoUnit.DAYS) / DiscordianChronology.DAYS_PER_SEASON;
                case ERAS:
                    return 0;
                default:
                    return isoDate.periodUntil(LocalDate.from(end), unit);
            }
        }
        return unit.between(this, endDateTime);
    }

    @Override
    public Chronology getChronology() {
        return DiscordianChronology.INSTANCE;
    }

    @Override
    public int lengthOfMonth() {
        return DiscordianChronology.DAYS_PER_SEASON;
    }

    @Override
    public ChronoLocalDate<DiscordianDate> with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            if (getLong(f) == newValue) {
                return this;
            }
            switch (f) {
                case DAY_OF_MONTH:
                    if (isLeapDay()) {
                        return DiscordianDate.of(getYear(), 1, (int) newValue);
                    } else {
                        return DiscordianDate.of(getYear(), getSeason(), (int) newValue);
                    }
            }
            // TODO: review other fields, such as WEEK_OF_YEAR
            return with(isoDate.with(field, newValue));
        }
        return field.adjustInto(this, newValue);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            return getChronology().range((ChronoField) field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case YEAR_OF_ERA:
                    return getYear();
                case ERA:
                    return 1L;
                case MONTH_OF_YEAR:
                    return getSeason();
                case DAY_OF_MONTH:
                    return getDayOfSeason();
                case DAY_OF_WEEK:
                    return getDayOfWeek();
            }
            // TODO: review other fields
            return isoDate.getLong(field);
        }
        return field.getFrom(this);
    }

    /**
     * Returns a String representation of this instance.
     * <p/>
     * The format used is compatible with the default format output by
     * <a href="http://linuxcommand.org/man_pages/ddate1.html">the UNIX <em>ddate</em> command</a>.
     * <p/>
     * Because of the leap year handling in the Discordian calendar string representations of dates
     * are not lexically sortable. The leap day has no season, day-of-season or day-of-week so it does
     * not make sense to attempt a numeric representation of those values.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (isLeapDay()) {
            buffer.append("St. Tib's Day! ");
        } else {
            buffer.append(getDayName())
                    .append(", ")
                    .append(getSeasonName())
                    .append(" ")
                    .append(getDayOfSeason())
                    .append(", ");
        }
        buffer.append(getYear()).append(" ").append(getEra());
        return buffer.toString();
    }

}
