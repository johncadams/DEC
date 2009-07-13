package com.smadanhoj.dec;

import java.io.File;

import com.smadanhoj.dec.mp3.MyMP3File;
import com.smadanhoj.dec.mp3.MyMP3File.NotAnMp3Exception;


abstract public class Mp3Walker {

	public static final String CWD = System.getProperty("CWD", "/content/music/slurped");
	
	protected boolean showErr = true;
	protected boolean descend;
	protected File[]  files;
	
	
	protected Mp3Walker(File[] files, boolean descend) {
		this.files   = files;
		this.descend = descend;
	}
	
	
	public void walkit() {
		for (int i=0; i<files.length; i++) {
			this.walkit(files[i], true);
		}
	}
	
	
	private void walkit(File file, boolean descend) {
		if (file.isDirectory()) {
			if (descend) {
				File[] files = file.listFiles();
				for (int i=0; i<files.length; i++) {
					walkit(files[i], this.descend);
				}
			}
			
		} else {
			try {
				processFile( new MyMP3File(file) );
				
			} catch (NotAnMp3Exception ex) {				

			} catch (Exception ex) {
				if (showErr) {
					String msg = ex.getMessage()==null?ex.toString():ex.getMessage();
					System.err.println(file+": "+msg);
				}
			}
		}		
	}
	
	
	abstract public void processFile(MyMP3File mp3File) throws Exception;
}