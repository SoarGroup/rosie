package edu.umich.rosie.gui;

import sml.Kernel;
import sml.Agent;

public class SVSTester {
    public static void main(String[] args) {
        Kernel k = Kernel.CreateKernelInNewThread(64001);
        Agent a = k.CreateAgent("soar");
        System.out.println(a.ExecuteCommandLine("svs -e"));
        System.out.println(a.ExecuteCommandLine("svs connect_viewer 65000"));
        a.SendSVSInput("add snowman world p 2 2 0");
        a.RunSelf(1);
//      System.out.println(a.ExecuteCommandLine("svs S1.scene.sgel add snowman world p 2 2 0"));
        while (true) {
            //need to not exit so viewer can show stuff
        }
    }
}