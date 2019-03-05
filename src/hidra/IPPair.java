package hidra;



import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Class holding two properties that are used to link IPs to in the class Pool. 
 * These two properties are a MAC-address and a endDate.
 */
public class IPPair {
	private byte[] mac;
	private Date endLease;
	
	public IPPair(byte[] mac, Date endLease) {
		this.mac = mac;
		this.endLease = endLease;
	}

	public byte[] getMac() { 
		return mac; 
	}
  
	public Date getEndLease() { 
		return endLease; 
	}	
	
	public void setEndLease(Date endLease) {
		this.endLease = endLease;
	}
	
	public String stringify() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return "End Lease: " + dateFormat.format(getEndLease());
	}
 
}
