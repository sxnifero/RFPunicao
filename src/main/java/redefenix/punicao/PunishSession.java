package redefenix.punicao;

import java.util.UUID;

public class PunishSession {
    public enum State { AWAITING_REASON, AWAITING_TIME }

    private final String targetName;
    private final boolean isBan;
    private final boolean isPermanent;
    private State state;
    private String reason;

    public PunishSession(String targetName, boolean isBan, boolean isPermanent) {
        this.targetName = targetName;
        this.isBan = isBan;
        this.isPermanent = isPermanent;
        this.state = State.AWAITING_REASON;
    }

    public String getTargetName() { return targetName; }
    public boolean isBan() { return isBan; }
    public boolean isPermanent() { return isPermanent; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
