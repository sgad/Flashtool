package flashsystem;

public class TaEntry {

	String _partition="";
	String _data="";
	String _size="";
	
	public TaEntry() {
	}
	
	public void setPartition(int partition) {
		_partition = HexDump.toHex(partition);
	}
	
	public void setPartition(String partition) {
		_partition = partition;
	}
	
	public void addData(String data) {
		_data = _data + " "+data;
		_data = _data.trim();
	}
	
	public void setData(byte[] d) {
		_data = "";
		for (int i = 0;i<d.length;i++) {
			String dataS = HexDump.toHex(d[i]);
			addData(dataS.substring(dataS.length()-2));
		}
	}
	
	public void addData(char[] data) {
		for (int i = 0;i<data.length;i++) {
			String dataS = HexDump.toHex(data[i]);
			addData(dataS.substring(dataS.length()-2));
		}
	}
	
	public String getPartition() {
		return _partition;
	}
	
	public Byte[] getPartitionBytes() {
		return BytesUtil.getBytes(_partition);
	}

	public void setSize(String size) {
		_size=size;
	}
	
	public void resize(int newsize) {
		int cursize=0;
		if (_data.length()==2) {
			cursize=1;
		}
		else {
			cursize = _data.split(" ").length;
		}
		if (newsize > cursize) {
			for (int i = cursize;i<newsize;i++) {
				addData("FF");
			}
		}
		else if (newsize<cursize) {
			_data="";
			for (int i = 0;i<newsize;i++) {
				addData("FF");
			}			
		}
	}
	
	public String getComputedSize() {
		String lsize="";
		if (_data.length()==2)
			lsize=HexDump.toHex((int)1);
		else
			lsize= HexDump.toHex(_data.split(" ").length);
		lsize=lsize.substring(lsize.length()-4);
		return lsize;
	}

	public Byte[] getSizeBytes() {
		return BytesUtil.getBytes(getComputedSize());
	}
	
	public String getSize() {
		return _size;
	}
	
	public String getData() {
		return _data;
	}
	
	public String getDataString() {
		String[] result = _data.split(" ");
		byte[] b = new byte[result.length];
		for (int i=0;i<result.length;i++) {
			b[i]=BytesUtil.getBytes(result[i])[0];
		}
		return new String(b);
	}
	
	public Byte[] getDataBytes() {
		String[] datas = _data.split(" ");
		Byte[] data = new Byte[datas.length];
		for (int j=0;j<datas.length;j++) {
			data[j]=BytesUtil.getBytes(datas[j])[0];
		}
		return data;
	}
	
	public Byte[] getWordByte() {
		return BytesUtil.concatAll(getPartitionBytes(), getSizeBytes(), getDataBytes());
	}

	public byte[] getWordbyte() {
		Byte[] b1 = getWordByte();
		byte[] b = new byte[b1.length];
		for (int i=0;i<b1.length;i++) {
			b[i]=b1[i];
		}
		return b;
	}

	public String toString() {
		return getPartition()+" "+getComputedSize()+" "+getData();
	}
	
	public void close() throws TaParseException {
		if (Integer.parseInt(getComputedSize(),16)!=Integer.parseInt(_size,16)) {
			throw new TaParseException("TA entry ("+getPartition()+")parsing error");
		}
	}
}
