package hidra;

import java.util.ArrayList;

public class HidraSubjectsSecurityProperties {
	private int i = 0; // to keep track of one-way chain function(?)
	private byte[] KSCM;
	private byte[] NonceSCM;
	public HidraSubjectsSecurityProperties(byte[] KSCM,  byte[] NonceSCM) {
		this.setKSCM(KSCM);
		this.NonceSCM = NonceSCM;
	}
	public byte[] getKSCM() {
		return KSCM;
	}
	
	private void setKSCM(byte[] kSCM) {
		KSCM = kSCM;
	}
}
