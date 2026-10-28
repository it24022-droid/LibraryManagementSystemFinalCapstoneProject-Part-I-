package library;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Calculates the late-return fine (Taka per overdue day). */
public class Fine {
    public static final double RATE_PER_DAY = 5.0;

    private final long daysLate;
    private final double amount;

    public Fine(LocalDate dueDate, LocalDate returnDate) {
        this.daysLate = Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
        this.amount = daysLate * RATE_PER_DAY;
    }

    public long getDaysLate() { return daysLate; }
    public double getAmount() { return amount; }
}
