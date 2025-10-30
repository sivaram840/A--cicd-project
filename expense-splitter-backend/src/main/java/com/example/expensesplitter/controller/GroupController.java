package com.example.expensesplitter.controller;

import com.example.expensesplitter.dto.BalanceDto;
import com.example.expensesplitter.dto.GroupDetailsDto;
import com.example.expensesplitter.dto.GroupDto;
import com.example.expensesplitter.dto.MembershipDto;
import com.example.expensesplitter.entity.Group;
import com.example.expensesplitter.entity.Membership;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.repository.GroupRepository;
import com.example.expensesplitter.repository.UserRepository;
import com.example.expensesplitter.security.CurrentUser;
import com.example.expensesplitter.service.ExpenseService;
import com.example.expensesplitter.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    // include ExpenseService in constructor injection
    public GroupController(GroupService groupService,
                           GroupRepository groupRepository,
                           UserRepository userRepository,
                           ExpenseService expenseService) {
        this.groupService = groupService;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.expenseService = expenseService;
    }

    /**
     * Create a new group. Owner is taken from the authenticated user (@CurrentUser).
     * Body: { "name": "My group name" }
     */
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody GroupDto dto, @CurrentUser User user) {
        try {
            Group created = groupService.createGroup(dto.getName(), user);
            return ResponseEntity.ok(new GroupDto(created.getId(), created.getName(), created.getOwner().getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * List groups the current user belongs to.
     */
    @GetMapping
    public ResponseEntity<?> listGroups(@CurrentUser User user) {
        List<Group> groups = groupService.listGroupsForUser(user);
        List<GroupDto> result = groups.stream()
                .map(g -> new GroupDto(g.getId(), g.getName(), g.getOwner().getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Get group details (members etc). Only members can access.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupDetails(@PathVariable("id") Long id, @CurrentUser User user) {
        try {
            GroupDetailsDto details = groupService.getGroupDetails(id, user);
            return ResponseEntity.ok(details);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }

    /**
     * Add a user to a group by email.
     * Body: { "email": "...", "role": "MEMBER" }
     * Only group owner can add members (simple rule for MVP).
     */
    public static class AddMemberRequest {
        public String email;
        public String role;
        // getters/setters omitted for brevity (Jackson can bind public fields)
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable("id") Long id,
                                       @RequestBody AddMemberRequest req,
                                       @CurrentUser User requestingUser) {
        try {
            Group group = groupRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

            // Only owner can add members for MVP
            if (!group.getOwner().getId().equals(requestingUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group owner can add members");
            }

            // find user to add
            User userToAdd = userRepository.findByEmail(req.email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to add not found"));

            Membership saved = groupService.addMemberToGroup(group, userToAdd, req.role);

            MembershipDto dto = new MembershipDto(saved.getId(), saved.getUser().getId(),
                    // ensure MembershipDto uses getName() field (or userName -> adapt accordingly)
                    saved.getUser().getName(), saved.getRole(), saved.getJoinedAt());

            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }

    /**
     * GET /api/groups/{id}/balances
     * Compute and return net balances for each member of the group.
     */
    @GetMapping("/{id}/balances")
    public ResponseEntity<?> getGroupBalances(@PathVariable("id") Long id, @CurrentUser User requester) {
        try {
            // delegate to ExpenseService (perform access checks there)
            List<BalanceDto> balances = expenseService.computeBalances(id, requester);
            return ResponseEntity.ok(balances);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
