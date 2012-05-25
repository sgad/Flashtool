package flashsystem.io;

import flashsystem.HexDump;
import flashsystem.S1Packet;
import flashsystem.X10FlashException;
import java.io.IOException;

import org.logger.MyLogger;

import linuxlib.JUsb;

public class USBFlashLinux {
	
	private static int lastflags;
	private static byte[] lastreply;
	
	public static void open(String pid) throws IOException {
		try {
			MyLogger.getLogger().info("Opening device for R/W");
			JUsb.open();
		}catch (Exception e) {
			if (lastreply == null) throw new IOException("Unable to read from device");
		}
	}

	public static void writeS1(S1Packet p) throws IOException,X10FlashException {
		try {
			JUsb.writeBytes(p.getByteArray());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		readS1Reply();
	}

	public static void write(byte[] array) throws IOException,X10FlashException {
		try {
			JUsb.writeBytes(array);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static  void readS1Reply() throws X10FlashException, IOException
    {
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
    		p.release();
    	}
    }

    public static void readReply()  throws X10FlashException, IOException {
    	try {
			lastreply = JUsb.readBytes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static int getLastFlags() {
    	return lastflags;
    }
    
    public static byte[] getLastReply() {
    	return lastreply;
    }

    public static void close() {
		try {
			JUsb.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}