package com.skillswap.service;

import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.SkillExchangeRequestDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.RequestStatus;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillExchangeRequest;
import com.skillswap.model.SkillType;
import com.skillswap.model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExchangeServiceTest {

    private StudentDAO studentDAO;
    private SkillDAO skillDAO;
    private SkillExchangeRequestDAO requestDAO;
    private ExchangeService exchangeService;

    private Student studentA;
    private Student studentB;
    private Skill studentATeachJava;
    private Skill studentALearnUI;
    private Skill studentBTeachUI;
    private Skill studentBLearnJava;

    private final List<Integer> createdRequestIds = new ArrayList<>();
    private final List<Integer> createdSkillIds = new ArrayList<>();
    private final List<Integer> createdStudentIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws SQLException {
        studentDAO = new StudentDAO();
        skillDAO = new SkillDAO();
        requestDAO = new SkillExchangeRequestDAO();
        exchangeService = new ExchangeService(requestDAO, studentDAO, skillDAO);

        // Create Student A
        studentA = studentDAO.insertStudent(new Student(
                "Alice Exchanger",
                "alice.exch." + System.currentTimeMillis() + "@test.edu",
                "111"
        ));
        createdStudentIds.add(studentA.getStudentId());

        // Create Student B
        studentB = studentDAO.insertStudent(new Student(
                "Bob Exchanger",
                "bob.exch." + System.currentTimeMillis() + "@test.edu",
                "222"
        ));
        createdStudentIds.add(studentB.getStudentId());

        // Student A: TEACH Java, LEARN UI/UX
        studentATeachJava = skillDAO.insertSkill(new Skill(
                studentA.getStudentId(), "Java", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Core Java"
        ));
        createdSkillIds.add(studentATeachJava.getSkillId());

        studentALearnUI = skillDAO.insertSkill(new Skill(
                studentA.getStudentId(), "UI/UX", SkillType.LEARN, ProficiencyLevel.BEGINNER, "Figma"
        ));
        createdSkillIds.add(studentALearnUI.getSkillId());

        // Student B: TEACH UI/UX, LEARN Java
        studentBTeachUI = skillDAO.insertSkill(new Skill(
                studentB.getStudentId(), "UI/UX", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Design systems"
        ));
        createdSkillIds.add(studentBTeachUI.getSkillId());

        studentBLearnJava = skillDAO.insertSkill(new Skill(
                studentB.getStudentId(), "Java", SkillType.LEARN, ProficiencyLevel.BEGINNER, "Spring & JDBC"
        ));
        createdSkillIds.add(studentBLearnJava.getSkillId());
    }

    @AfterEach
    void tearDown() {
        for (int id : createdRequestIds) {
            try {
                requestDAO.deleteRequest(id);
            } catch (SQLException ignored) {
            }
        }
        for (int id : createdSkillIds) {
            try {
                skillDAO.deleteSkill(id);
            } catch (SQLException ignored) {
            }
        }
        for (int id : createdStudentIds) {
            try {
                studentDAO.deleteStudent(id);
            } catch (SQLException ignored) {
            }
        }
    }

    @Test
    @DisplayName("Should create a valid exchange request with status PENDING")
    void testCreateValidRequest() throws SQLException {
        SkillExchangeRequest req = exchangeService.createRequest(
                studentA.getStudentId(),
                studentB.getStudentId(),
                studentATeachJava.getSkillId(),
                studentBTeachUI.getSkillId()
        );

        assertNotNull(req);
        assertTrue(req.getRequestId() > 0);
        assertEquals(studentA.getStudentId(), req.getRequesterId());
        assertEquals(studentB.getStudentId(), req.getReceiverId());
        assertEquals(RequestStatus.PENDING, req.getStatus());
        createdRequestIds.add(req.getRequestId());

        // Verify incoming and outgoing lists
        List<SkillExchangeRequest> incoming = exchangeService.getIncomingRequests(studentB.getStudentId());
        assertEquals(1, incoming.size());
        assertEquals(req.getRequestId(), incoming.get(0).getRequestId());

        List<SkillExchangeRequest> outgoing = exchangeService.getOutgoingRequests(studentA.getStudentId());
        assertEquals(1, outgoing.size());
        assertEquals(req.getRequestId(), outgoing.get(0).getRequestId());
    }

    @Test
    @DisplayName("Should reject self-request (requester == receiver)")
    void testRejectSelfRequest() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeService.createRequest(
                        studentA.getStudentId(),
                        studentA.getStudentId(),
                        studentATeachJava.getSkillId(),
                        studentATeachJava.getSkillId()
                ));
        assertTrue(ex.getMessage().contains("yourself"));
    }

    @Test
    @DisplayName("Should reject offered skill that does not belong to requester")
    void testRejectOfferedSkillNotOwned() {
        // studentA tries to offer studentB's skill
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeService.createRequest(
                        studentA.getStudentId(),
                        studentB.getStudentId(),
                        studentBTeachUI.getSkillId(), // Not owned by A!
                        studentBTeachUI.getSkillId()
                ));
        assertTrue(ex.getMessage().contains("does not belong to you"));
    }

    @Test
    @DisplayName("Should reject offered skill of type LEARN (must be TEACH)")
    void testRejectOfferedSkillNotTeachType() {
        // studentA tries to offer a LEARN skill
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeService.createRequest(
                        studentA.getStudentId(),
                        studentB.getStudentId(),
                        studentALearnUI.getSkillId(), // LEARN type!
                        studentBTeachUI.getSkillId()
                ));
        assertTrue(ex.getMessage().contains("can only offer skills that you can TEACH"));
    }

    @Test
    @DisplayName("Should reject wanted skill of type LEARN (receiver must TEACH it)")
    void testRejectWantedSkillNotTeachType() {
        // studentA asks for studentB's LEARN skill
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeService.createRequest(
                        studentA.getStudentId(),
                        studentB.getStudentId(),
                        studentATeachJava.getSkillId(),
                        studentBLearnJava.getSkillId() // receiver's LEARN skill!
                ));
        assertTrue(ex.getMessage().contains("receiver can TEACH"));
    }

    @Test
    @DisplayName("Should prevent duplicate active request")
    void testPreventDuplicateActiveRequest() throws SQLException {
        SkillExchangeRequest req = exchangeService.createRequest(
                studentA.getStudentId(),
                studentB.getStudentId(),
                studentATeachJava.getSkillId(),
                studentBTeachUI.getSkillId()
        );
        createdRequestIds.add(req.getRequestId());

        // Attempting to send identical request while first is still pending
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                exchangeService.createRequest(
                        studentA.getStudentId(),
                        studentB.getStudentId(),
                        studentATeachJava.getSkillId(),
                        studentBTeachUI.getSkillId()
                ));
        assertTrue(ex.getMessage().contains("already pending or accepted"));
    }
}
