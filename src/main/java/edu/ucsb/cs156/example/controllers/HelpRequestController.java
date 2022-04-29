package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

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

import javax.validation.Valid;
import java.time.LocalDateTime;

@Api(description = "HelpRequest")
@RequestMapping("/api/helprequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController{
    @Autowired
    HelpRequestRepository helpRequestRepository;

    @ApiOperation(value = "List all help request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<HelpRequest> allCommonss() {
        Iterable<HelpRequest> requests = helpRequestRepository.findAll();
        return requests;
    }

    @ApiOperation(value = "Get a single request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public HelpRequest getById(
            @ApiParam("code") @RequestParam String code) {
                HelpRequest requests = helpRequestRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, code));

        return requests;
    }

    @ApiOperation(value = "Create a new request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public HelpRequest postCommons(
        @ApiParam("code") @RequestParam String code, //do i need this

        @ApiParam("requesterEmail") @RequestParam String requesterEmail,
        @ApiParam("teamId") @RequestParam String teamId,
        @ApiParam("tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
        @ApiParam("requestTime") @RequestParam LocalDateTime requestTime,
        @ApiParam("explanation") @RequestParam String explanation,
        @ApiParam("solved") @RequestParam boolean solved
        )
        {

            HelpRequest requests = new HelpRequest();
            requests.setCode(code);
            requests.setRequesterEmail(requesterEmail);
            requests.setTeamId(teamId);
            requests.setTableOrBreakoutRoom(tableOrBreakoutRoom);
            requests.setRequestTime(requestTime);
            requests.setExplanation(explanation);
            requests.setSolved(solved);

            HelpRequest savedRequests = helpRequestRepository.save(requests);

        return savedRequests;
    }

    @ApiOperation(value = "Delete a HelpRequest")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteCommons(
            @ApiParam("code") @RequestParam String code) {
                HelpRequest requests = helpRequestRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, code));

                helpRequestRepository.delete(requests);
        return genericMessage("HelpRequest with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public HelpRequest updateCommons(
            @ApiParam("code") @RequestParam String code,
            @RequestBody @Valid HelpRequest incoming) {

                HelpRequest requests = helpRequestRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, code));


                requests.setCode(incoming.getCode());  
                requests.setRequesterEmail(incoming.getRequesterEmail());
                requests.setTeamId(incoming.getTeamId());
                requests.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
                requests.setRequestTime(incoming.getRequestTime());
                requests.setExplanation(incoming.getExplanation());
                requests.setSolved(incoming.getSolved());

                helpRequestRepository.save(requests);

        return requests;
    }
}
