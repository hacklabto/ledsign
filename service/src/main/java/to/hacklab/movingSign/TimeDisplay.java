package to.hacklab.movingSign;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.GregorianCalendar;

import to.hacklab.movingSign.blinkenLights.BLFramePacket;
import to.hacklab.movingSign.blinkenLights.BLNetworkException;
import to.hacklab.movingSign.blinkenLights.BLPacketSender;

public class TimeDisplay {
	
	/**
	 * Creates a new TimeDisplay for showing the time
	 * 
	 * @param host the hostname or IP address to send to
	 * @param portNumber the port number (as a String) to send to
	 * @param timeout the number of seconds to show each page
	 */
	public TimeDisplay(String host, String portNumber, String timeout) {
		int port = 2323;
		int totalTime = 1;
		try {
			port = Integer.parseInt(portNumber);
			totalTime = Integer.parseInt(timeout);
		} catch(NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		if(totalTime < 1) {
			totalTime = 0;
		}
		
		BLPacketSender sender = null;
		try {
			sender = new BLPacketSender(host, port);
		} catch (BLNetworkException e) {
			e.printStackTrace();
			return;
		}
		
		BufferedImage img = new BufferedImage(96, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D)img.getGraphics();		

		// set up the font
		g2.setFont(new Font("SansSerif", Font.BOLD, 20));
		int fontHeight = (g2.getFontMetrics().getMaxAscent());	

		try {
			for(int j = 0; (j < (totalTime * 2)) || totalTime == 0; j ++) {
				GregorianCalendar date = new GregorianCalendar();
				int hour = date.get(GregorianCalendar.HOUR_OF_DAY);
				int min = date.get(GregorianCalendar.MINUTE);
				int sec = date.get(GregorianCalendar.SECOND);
				
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, img.getWidth(), img.getHeight());
				g2.setColor(Color.WHITE);
				String timeStr = String.format("%02d:%02d:%02d", hour, min, sec);
				g2.drawString(timeStr, 4, (img.getHeight() / 2) + (fontHeight / 3) + 1);
				
				g2.fillRect(0, 0, (min * 96) / 60, 2);
				g2.fillRect(0, 30, (sec * 96) / 60, 2);
				
				BLFramePacket framePacket = new BLFramePacket(img);
				sender.send(framePacket.getNetworkBytes());
				Thread.sleep(100);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 3) {
			System.err.println("Usage: hostname port timeout");
			System.exit(1);
		}
		new TimeDisplay(args[0], args[1], args[2]);
	}

}
