package flashsystem;

public class SinPartInfo {

	byte[] partinfo=null;

	public SinPartInfo() {
	}

	public SinPartInfo(byte[] array) {
		partinfo = array;
	}

	public void setPartInfo(byte[] array) {
		partinfo = array;
	}

	public byte[] getPartInfo() {
		if (partinfo == null) {
			byte[] b = new byte[1];
			b[0] = 0;
			return b;
		}
		return partinfo;
	}
	
	public long getNbPartitionBlocks() {
		if (partinfo == null) return 0;
		byte[] nbblocks = new byte[4];
		System.arraycopy(partinfo, 12, nbblocks, 0, 4);
		int res = BytesUtil.getInt(nbblocks);
		
		if ((res < 0)|| (res>64*1024))
			BytesUtil.revert(nbblocks);
		return BytesUtil.getLong(nbblocks);
	}
}
