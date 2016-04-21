import simulator.*;

import java.util.Scanner;

/**
 * Driver class for round robin scheduling simulation
 */
public class SimulateRR {
    public static void main(String[] args){
        //Read info from screen/user to set up simulation parameters
        System.out.println("*** RR Simulator ***");
        Scanner scan = new Scanner(System.in);
        print("Enter configuration file name: ");
        String config_filename = scan.nextLine();
        print ("Enter slice time: ");
        int timeSlice = scan.nextInt();
        print("Enter cost of system call: ");
        int cost_syscall = scan.nextInt();
        print("Enter cost of context switch: ");
        int cost_context_switch = scan.nextInt();
        print("Enter trace level: ");
        int trace_level = scan.nextInt();
        scan.close();

        //Print trace level if asked for one
        if (trace_level>0){
            System.out.println("\n*** Trace ***");
        }

        //init kernel
        Kernel kernel = new KernelRR(timeSlice);

        //init trace
        TRACE.SET_TRACE_LEVEL(trace_level);

        //init config
        Config.init(kernel, cost_context_switch, cost_syscall);
        Config.buildConfiguration(config_filename);
        Config.run();

        //output results
        System.out.println("\n*** Results ***");
        System.out.println(Config.getSystemTimer().toString());
        print("Context switches: "+Config.getCPU().getContextSwitches()+"\n");
        System.out.printf("CPU utilization: %.2f\n",((double)Config.getSystemTimer().getUserTime())/Config.getSystemTimer().getSystemTime()*100);

    }

    /**
     * Helper method so that I don't have to type out System.out.print every time
     * @param s the string you want printed
     */
    private static void print(String s){
        System.out.print(s);
    }
}
