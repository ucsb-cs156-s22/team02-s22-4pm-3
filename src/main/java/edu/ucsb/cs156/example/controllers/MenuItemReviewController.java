package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;


@Api(description = "MenuItemReview")
@RequestMapping("/api/MenuItemReview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    @ApiOperation(value = "Get reviews of all Menu Items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allReviews() {
        Iterable<MenuItemReview> reviews  = menuItemReviewRepository.findAll();
        return reviews;
    }

    @ApiOperation(value = "Get a single Menu Item Review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public MenuItemReview getById(
            @ApiParam("id") @RequestParam Long id) {
        MenuItemReview reviews = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return reviews;
    }

    @ApiOperation(value = "Create a new Menu Item Review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postMenuItemReview(
        @ApiParam("itemID") @RequestParam Long itemId,
        @ApiParam("reviewerEmail") @RequestParam String reviewerEmail,
        @ApiParam("stars") @RequestParam int stars,
        @ApiParam("dateReviewed (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateReviewed") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateReviewed,
        @ApiParam("comments") @RequestParam String comments
        )
        throws JsonProcessingException{

        log.info("dateReviewed={}", dateReviewed);

        MenuItemReview review = new MenuItemReview();
        review.setItemId(itemId);
        review.setReviewerEmail(reviewerEmail);
        review.setStars(stars);
        review.setDateReviewed(dateReviewed);
        review.setComments(comments);

        MenuItemReview savedReview = menuItemReviewRepository.save(review);

        return savedReview;
    }

    @ApiOperation(value = "Delete a MenuItemReview")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteReview(
            @ApiParam("id") @RequestParam Long id) {
                MenuItemReview review = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        menuItemReviewRepository.delete(review);
        return genericMessage("MenuItemReview with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateReview(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid MenuItemReview incoming) {

            MenuItemReview review = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        review.setItemId(incoming.getItemId());
        review.setReviewerEmail(incoming.getReviewerEmail());  
        review.setStars(incoming.getStars());
        review.setDateReviewed(incoming.getDateReviewed());
        review.setComments(incoming.getComments());

        menuItemReviewRepository.save(review);

        return review;
    }
}
