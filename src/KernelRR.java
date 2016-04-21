import simulator.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import static simulator.ProcessControlBlock.State.*;

/**
 * Concrete Kernel type for the execution of a round robin simulation of process scheduling in an operating system.
 */
public class KernelRR implements Kernel {
    //I prefer using LinkedLists
    private LinkedList<ProcessControlBlock> readyQueue = new LinkedList<ProcessControlBlock>();;
    private int slice;

    /**
     * Constructor method for KernelRR
     * @param slice
     */
    public KernelRR(int slice) {
        this.slice = slice;
    }

    /**
     * Puts a new process onto the eventQ, performing a context switch to do so
     * @return the process taken off of the q
     */
    private ProcessControlBlock dispatch() {
        CPU cpu = Config.getCPU();
        ProcessControlBlockImpl prev_process = (ProcessControlBlockImpl) cpu.getCurrentProcess();
        ProcessControlBlock next_process;

        //Checks if the ready queue isn't empty
        if(!readyQueue.isEmpty()) {
            next_process = readyQueue.poll();
            next_process.setState(READY);
            //Switch cpu content to next setup
            cpu.contextSwitch(next_process);
        }else{
            cpu.contextSwitch(null);
        }
        return prev_process;
	}

    /**
     * Puts a new process onto the eventQ, performing a context switch to do so
     * @return the process taken off of the q
     */
    public int syscall(int number, Object... varargs) {
        int result = 0;
        CPU cpu = Config.getCPU();
        SystemTimer systemTimer = Config.getSystemTimer();
        switch (number) {
            case MAKE_DEVICE:
                {
                    IODevice device = new IODevice((Integer)varargs[0], (String)varargs[1]);
                    Config.addDevice(device);
                }
                break;
            case EXECVE:
                {
                    //load the program into a pcb
                    ProcessControlBlock pcb = loadProgram((String)varargs[0]);
                    if (!pcb.equals(null)) {
                        //set the priority of the pcb according to the spec given in the config file
                        pcb.setPriority((Integer)varargs[1]);
                        readyQueue.add(pcb);
                        //execute and schedule a time to pause/halt the execution
                        if (cpu.isIdle()){
                            dispatch();
                            int pid = cpu.getCurrentProcess().getPID();
                            systemTimer.scheduleInterrupt(slice, this, pid);
                        }
                    }
                    else {
                        result = -1;
                    }
                }
                break;
            case IO_REQUEST:
                {
                    //starts off the same as in fcfs
                    ProcessControlBlock pcb = cpu.getCurrentProcess();
                    IODevice ioDevice = Config.getDevice((Integer)varargs[0]);
                    ioDevice.requestIO((Integer)varargs[1], pcb, this);
                    pcb.setState(WAITING);
                    //difference comes in here: we cancel any forthcoming interrupts
                    if(!pcb.equals(null)){
                        int pid = pcb.getPID();
                        systemTimer.cancelInterrupt(pid);
                    }
                    //then begin execution and schedule our own one to happen
                    dispatch();
                    if(cpu.getCurrentProcess()!=null){
                        int pid = cpu.getCurrentProcess().getPID();
                        systemTimer.scheduleInterrupt(slice, this, pid);
                    }
                }
                break;
            case TERMINATE_PROCESS:
                {
                    //starts off like fcfs
                    ProcessControlBlock pcb = (ProcessControlBlock)cpu.getCurrentProcess();
                    pcb.setState(TERMINATED);
                    //then we act as we would in io_request
                    if(!pcb.equals(null)){
                        systemTimer.cancelInterrupt(pcb.getPID());
                    }
                    dispatch();
                    //the difference being here, where we schedule a new interrupt if there are more processes
                    // still to share time slices with, otherwise it carries on running on of its own accord
                    if (cpu.getCurrentProcess() != null){
                        int pid = cpu.getCurrentProcess().getPID();
                        systemTimer.scheduleInterrupt(slice, this, pid);
                    }
                }
                break;
             default:
                result = -1;
        }
        return result;
    }

    /**
     * Kernel handler method for interrupts
     * @param interruptType a representation of an enum which indicates which type of interrupt has occurred
     * @param varargs other argumens which the syscall may or may not require
     */
    public void interrupt(int interruptType, Object... varargs){
        CPU cpu = Config.getCPU();
        SystemTimer systemTimer = Config.getSystemTimer();

        switch (interruptType) {
            case TIME_OUT:
                if(!cpu.isIdle()){
                    ProcessControlBlock pcb = (ProcessControlBlock)cpu.getCurrentProcess();
                    readyQueue.add(pcb);
                    cpu.contextSwitch(readyQueue.poll());
                    ProcessControlBlock curr = cpu.getCurrentProcess();
                    int pid = curr.getPID();
                    systemTimer.scheduleInterrupt(slice, this, pid);
                }
                break;
            case WAKE_UP:
                ProcessControlBlock pcb = (ProcessControlBlock)varargs[1];
                readyQueue.add(pcb);
                if(cpu.isIdle()){
                    dispatch();
                    int pid = cpu.getCurrentProcess().getPID();
                    systemTimer.scheduleInterrupt(slice, this, pid);
                }
                break;
            default:
                throw new IllegalArgumentException("KernelRR:interrupt("+interruptType+"...): unknown type.");
        }
    }

    /**
     * Helper method for constructing a pcb with a program's instructions loaded into it
     * @param filename the name of the program who's instructions are to be loaded
     * @return a pcb representing the program
     */
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
