package flashsystem;

import java.util.Vector;

public class SinFileHeader {

	private byte[] version = new byte[1]; 
	private byte[] nextHeader = new byte[1];;
	private byte[] headersize = new byte[4];
	private byte[] partitionType = new byte[1];;
	private byte[] sinreserved = new byte[4];
	private byte[] hashlistsize = new byte[4];
	private SinPartInfo partitioninfo = new SinPartInfo();
	private byte[] header;
	private Vector<SinHashBlock> blocks = new Vector<SinHashBlock>();
	private int blocksize;
	
	public SinFileHeader(byte[] header) {
		System.arraycopy(header, 0, version, 0, 1);
		System.arraycopy(header, 1, nextHeader, 0, 1);
		System.arraycopy(header, 2, headersize, 0, 4);
		partitionType[0] = header[6];
		System.arraycopy(header, 7, sinreserved, 0, 4);
		System.arraycopy(header, 11, hashlistsize, 0, 4);
		if (getHashListSize()>0) {
			try {
				int hashoffset = 15;
				int read = 0;
				byte[] block = new byte[9];
				int index = 0;
				do {
					System.arraycopy(header, hashoffset, block, 0, block.length);
					hashoffset+=block.length;
					read+=block.length;
					SinHashBlock b = new SinHashBlock(block,index,getPartitionType()==0x0A?4096:0);
					byte[] hash = new byte[b.getHashSize()];
					System.arraycopy(header, hashoffset, hash, 0, hash.length);
					hashoffset+=hash.length;
					read+=hash.length;
					b.setHash(hash);
					blocks.add(b);
					if (b.getLength()>0x10)
						index++;
				} while (read < BytesUtil.getInt(hashlistsize));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int getPartitionInfoLength() {
		return blocks.get(0).getLength();
	}
	
	public boolean hasPartitionInfo() {
		return (blocks.size()>1);
	}
	
	public void setPartitionInfo(byte[] partinfo) {
		partitioninfo.setPartInfo(partinfo);
	}
	
	public byte[] getPartitionInfo() {
		return partitioninfo.getPartInfo();
	}
	
	public void setBlockSize(int bksize) {
		blocksize = bksize;
	}
	
	public long getOutfileLength() {
		return partitioninfo.getNbPartitionBlocks()*blocksize;
	}
	
	public int getNbHashBlocks() {
		return blocks.size();
	}
	
	public int getVersion() {
		return BytesUtil.getInt(version);
	}
	
	public int getNextHeader() {
		return BytesUtil.getInt(nextHeader);
	}
	
	public byte getPartitionType() {
		return partitionType[0];
	}

	public byte[] getHeader() {
		return header;
	}
	
	public String getPartypeString() {
		if (partitionType[0]==0x09)
			return "Without spare";
		if (partitionType[0]==0x0A)
			return "With spare";
		return "unknown";
	}
	
	public Vector<SinHashBlock> getHashBlocks() {
		return blocks;
	}
	
	public int getHashListSize() {
		return BytesUtil.getInt(hashlistsize);
	}

	public int getHeaderSize() {
		return BytesUtil.getInt(headersize);
	}

}
