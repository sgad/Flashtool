package flashsystem.io;

import flashsystem.HexDump;
import flashsystem.S1Packet;
import flashsystem.X10FlashException;
import java.io.IOException;
import org.logger.MyLogger;

import win32lib.JKernel32;
import linuxlib.JUsb;

public class USBFlashLinux {
	
	private static int lastflags;
	private static byte[] lastreply;
	
	public static void linuxOpen(String pid) throws IOException, Exception  {
			MyLogger.getLogger().info("Opening device for R/W");
			JUsb.fillDevice(false);
			JUsb.open();
			MyLogger.getLogger().info("Device ready for R/W.");
	}

	public static void linuxWriteS1(S1Packet p) throws IOException,X10FlashException {
		try {
			MyLogger.getLogger().debug("Writing packet to phone");
			if (p.getDataLength()>=65536) {
				byte[] part1 = new byte[65536];
				byte[] part2 = new byte[p.getByteArray().length-65536];
				System.arraycopy(p.getByteArray(), 0, part1, 0, 65536);
				System.arraycopy(p.getByteArray(), 65536, part2, 0, part2.length);
				JUsb.writeBytes(part1);
				JUsb.writeBytes(part2);
			}
			else
				JUsb.writeBytes(p.getByteArray());
			MyLogger.getLogger().debug("OUT : " + p);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void linuxWrite(byte[] array) throws IOException,X10FlashException {
		try {
			JUsb.writeBytes(array);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static  void linuxReadS1Reply() throws X10FlashException, IOException
    {
    	MyLogger.getLogger().debug("Reading packet from phone");
    	S1Packet p=null;
    	boolean finished = false;
		try {
			while (!finished) {
				byte[] b = JUsb.readBytes();
				if (p==null) {
					p = new S1Packet(b);
				}
				else {
					p.addData(b);
				}
				finished=!p.hasMoreToRead();
			}
			p.validate();
			MyLogger.getLogger().debug("IN : " + p);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (p!=null) p.release();
			p=null;
		}
    	if (p!=null) {
    		lastreply = p.getDataArray();
    		lastflags = p.getFlags();
    		p.release();
    	}
    	else {
    		lastreply = null;
    		lastflags = 0;
    	}
    	if (lastreply == null) throw new X10FlashException("Cannot read from device");
    }

    public static  void linuxReadS1Reply(int timeout) throws X10FlashException, IOException
    {
    	S1Packet p=null;
    	boolean finished = false;
		try {
			while (!finished) {
				byte[] b = JUsb.readBytes(timeout);
				if (p==null) {
					p = new S1Packet(b);
				}
				else {
					p.addData(b);
				}
				finished=!p.hasMoreToRead();
			}
			p.validate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (p!=null) p.release();
			p=null;
		}
    	if (p!=null) {
    		lastreply = p.getDataArray();
    		lastflags = p.getFlags();
    		p.release();
    	}
    	else {
    		lastreply = null;
    		lastflags = 0;
    	}
    }

    public static void linuxReadReply()  throws X10FlashException, IOException {
    	try {
			lastreply = JUsb.readBytes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static int linuxGetLastFlags() {
    	return lastflags;
    }
    
    public static byte[] linuxGetLastReply() {
    	return lastreply;
    }

    public static void linuxClose() {
		try {
			JUsb.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}