package simulator;
//
import java.io.FileNotFoundException;
import java.io.IOException;
//
import java.util.*;

import static simulator.ProcessControlBlock.State.*;

/**
 * Concrete Kernel type
 * 
 * @author Stephan Jamieson
 * @version 8/3/15
 */
public class FCFSKernel implements Kernel {
    

    //changed to a linkedlist because it's less complex
    private LinkedList<ProcessControlBlock> readyQueue;
    //this is a bad idea, but so is implementing an OS in Java
    private static int pid = 1;

    public FCFSKernel() {
		// Set up the ready queue.
        this.readyQueue = new LinkedList<ProcessControlBlock>();
    }
    
    private ProcessControlBlock dispatch() {
        ProcessControlBlockImpl prev_process = (ProcessControlBlockImpl)Config.getCPU().getCurrentProcess();
        ProcessControlBlockImpl next_process;

        if(readyQueue.isEmpty()){
           //set cpu to idle
            Config.getCPU().contextSwitch(null);
        }
        else{
            next_process = (ProcessControlBlockImpl)readyQueue.poll();
            next_process.setState(READY);
            Config.getCPU().contextSwitch(next_process);
        }
        return prev_process;
	}
            
    
                
    public int syscall(int number, Object... varargs) {
        int result = 0;
        switch (number) {
             case MAKE_DEVICE:
                {
                    IODevice device = new IODevice((Integer)varargs[0], (String)varargs[1]);
                    Config.addDevice(device);
                }
                break;
             case EXECVE: 
                {
                    ProcessControlBlock pcb = this.loadProgram((String)varargs[0]);
                    if (pcb!=null) {
                        // Loaded successfully.
						// Now add to end of ready queue.
                        readyQueue.add(pcb);
						// If CPU idle then call dispatch.
                        if(Config.getCPU().isIdle()){
                            dispatch();
                        }
                    }
                    else {
                        result = -1;
                    }
                }
                break;
             case IO_REQUEST: 
                {
					// IO request has come from process currently on the CPU.
					// Get PCB from CPU.
                    ProcessControlBlock cpuPCB = Config.getCPU().getCurrentProcess();
					// Find IODevice with given ID: Config.getDevice((Integer)varargs[0]);
                    IODevice ioDevice = Config.getDevice((Integer)varargs[0]);
					// Make IO request on device providing burst time (varages[1]),
					// the PCB of the requesting process, and a reference to this kernel (so // that the IODevice can call interrupt() when the request is completed.
                    ioDevice.requestIO((Integer)varargs[1], cpuPCB, this);
					// Set the PCB state of the requesting process to WAITING.
                    cpuPCB.setState(WAITING);
					// Call dispatch().
                    dispatch();
                }
                break;
             case TERMINATE_PROCESS:
                {
					// Process on the CPU has terminated.
					// Get PCB from CPU.
                    ProcessControlBlock pcb = Config.getCPU().getCurrentProcess();
					// Set status to TERMINATED.
                    pcb.setState(TERMINATED);
                    // Call dispatch().
                    dispatch();
                }
                break;
             default:
                result = -1;
        }
        return result;
    }


    public void interrupt(int interruptType, Object... varargs){
        switch (interruptType) {
            case TIME_OUT:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not suppor timeouts.");
            case WAKE_UP:
				// IODevice has finished an IO request for a process.
				// Retrieve the PCB of the process (varargs[1]), set its state
                // to READY, put it on the end of the ready queue.
                ProcessControlBlockImpl pcb = (ProcessControlBlockImpl)varargs[1];
                readyQueue.addLast(pcb);
				// If CPU is idle then dispatch().
                if(Config.getCPU().isIdle()){
                    dispatch();
                }
                break;
            default:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): unknown type.");
        }
    }
    
    private static ProcessControlBlock loadProgram(String filename) {
        try {
            return ProcessControlBlockImpl.loadProgram(filename);

        }
        catch (FileNotFoundException fileExp) {
            return null;
        }
        catch (IOException ioExp) {
            return null;
        }
    }
}
