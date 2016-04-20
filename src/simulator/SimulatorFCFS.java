package simulator;

import java.util.Scanner;

/**
 * Created by Jacques on 4/18/2016.
 */
public class SimulatorFCFS {
    public static void main(String[] args){
        //Read info from screen/user
        System.out.println("*** FCFS Simulator ***");
        Scanner input = new Scanner(System.in);
        //File name
       print("Enter configuration file name: ");
        String config_filename = input.nextLine();
        //System call cost
       print("Enter cost of system call: ");
        int cost_syscall = input.nextInt();
        //Content switch cost
        System.out.print("Enter cost of context switch: ");
        int cost_context_switch = input.nextInt();
        //Trace level
       print("Enter trace level: ");
        int trace_level = input.nextInt();
        input.close();

        //Determine if there is a trace level, if so print trace
        if (trace_level>0){
            System.out.println("\n*** Trace ***");
        }

        //init eventQ, SystemTimer and kernel
        Kernel kernel = new FCFSKernel();

        //init trace
        TRACE.SET_TRACE_LEVEL(trace_level);

        //init config
        Config.init(kernel, cost_context_switch, cost_syscall);
        Config.buildConfiguration(config_filename);
        Config.run();

        System.out.println("\n*** Results ***");
        System.out.println(Config.getSimulationClock().toString());
        print("Context switches: "+Config.getCPU().getContextSwitches()+"\n");
        System.out.printf("CPU utilization: %.2f\n",((double)Config.getSystemTimer().getUserTime())/Config.getSystemTimer().getSystemTime()*100);
    }

    public static void print(String s){
        System.out.print(s);
    }
}
