import simulator.Config;
import simulator.Kernel;
import simulator.TRACE;

import java.util.Scanner;

/**
 * A class for simulating the scheduling of processes on an operating system
 */

public class SimulateFCFS {
    public static void main(String[] args){
        //Get user input
        System.out.println("*** FCFS Simulator ***");
        Scanner input = new Scanner(System.in);
       print("Enter configuration file name: ");
        String config_filename = input.nextLine();
       print("Enter cost of system call: ");
        int cost_syscall = input.nextInt();
        System.out.print("Enter cost of context switch: ");
        int cost_context_switch = input.nextInt();
       print("Enter trace level: ");
        int trace_level = input.nextInt();
        input.close();

        //if there's a trace, print this line
        if (trace_level>0){
            System.out.println("\n*** Trace ***");
        }

        //init kernel
        Kernel kernel = new FCFSKernel();

        //init trace
        TRACE.SET_TRACE_LEVEL(trace_level);

        //init config
        Config.init(kernel, cost_context_switch, cost_syscall);
        Config.buildConfiguration(config_filename);
        Config.run();

        //output
        System.out.println("\n*** Results ***");
        System.out.println(Config.getSystemTimer().toString());
        print("Context switches: "+Config.getCPU().getContextSwitches()+"\n");
        System.out.printf("CPU utilization: %.2f\n",((double)Config.getSystemTimer().getUserTime())/Config.getSystemTimer().getSystemTime()*100);
    }

    /**
     * A helper method to prvent me typing out that long as line each time
     * @param s the string you want printed
     */
    public static void print(String s){
        System.out.print(s);
    }
}
