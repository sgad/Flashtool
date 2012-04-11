package flashsystem.io;

import flashsystem.S1Packet;
import flashsystem.X10FlashException;
import java.io.IOException;
import org.system.Device;
import org.system.DeviceChangedListener;
import org.system.OS;

public class USBFlash {

	public static void open(String pid) throws IOException {
		DeviceChangedListener.pause(true);
		if (OS.getName().equals("windows")) {
			USBFlashWin32.open(pid);
		}
		else {
			USBFlashLinux.open(pid);
		}
	}

	public static void writeS1(S1Packet p) throws IOException,X10FlashException {
		if (OS.getName().equals("windows")) {
			USBFlashWin32.writeS1(p);
		}
		else {
			USBFlashLinux.writeS1(p);
		}
	}

	public static void write(byte[] array) throws IOException,X10FlashException {
		if (OS.getName().equals("windows")) {
			USBFlashWin32.write(array);
		}
		else {
			USBFlashLinux.write(array);
		}		
	}

	public static void readS1Reply()  throws IOException,X10FlashException {
		if (OS.getName().equals("windows")) {
			USBFlashWin32.readS1Reply();
		}
		else {
			USBFlashLinux.readS1Reply();
		}
	}

	public static void readReply()  throws IOException,X10FlashException {
		if (OS.getName().equals("windows")) {
			USBFlashWin32.readReply();
		}
		else {
			USBFlashLinux.readReply();
		}
	}

	public static int getLastFlags() {
		if (OS.getName().equals("windows")) {
			return USBFlashWin32.getLastFlags();
		}
		else {
			return USBFlashLinux.getLastFlags();
		}
    }

    public static byte[] getLastReply() {
		if (OS.getName().equals("windows")) {
			return USBFlashWin32.getLastReply();
		}
		else {
			return USBFlashLinux.getLastReply();
		}
    }

    public static void close() {
    	if (OS.getName().equals("windows")) {
    		USBFlashWin32.close();
    	}
    	else
    		USBFlashLinux.close();
    	DeviceChangedListener.pause(false);
    }
}