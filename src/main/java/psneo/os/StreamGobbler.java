package psneo.os;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class StreamGobbler implements Callable<byte[]> {
	private InputStream is;
	private ByteArrayOutputStream baos;
	private byte[] output;
	
	public StreamGobbler(InputStream is) {
		this.is = is;
		this.baos = new ByteArrayOutputStream();
	}
	
	@Override
	public byte[] call() throws Exception {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = this.is.read(buffer)) != -1) {
			this.baos.write(buffer, 0, len);
		}
		this.is.close();
		this.output = this.baos.toByteArray();
		this.baos.close();
		return this.output;
	}

	public byte[] getOutput() {
		if (this.baos.size() == 0) {
			return null;
		}
		return this.output;
	} 
}
