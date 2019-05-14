package hidra;

import java.util.ArrayList;

public class HidraSubjectsSecurityProperties {
	private int i = 0; // to keep track of one-way chain function(?)
	private byte[] KSCM;
	private byte[] NonceSCM;
	private byte[] nonce2; 
	private byte[] nonceSR;
	
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
	public byte[] getNonce2() {
		return nonce2;
	}
	public void setNonce2(byte[] nonce2) {
		this.nonce2 = nonce2;
	}
	public byte[] getNonceSR() {
		return nonceSR;
	}
	public void setNonceSR(byte[] nonceSR) {
		this.nonceSR = nonceSR;
	}
}
