package edu.ucsb.cs156.example.entities;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "menuitemreview")
public class MenuItemReview {
    @Id
    private Long itemId;
    private String reviewerEmail;
    private int stars;
    private LocalDateTime dateReviewed;
    private String comments;
    
}
