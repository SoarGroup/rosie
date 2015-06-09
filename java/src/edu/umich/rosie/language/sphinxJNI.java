// JNI class for interfaceing with pocketsphinx
package edu.umich.rosie.language;

public class sphinxJNI {
	   static {
	      System.loadLibrary("sphinx"); // Load native library at runtime
	      // sphinx.dll (Windows) or libsphinx.so (Unixes)
	   }
	   // Declare a native method decodeAudio
	   public native String decodeAudio(String lmFile, String dicFile);
	}
