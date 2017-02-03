java org.antlr.v4.Tool Regress.g4
javac *.java
java org.antlr.v4.runtime.misc.TestRig Regress corpus john_trace.exp.txt -tokens -gui
