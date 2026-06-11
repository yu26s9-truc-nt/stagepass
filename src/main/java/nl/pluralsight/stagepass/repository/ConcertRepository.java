package nl.pluralsight.stagepass.repository;

import nl.pluralsight.stagepass.model.Concert;
import nl.pluralsight.stagepass.dto.ConcertSummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long> {
    List<Concert> findByArtistId(Long artistId);
    List<Concert> findByDateAfterOrderByDateAsc(LocalDate date);

    @Query("""
        SELECT new nl.pluralsight.stagepass.dto.ConcertSummary(
            id, 
            title, 
            totalSeats, 
            (totalSeats - availableSeats), 
            availableSeats, 
            (ticketPrice * (totalSeats - availableSeats))
        ) 
        FROM Concert 
        WHERE id = :id
    """)
    Optional<ConcertSummary> getSummary(@Param("id") Long id);
}
