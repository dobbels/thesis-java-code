package hidra;

public class HidraSubjectsSecurityProperties {
	private int i = 0; // to keep track of one-way chain function(?)
	private byte[] KSCM;
	private byte[] NonceSCM;
	public HidraSubjectsSecurityProperties(byte[] KSCM,  byte[] NonceSCM) {
		this.KSCM = KSCM;
		this.NonceSCM = NonceSCM;
	}
}
