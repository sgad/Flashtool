package libusb;

import java.util.Iterator;
import java.util.Vector;

public class UsbDevList {

	Vector<UsbDevice> list = new Vector<UsbDevice>();
	
	public void addDevice(UsbDevice d) {
		d.setConfiguration();
		d.ref();
		list.add(d);
	}

	public Iterator<UsbDevice> getDevices() {
		return list.iterator();
	}

	public void destroyDevices() {
		Iterator<UsbDevice> i = getDevices();
		while (i.hasNext()) {
			i.next().destroy();
		}
		list.clear();
	}

	public int size() {
		return list.size();
	}

	public UsbDevice get(int index) {
		return list.get(index);
	}

}