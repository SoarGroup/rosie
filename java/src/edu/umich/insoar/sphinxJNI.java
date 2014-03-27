// JNI class for interfaceing with pocketsphinx
package edu.umich.insoar;

public class sphinxJNI {
	   static {
	      System.loadLibrary("sphinx"); // Load native library at runtime
	                                   // hello.dll (Windows) or libhello.so (Unixes)
	   }
	 
	   // Declare a native method sayHello() that receives nothing and returns void
	   public native String decodeAudio();
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
	      new sphinxJNI().decodeAudio();  // invoke the native method
	   }
	}