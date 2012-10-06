package se.marell.libusb;

import java.util.ArrayList;
import java.util.List;

public class VendorProductVisitor
  implements UsbSystem.UsbDeviceVisitor
{
  private int vendorId;
  private int productId;

  public VendorProductVisitor(int vendorId)
  {
    this.vendorId = vendorId;
  }

  public List<UsbDevice> visitDevices(List<UsbDevice> allDevices)
  {
    List devices = new ArrayList();
    for (UsbDevice d : allDevices) {
      if (d.getIdVendor() == this.vendorId) {
        devices.add(d);
      }
    }
    return devices;
  }
}