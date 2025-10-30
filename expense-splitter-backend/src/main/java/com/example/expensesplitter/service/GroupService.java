package com.example.expensesplitter.service;

import com.example.expensesplitter.dto.GroupDetailsDto;
import com.example.expensesplitter.dto.MembershipDto;
import com.example.expensesplitter.entity.Group;
import com.example.expensesplitter.entity.Membership;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.repository.GroupRepository;
import com.example.expensesplitter.repository.MembershipRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;

    public GroupService(GroupRepository groupRepository,
                        MembershipRepository membershipRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
    }

    /**
     * Create a new group with the provided name, owned by the given user.
     * Also creates an OWNER membership row for the owner.
     */
    @Transactional
    public Group createGroup(String name, User owner) {
        Group g = new Group();
        g.setName(name);
        g.setOwner(owner);
        Group saved = groupRepository.save(g);

        // create membership for owner with role OWNER
        Membership m = new Membership();
        m.setGroup(saved);
        m.setUser(owner);
        m.setRole("OWNER");
        membershipRepository.save(m);

        // keep in-memory consistency
        saved.getMemberships().add(m);
        return saved;
    }

    /**
     * Return all groups a given user belongs to.
     * We fetch memberships by user then map to groups.
     */
    @Transactional(readOnly = true)
    public List<Group> listGroupsForUser(User user) {
        List<Membership> memberships = membershipRepository.findByUser(user);
        return memberships.stream()
                .map(Membership::getGroup)
                .collect(Collectors.toList());
    }

    /**
     * Add a user to a group as MEMBER (simple invite/accept omitted).
     * Throws IllegalArgumentException if user already a member.
     */
    @Transactional
    public Membership addMemberToGroup(Group group, User user, String role) {
        membershipRepository.findByGroupAndUser(group, user).ifPresent(m -> {
            throw new IllegalArgumentException("User already a member of the group");
        });

        Membership m = new Membership();
        m.setGroup(group);
        m.setUser(user);
        m.setRole(role == null ? "MEMBER" : role);
        Membership saved = membershipRepository.save(m);

        // keep in-memory relationship consistent
        group.getMemberships().add(saved);
        return saved;
    }

    /**
     * Get group details (including members). Only allow access if the requesting user is a member.
     */
    @Transactional(readOnly = true)
    public GroupDetailsDto getGroupDetails(Long groupId, User requestingUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // verify membership
        membershipRepository.findByGroupAndUser(group, requestingUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this group"));

        List<MembershipDto> members = group.getMemberships().stream()
                .map((Membership m) -> new MembershipDto(
                        m.getId(),
                        m.getUser().getId(),
                        m.getUser().getName(),
                        m.getRole(),
                        m.getJoinedAt()
                ))
                .collect(Collectors.toList());

        return new GroupDetailsDto(group.getId(), group.getName(), group.getOwner().getId(), group.getCreatedAt(), members);
    }
}

