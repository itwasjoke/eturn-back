package com.eturn.eturn.repository;


import com.eturn.eturn.entity.Feedback;
import com.eturn.eturn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByUser(User user);

    @Query("SELECT ROUND(AVG(f.evaluation), 2) FROM Feedback f")
    Double findAverageEvaluation();
}
