package hidra;

import java.util.ArrayList;

public class SubjectSecurityProperties {
	private byte[] KSCM;
	private byte[] NonceSCM;
	private byte[] nonce2; 
	private byte[] nonceSR;
	private byte[] pseudonym = new byte[2];
	
	public SubjectSecurityProperties(byte[] KSCM,  byte[] NonceSCM) {
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
	public byte[] getPseudonym() {
		return pseudonym;
	}
	public void setPseudonym(byte[] pseudonym) {
		this.pseudonym = pseudonym;
	}
}
