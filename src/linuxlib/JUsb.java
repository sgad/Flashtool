package linuxlib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.logger.MyLogger;

import flashsystem.BytesUtil;
import flashsystem.HexDump;

import se.marell.libusb.LibUsbBusyException;
import se.marell.libusb.LibUsbInvalidParameterException;
import se.marell.libusb.LibUsbNoDeviceException;
import se.marell.libusb.LibUsbNotFoundException;
import se.marell.libusb.LibUsbOtherException;
import se.marell.libusb.LibUsbOverflowException;
import se.marell.libusb.LibUsbPermissionException;
import se.marell.libusb.LibUsbPipeException;
import se.marell.libusb.LibUsbSystem;
import se.marell.libusb.LibUsbTimeoutException;
import se.marell.libusb.LibUsbTransmissionException;
import se.marell.libusb.UsbDevice;
import se.marell.libusb.UsbSystem;
import se.marell.libusb.VendorProductVisitor;


public class JUsb {
	
	private static byte[] data = new byte[512];
	private static UsbSystem us=null;
	private static UsbDevice dev=null;
	private static String VendorId = "";
	private static String DeviceId = "";
	private static String Serial = "";
	
	public static void init() throws LibUsbNoDeviceException, LibUsbPermissionException, LibUsbOtherException {
		us = new LibUsbSystem(false, 0);
	}
	
	public static void fillDevice() {
		dev = getDevice();
		if (dev!=null) {
			VendorId = HexDump.toHex(dev.getIdVendor()).toUpperCase();
			DeviceId = HexDump.toHex(dev.getIdProduct()).toUpperCase();
			try {
				dev.open();
				Serial = dev.get_string_ascii((byte)3);
				dev.close();
			} catch (LibUsbNoDeviceException e) {
				dev=null;
				VendorId = "";
				DeviceId = "";
				Serial = "";
			} catch (LibUsbPermissionException e) {
				MyLogger.getLogger().error("No permission on device. Add valid udev rules");
				dev=null;
				VendorId = "";
				DeviceId = "";
				Serial = "";
			} catch (LibUsbOtherException e) {
				dev=null;
				VendorId = "";
				DeviceId = "";
				Serial = "";
			}
		}
		else {
			VendorId = "";
			DeviceId = "";
			Serial = "";			
		}
	}
	
	public static String getVendorId() {
		return VendorId;
	}
	
	public static String getProductId() {
		return DeviceId;
	}
	
	public static String getSerial() {
		return Serial;
	}
	
	private static UsbDevice getDevice() {
		UsbDevice device=null;		
		ArrayList<UsbDevice> devices = new ArrayList<UsbDevice>();
		try {
			devices = (ArrayList<UsbDevice>)us.visitUsbDevices(new VendorProductVisitor(0xfce));
		} catch (LibUsbNoDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LibUsbPermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LibUsbOtherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    if (devices.size()> 0 ) {
	    	device = devices.get(0);
	    }
	    return device;
	}
	
	public static void open() throws LibUsbNoDeviceException, LibUsbPermissionException, LibUsbOtherException, LibUsbNotFoundException, LibUsbBusyException, LibUsbInvalidParameterException {
  	  	dev.open();
  	  	if (dev.kernel_driver_active(0))
  	  		dev.detach_kernel_driver(0);
  	  	dev.claim_interface(0);		
	}
	
	public static void writeBytes(byte[] towrite) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(towrite);		
  	  	boolean hasData = true;
  	  	int loop = 0;
  	  	while (hasData) {
  	  			try {
					int read = in.read(data);
					if (read > 0) {
	  	  				dev.bulk_write(0x01, BytesUtil.getReply(data, read), 0);
					}
					else hasData=false;
				} catch (LibUsbTimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LibUsbPipeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LibUsbNoDeviceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LibUsbTransmissionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LibUsbOtherException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
  	  	}
  	  	in.close();
	}

	public static void close() throws Exception {
  	  	dev.release_interface(0);
  	  	dev.close();		
	}
	
	public static byte[] readBytes() throws LibUsbTimeoutException, LibUsbPipeException, LibUsbOverflowException, LibUsbNoDeviceException, LibUsbOtherException {
		int read = dev.bulk_read(0x81, data, 0);
		return BytesUtil.getReply(data, read);
	}

	public static byte[] readBytes(int timeout) throws LibUsbTimeoutException, LibUsbPipeException, LibUsbOverflowException, LibUsbNoDeviceException, LibUsbOtherException {
		int read = dev.bulk_read(0x81, data, timeout);
		return BytesUtil.getReply(data, read);
	}

	public static void cleanup() throws Exception {
		us.cleanup();
	}

}