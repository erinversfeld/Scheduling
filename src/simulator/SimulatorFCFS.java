package simulator;

import java.util.Scanner;

/**
 * Created by Erin on 18/04/2016.
 */
public class SimulatorFCFS {

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

        EventQueue eventQueue = new EventQueue();
        SystemTimerImpl systemTimer = new SystemTimerImpl();
        Kernel kernel = new FCFSKernel();

        //init trace
        TRACE.SET_TRACE_LEVEL(trace_level);

        //init config
        Config.init(kernel, cost_context_switch, cost_syscall);
        Config.buildConfiguration(config_filename);
        Config.run();

        print("*** Results ***");
        print(Config.getSimulationClock().toString());
        print("Context switches: "+Config.getCPU().getContextSwitches());
        print("CPU utilization: "+(float)Config.getSimulationClock().getUserTime()/Config.getSimulationClock().getSystemTime()*100);
    }

    private static void print(String s){
        System.out.println(s.trim());
    }
}
