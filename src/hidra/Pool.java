package hidra;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class represents the pool of IP-addresses, that our DHCPServer has available to lease. 
 * It can be managed by claiming or releasing IP-addresses.
 */
public class Pool {
	
	public List<String> allIPs = new ArrayList<String>();
	// Map with claimed IPs as keys and corresponding client-MAC-address and the end of the leasetime as values. 
	public ConcurrentHashMap<String, IPPair> claimedIPs = new ConcurrentHashMap<String, IPPair>(); 
	public int poolWidth = 0; 
	public int standardLeaseTime = 0; // standard lease-time in milliseconds (= 1 day)
	public String lowestIPInPool = null;
	
	private String serverIP;
	
	/**
	 * Construct a pool where the lowest IP is given as an argument and 
	 * @param lowestIPInPool
	 * @param serverIP
	 */
	public Pool(String serverIP) {
		this.setServerIP(serverIP);
		int lowestIP = HidraUtility.strIPToInt(lowestIPInPool);
		for (int i = 0; i < poolWidth; i++) {
			allIPs.add(HidraUtility.intToStrIP(lowestIP+i));
		}
		
		if (allIPs.contains(serverIP)) {
			allIPs.remove(serverIP);
			allIPs.add(HidraUtility.intToStrIP(lowestIP+poolWidth));
		}
	}
	
	/**
	 * Print all claimed IPs with their corresponding MAC-addresses and lease end-times
	 * in an ordered way.
	 * @param map
	 */
	public void printsHashmap(ConcurrentHashMap<String, IPPair> map){
		System.out.println("\n*******************************\nLEASED IP ADDRESSES:");
		for (String key : map.keySet()) {
		    System.out.println("IP: " + key + " " + map.get(key).stringify());
		} 
		System.out.println("*******************************\n");

	}
	
	/**
	 * Claim an IP out of the unused IPs in this Pool.
	 * @return ip or null | The claimed IP as String
	 */
	public String claimIP(byte[] mac){
		boolean ipFound = false;
		String ip=null;

		for(int i=0; i< allIPs.size(); i++){

			if(!claimedIPs.containsKey(allIPs.get(i))){
				ipFound = true;
				ip = (String) allIPs.get(i);
				Date endTime = new Date(); 
				endTime.setTime(endTime.getTime()+standardLeaseTime); 
				claimedIPs.put(ip, new IPPair(mac, endTime));
				break;
			}	
		}
		if(ipFound){
			return ip;
		}
		else{
			System.out.println("No IP available");
			return null;
		}
	}
	
	/**
	 * Claim an IP out of the unused IPs in this Pool for the leaseTime specified.
	 * @return ip or null | The claimed IP as String
	 */
	public String claimIP(byte[] mac, int leaseTime){
		boolean ipFound = false;
		String ip=null;

		for(int i=0; i< allIPs.size(); i++){

			if(!claimedIPs.containsKey(allIPs.get(i))){
				ipFound = true;
				ip = (String) allIPs.get(i);
				Date endTime = new Date(); 
				endTime.setTime(endTime.getTime()+1000*leaseTime); 
				claimedIPs.put(ip, new IPPair(mac, endTime));
				break;
			}	
		}
		if(ipFound){
			return ip;
		}
		else{
			System.out.println("No IP available");
			return null;
		}
	}
	
	/**
	 * If possible, claim the IP specified as an argument.
	 * @return ip or null | The claimed IP as String
	 */
	public String claimIP(byte[] mac, String ip) {
		if (getAllIPs().contains(ip)) {
			if (getClaimedIPs().containsKey(ip)) {
				if (HidraUtility.areEqual(getClaimedIPs().get(ip).getMac(), mac)) {
					Date endTime = new Date(); 
					endTime.setTime(endTime.getTime()+standardLeaseTime);
					getClaimedIPs().get(ip).setEndLease(endTime);
					System.out.println("Leasetime renewed");
					return ip;
				}
				else {
					System.out.println("The requested IP is already occupied by another client.");
				}
			}
			else {
				Date endTime = new Date(); 
				endTime.setTime(endTime.getTime()+standardLeaseTime); 
				claimedIPs.put(ip, new IPPair(mac, endTime));
				return ip;
			}
		}
		System.out.println("The requested IP is not available in this pool.");
		return null;
	}
	
	/**
	 * Remove the given ip from the map of claimed IPs.
	 * @param ip
	 * @return
	 */
	public boolean disclaimIP(String ip){
		if(claimedIPs.containsKey(ip)){
			claimedIPs.remove(ip);
			// System.out.println("Returned IP : " + ip + " to pool");
			return true;
		}
		else{
			System.out.println("IP was not claimed, Could not release the IP");
			return false;
		}
	}
	
	/**
	 * Check if lease times have expired (used by server).
	 * Remove IPs from the claimed IPs if this is the case.
	 */
	public void checkLeaseTimes() {
		if (!(claimedIPs.size() == 0)) {
			Date now =  new Date();
			for (Entry<String, IPPair> entry : claimedIPs.entrySet()) {
				if (entry.getValue().getEndLease().before(now)) {
					claimedIPs.remove(entry.getKey());
					System.out.println("Removed IP " + entry.getKey() + " from claimed IPs because of expired lease time.");
				}
			}
		}
	}
	
	public ConcurrentHashMap<String, IPPair> getClaimedIPs() {
		return this.claimedIPs;
	}
	
	public List<String> getAllIPs() {
		return this.allIPs;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
}

