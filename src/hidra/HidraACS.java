package hidra;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import hidra.Terminal;

/* 
 * Send UDP message into WSN
 * 
 */
public class HidraACS {
	public static void main(String[] args) throws IOException {
		System.out.println("Start ACS");
		
		// Set up connection with RPL border router 
		Terminal.execute("make --directory /home/user/thesis-code/contiki/examples/ipv6/rpl-border-router/ TARGET=cooja connect-router-cooja");

		// Default port = 60001
	}
	
	
}

