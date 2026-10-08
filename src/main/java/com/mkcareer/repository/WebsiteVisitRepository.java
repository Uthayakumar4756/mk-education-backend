package com.mkcareer.repository;

import com.mkcareer.entity.WebsiteVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WebsiteVisitRepository extends JpaRepository<WebsiteVisit, Long> {

    @Query("SELECT COUNT(v) FROM WebsiteVisit v")
    long countTotalVisits();

    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM WebsiteVisit v")
    long countUniqueVisitors();

}