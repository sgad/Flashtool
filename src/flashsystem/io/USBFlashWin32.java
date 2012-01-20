package flashsystem.io;

import flashsystem.S1Packet;
import flashsystem.X10FlashException;
import java.io.IOException;

import org.logger.MyLogger;

import win32lib.JKernel32;

public class USBFlashWin32 {
	
	private static int lastflags;
	private static byte[] lastreply;
	
	public static void open() throws IOException {
		try {
    		MyLogger.getLogger().info("Opening device for R/W");
			JKernel32.openDevice();
			MyLogger.getLogger().info("Reading device information");
			readReply();
		}catch (Exception e) {
			if (lastreply == null) throw new IOException("Unable to read from device");
		}
	}

	public static void close() {
		JKernel32.closeDevice();
	}
	
	private static void sleep(int len) {
		try {
			Thread.sleep(len);
		}
		catch (Exception e) {}
	}

	public static boolean write(S1Packet p) throws IOException,X10FlashException {
		JKernel32.writeBytes(p.getByteArray());
		readReply();
		return true;
	}

    private static  void readReply() throws X10FlashException, IOException
    {
    	S1Packet p=null;
		boolean finished = false;
		while (!finished) {
			byte[] read = JKernel32.readBytes(0x10000);
			if (p==null) {
				p = new S1Packet(read);
			}
			else {
				p.addData(read);
			}
			finished=!p.hasMoreToRead();
		}
		p.validate();
		lastreply = p.getDataArray();
		lastflags = p.getFlags();
    }

    public static int getLastFlags() {
    	return lastflags;
    }
    
    public static byte[] getLastReply() {
    	return lastreply;
    }

}