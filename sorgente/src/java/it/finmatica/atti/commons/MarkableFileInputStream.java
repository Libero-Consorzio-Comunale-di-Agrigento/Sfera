package it.finmatica.atti.commons;

import org.apache.commons.io.input.CountingInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class MarkableFileInputStream extends CountingInputStream {
	private FileChannel myFileChannel;
    private long mark = -1;

    public MarkableFileInputStream (InputStream is) {
        super (is);
        
        if (is instanceof FileInputStream) {
        	myFileChannel = ((FileInputStream)is).getChannel();
        }
    }

    @Override
    public boolean markSupported() {
    	if (myFileChannel != null) {
    		return true;
    	}
    	
    	return super.markSupported();
    }

    @Override
    public synchronized void mark (int readlimit) {
    	if (myFileChannel != null) {
    		try {
    			mark = myFileChannel.position();
    		} catch (IOException ex) {
    			mark = -1;
    		}
    	} else {
    		super.mark(readlimit);
    	}    	
    }

    @Override
    public synchronized void reset () throws IOException {
    	if (myFileChannel != null) {
            if (mark == -1) {
                throw new IOException("not marked");
            }
            myFileChannel.position(mark);
    	} else {
    		super.reset();
    	}
    }

	@Override
	public void close () throws IOException {
		// impedisco la chiusura dello stream così da poterlo rileggere
	}
	
	public void chiudiMeglio () throws IOException {
		super.close();
	}
}
