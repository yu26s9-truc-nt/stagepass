package nl.pluralsight.stagepass.service;

import nl.pluralsight.stagepass.exception.InsufficientSeatsException;
import nl.pluralsight.stagepass.model.Booking;
import nl.pluralsight.stagepass.model.Concert;
import nl.pluralsight.stagepass.repository.BookingRepository;
import nl.pluralsight.stagepass.repository.ConcertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ConcertRepository concertRepository;

    public BookingService(BookingRepository bookingRepository, ConcertRepository concertRepository) {
        this.bookingRepository = bookingRepository;
        this.concertRepository = concertRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookingsByConcert(Long concertId) {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        Concert concert = concertRepository.findById(booking.getConcert().getId())
                .orElseThrow(() -> new RuntimeException("Concert not found"));

        int requestedTickets = booking.getNumberOfTickets();
        if (concert.getAvailableSeats() < requestedTickets) {
            throw new IllegalArgumentException("Not enough available seats for this concert. Available: "
                    + concert.getAvailableSeats());
        }
        concert.setAvailableSeats(concert.getAvailableSeats() - requestedTickets);

        // Compute total price
        booking.setTotalPrice(BigDecimal.ZERO);

        // Set booking date and concert reference
        booking.setBookingDate(LocalDate.now());
        booking.setConcert(concert);

        return bookingRepository.save(booking);
    }

    public boolean cancelBooking(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return false;
        }

        Booking booking = bookingOpt.get();
        Concert concert = booking.getConcert();
        if (concert == null) {
            throw new RuntimeException("Concert relation not found for this booking");
        }

        concert.setAvailableSeats(Math.min(concert.getAvailableSeats() + booking.getNumberOfTickets(), concert.getTotalSeats()));

        bookingRepository.deleteById(id);
        return true;
    }

}
