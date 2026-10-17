package com.skillswap.model;

/**
 * Domain model representing platform-wide DBMS summary metrics
 * and active student request counts.
 */
public class PlatformReport {

    private int totalStudents;
    private int totalSkills;
    private int totalTeachSkills;
    private int totalLearnSkills;

    private int totalRequests;
    private int pendingRequests;
    private int acceptedRequests;
    private int completedExchanges;
    private int rejectedRequests;
    private int cancelledRequests;

    // Optional active student specific metrics
    private Integer activeStudentId;
    private int studentOutgoingRequests;
    private int studentIncomingRequests;
    private int studentCompletedExchanges;

    public PlatformReport() {
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalSkills() {
        return totalSkills;
    }

    public void setTotalSkills(int totalSkills) {
        this.totalSkills = totalSkills;
    }

    public int getTotalTeachSkills() {
        return totalTeachSkills;
    }

    public void setTotalTeachSkills(int totalTeachSkills) {
        this.totalTeachSkills = totalTeachSkills;
    }

    public int getTotalLearnSkills() {
        return totalLearnSkills;
    }

    public void setTotalLearnSkills(int totalLearnSkills) {
        this.totalLearnSkills = totalLearnSkills;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(int pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public int getAcceptedRequests() {
        return acceptedRequests;
    }

    public void setAcceptedRequests(int acceptedRequests) {
        this.acceptedRequests = acceptedRequests;
    }

    public int getCompletedExchanges() {
        return completedExchanges;
    }

    public void setCompletedExchanges(int completedExchanges) {
        this.completedExchanges = completedExchanges;
    }

    public int getRejectedRequests() {
        return rejectedRequests;
    }

    public void setRejectedRequests(int rejectedRequests) {
        this.rejectedRequests = rejectedRequests;
    }

    public int getCancelledRequests() {
        return cancelledRequests;
    }

    public void setCancelledRequests(int cancelledRequests) {
        this.cancelledRequests = cancelledRequests;
    }

    public Integer getActiveStudentId() {
        return activeStudentId;
    }

    public void setActiveStudentId(Integer activeStudentId) {
        this.activeStudentId = activeStudentId;
    }

    public int getStudentOutgoingRequests() {
        return studentOutgoingRequests;
    }

    public void setStudentOutgoingRequests(int studentOutgoingRequests) {
        this.studentOutgoingRequests = studentOutgoingRequests;
    }

    public int getStudentIncomingRequests() {
        return studentIncomingRequests;
    }

    public void setStudentIncomingRequests(int studentIncomingRequests) {
        this.studentIncomingRequests = studentIncomingRequests;
    }

    public int getStudentCompletedExchanges() {
        return studentCompletedExchanges;
    }

    public void setStudentCompletedExchanges(int studentCompletedExchanges) {
        this.studentCompletedExchanges = studentCompletedExchanges;
    }
}
