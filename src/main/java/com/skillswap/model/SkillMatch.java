package com.skillswap.model;

/**
 * Domain model representing a matched skill exchange opportunity between students.
 */
public class SkillMatch {

    private int partnerStudentId;
    private String partnerName;
    private String partnerEmail;
    private String partnerContact;

    // The skill the active student can teach to the partner
    private int myTeachSkillId;
    private String myTeachSkillName;
    private ProficiencyLevel myTeachLevel;

    // The skill the partner can teach to the active student (populated for mutual matches)
    private int partnerTeachSkillId;
    private String partnerTeachSkillName;
    private ProficiencyLevel partnerTeachLevel;

    private boolean isMutual;
    private String matchTypeDescription;

    public SkillMatch() {
    }

    public int getPartnerStudentId() {
        return partnerStudentId;
    }

    public void setPartnerStudentId(int partnerStudentId) {
        this.partnerStudentId = partnerStudentId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerEmail() {
        return partnerEmail;
    }

    public void setPartnerEmail(String partnerEmail) {
        this.partnerEmail = partnerEmail;
    }

    public String getPartnerContact() {
        return partnerContact;
    }

    public void setPartnerContact(String partnerContact) {
        this.partnerContact = partnerContact;
    }

    public int getMyTeachSkillId() {
        return myTeachSkillId;
    }

    public void setMyTeachSkillId(int myTeachSkillId) {
        this.myTeachSkillId = myTeachSkillId;
    }

    public String getMyTeachSkillName() {
        return myTeachSkillName;
    }

    public void setMyTeachSkillName(String myTeachSkillName) {
        this.myTeachSkillName = myTeachSkillName;
    }

    public ProficiencyLevel getMyTeachLevel() {
        return myTeachLevel;
    }

    public void setMyTeachLevel(ProficiencyLevel myTeachLevel) {
        this.myTeachLevel = myTeachLevel;
    }

    public int getPartnerTeachSkillId() {
        return partnerTeachSkillId;
    }

    public void setPartnerTeachSkillId(int partnerTeachSkillId) {
        this.partnerTeachSkillId = partnerTeachSkillId;
    }

    public String getPartnerTeachSkillName() {
        return partnerTeachSkillName;
    }

    public void setPartnerTeachSkillName(String partnerTeachSkillName) {
        this.partnerTeachSkillName = partnerTeachSkillName;
    }

    public ProficiencyLevel getPartnerTeachLevel() {
        return partnerTeachLevel;
    }

    public void setPartnerTeachLevel(ProficiencyLevel partnerTeachLevel) {
        this.partnerTeachLevel = partnerTeachLevel;
    }

    public boolean isMutual() {
        return isMutual;
    }

    public void setMutual(boolean mutual) {
        isMutual = mutual;
    }

    public String getMatchTypeDescription() {
        return matchTypeDescription;
    }

    public void setMatchTypeDescription(String matchTypeDescription) {
        this.matchTypeDescription = matchTypeDescription;
    }

    @Override
    public String toString() {
        if (isMutual) {
            return String.format("[MUTUAL MATCH] %s (ID: %d) | You teach: %s (%s) <---> They teach: %s (%s)",
                    partnerName, partnerStudentId, myTeachSkillName, myTeachLevel, partnerTeachSkillName, partnerTeachLevel);
        } else {
            return String.format("[PARTIAL MATCH] %s (ID: %d) | %s",
                    partnerName, partnerStudentId, matchTypeDescription);
        }
    }
}
