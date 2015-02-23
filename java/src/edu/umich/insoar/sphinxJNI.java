// JNI class for interfaceing with pocketsphinx
package edu.umich.insoar;

public class sphinxJNI {
	   static {
	      System.loadLibrary("sphinx"); // Load native library at runtime
	                                   // hello.dll (Windows) or libhello.so (Unixes)
	   }
	 
	   // Declare a native method sayHello() that receives nothing and returns void
	   public native String decodeAudio(String lmFile, String dicFile);
	   /*
	   public String decode()
	   {
		   String sentence;
		   new sphinxJNI().decodeAudio();
		   //sentence = "test";
		   return null;
	   }
	   */
	   // Test Driver
	   public static void main(String[] args) {
		   String lmFile = "/home/bolt/demo/speech/sample.lm";
		   String dicFile = "/home/bolt/demo/speech/sample.dic";
	      new sphinxJNI().decodeAudio(lmFile, dicFile);  // invoke the native method
	   }
	}
