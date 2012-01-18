package win32lib;

import java.io.IOException;
import org.system.Device;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;

import flashsystem.BytesUtil;

public class JKernel32 {

	public static Kernel32RW kernel32 = (Kernel32RW) Native.loadLibrary("kernel32", Kernel32RW.class, W32APIOptions.UNICODE_OPTIONS);
	static WinNT.HANDLE HandleToDevice = WinBase.INVALID_HANDLE_VALUE;
	static WinBase.OVERLAPPED ovwrite = new WinBase.OVERLAPPED();

	public static boolean openDevice() throws IOException {
        /* Kernel32RW.GENERIC_READ | Kernel32RW.GENERIC_WRITE not used in dwDesiredAccess field for system devices such a keyboard or mouse */
        int shareMode = WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE;
        int Access = WinNT.GENERIC_WRITE | WinNT.GENERIC_READ;
		HandleToDevice = Kernel32.INSTANCE.CreateFile(
                Device.getConnectedDeviceWin32().getDevPath(), 
                Access, 
                shareMode, 
                null, 
                WinNT.OPEN_EXISTING, 
                0,//WinNT.FILE_FLAG_OVERLAPPED, 
                (WinNT.HANDLE)null);
		if (HandleToDevice == WinBase.INVALID_HANDLE_VALUE) throw new IOException(getLastError());
		return true;
	}

	public static boolean openDeviceAsync() throws IOException {
        /* Kernel32RW.GENERIC_READ | Kernel32RW.GENERIC_WRITE not used in dwDesiredAccess field for system devices such a keyboard or mouse */
        int shareMode = WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE;
        int Access = WinNT.GENERIC_WRITE | WinNT.GENERIC_READ;
		HandleToDevice = Kernel32.INSTANCE.CreateFile(
                Device.getConnectedDeviceWin32().getDevPath(), 
                Access, 
                shareMode, 
                null, 
                WinNT.OPEN_EXISTING, 
                WinNT.FILE_FLAG_OVERLAPPED, 
                (WinNT.HANDLE)null);
		if (HandleToDevice == WinBase.INVALID_HANDLE_VALUE) throw new IOException(getLastError());
		return true;
	}

	public static byte[] readBytes(int bufsize) throws IOException {
		IntByReference nbread = new IntByReference();
		byte[] b = new byte[bufsize];
		boolean result = kernel32.ReadFile(HandleToDevice, b, bufsize, nbread, null);
		if (!result) throw new IOException("Read error :"+getLastError());
		return BytesUtil.getReply(b,nbread.getValue());
	}

	public static WinNT.HANDLE createEvent() throws IOException {
		WinNT.HANDLE hevent = kernel32.CreateEvent(null, false, false, null);
		int res = kernel32.GetLastError();
		if (hevent == WinBase.INVALID_HANDLE_VALUE || res!=0)
				throw new IOException(JKernel32.getLastError());
		return hevent;
	}
	
	public static byte[] readBytesAsync(int bufsize) throws IOException {
		IntByReference nbread = new IntByReference();
		WinBase.OVERLAPPED ovread = new WinBase.OVERLAPPED();
		ovread.Offset     = 0; 
		ovread.OffsetHigh = 0;
		//ovread.hEvent = JKernel32.createEvent();
		System.out.println("Read Event created");
		byte[] b = new byte[bufsize];
		if (!kernel32.ReadFile(HandleToDevice, b, bufsize, nbread, ovread)) {
			System.out.println("Read done");
			if (kernel32.GetLastError() == kernel32.ERROR_IO_PENDING) {
				System.out.println("IO Pending, waiting event");
				//if (kernel32.WaitForSingleObject(ovread.hEvent, 10000)==kernel32.WAIT_OBJECT_0) {
					System.out.println("Getting overlapped result");
					boolean result = kernel32.GetOverlappedResult(HandleToDevice, ovread, nbread, true);
					System.out.println(result + " : " + JKernel32.getLastError());
				//}
				//else {
				//	System.out.println(JKernel32.getLastError());
				//}
			}
		}
		kernel32.CloseHandle(ovread.hEvent);
		return BytesUtil.getReply(b,nbread.getValue());
	}
	
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {}
	}
	
	public static boolean writeBytesAsync(byte bytes[]) throws IOException {
		System.out.println("=== Begin Writing to device ===");
		IntByReference nbwritten = new IntByReference();
		ovwrite.Offset     = 0; 
		ovwrite.OffsetHigh = 0;
		ovwrite.hEvent = JKernel32.createEvent();
		System.out.println("Write Event created");
		if (!kernel32.WriteFile(HandleToDevice, bytes, bytes.length, nbwritten,ovwrite)) {
			System.out.println("Write done");
			if (kernel32.GetLastError() == kernel32.ERROR_IO_PENDING) {
				System.out.println("IO Pending, waiting event");
				if (kernel32.WaitForSingleObject(ovwrite.hEvent, kernel32.INFINITE)==kernel32.WAIT_OBJECT_0) {
					if (!kernel32.GetOverlappedResult(HandleToDevice, ovwrite, nbwritten, true))
						System.out.println(JKernel32.getLastError());
				}
				else {
					System.out.println(JKernel32.getLastError());
				}
			}
		}
		kernel32.CloseHandle(ovwrite.hEvent);
		System.out.println("buffer : "+bytes.length+" nbwritten : "+nbwritten.getValue());
		if (nbwritten.getValue()!=bytes.length)
			throw new IOException("Did not write all data");
		bytes = null;
		System.out.println("==== End Writing to device ===");
		return true;
	}

	public static boolean writeBytes(byte bytes[]) throws IOException {
		IntByReference nbwritten = new IntByReference();
		boolean result = kernel32.WriteFile(HandleToDevice, bytes, bytes.length, nbwritten, null);
		if (!result) if (!result) throw new IOException(getLastError());
		if (nbwritten.getValue()!=bytes.length) throw new IOException("Did not write all data");
		bytes = null;
		return result;
	}

	public static boolean closeDevice() {
		boolean result = true;
		
		if (HandleToDevice != WinBase.INVALID_HANDLE_VALUE) {
			result = kernel32.CloseHandle(HandleToDevice);
		}
		HandleToDevice = WinBase.INVALID_HANDLE_VALUE;
		return result;
	}
	
	public static int getLastErrorCode() {
		return Kernel32.INSTANCE.GetLastError();
	}
	
	public static String getLastError() {
		int code = Kernel32.INSTANCE.GetLastError();
	    Kernel32 lib = Kernel32.INSTANCE;
	    PointerByReference pref = new PointerByReference();
	    /*OVERLAPPED ov = new OVERLAPPED();
	    ov.Offset=0;
	    ov.OffsetHigh=0;
	    kernel32.CreateEvent(null, arg1, arg2, arg3)*/
	    lib.FormatMessage(
	        WinBase.FORMAT_MESSAGE_ALLOCATE_BUFFER | WinBase.FORMAT_MESSAGE_FROM_SYSTEM | WinBase.FORMAT_MESSAGE_IGNORE_INSERTS, 
	        null, 
	        code, 
	        0, 
	        pref, 
	        0, 
	        null);
	    String s = code + " : " +pref.getValue().getString(0, !Boolean.getBoolean("w32.ascii"));
	    lib.LocalFree(pref.getValue());
	    return s.replaceAll("[\n\r]+"," ");
	}

}
