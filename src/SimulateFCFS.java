import simulator.*;

import java.util.Scanner;

/**
 * Created by Erin on 18/04/2016.
 */
public class SimulateFCFS {

    public static void main(String[] args){
        print("*** FCFS ***");
        Scanner s = new Scanner(System.in);
        print("Enter configuration file name: ");
        String config_filename = s.nextLine();
        print("Enter cost of system call: ");
        int cost_syscall = s.nextInt();
        print("Enter cost of context switch: ");
        int cost_context_switch = s.nextInt();
        print("Enter trace level: ");
        int trace_level = s.nextInt();
        s.close();

        //check if there'll be a trace printed out
        if (trace_level>0){
            print("*** Trace ***");
        }

        final Kernel kernel = new FCFSKernel();

        //init trace
        TRACE.SET_TRACE_LEVEL(trace_level);

        //init config
        Config.init(kernel, cost_context_switch, cost_syscall);
        Config.buildConfiguration(config_filename);
        Config.run();

        System.out.println("\n*** Results ***");
        System.out.println(Config.getSystemTimer().toString());
        print("Context switches: "+Config.getCPU().getContextSwitches()+"\n");
        System.out.printf("CPU utilization: %.2f\n",((double)Config.getSystemTimer().getUserTime())/Config.getSystemTimer().getSystemTime()*100);
    }

    private static void print(String s){
        System.out.print(s.trim());
    }
}
