import simulator.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import static simulator.ProcessControlBlock.State.*;

/**
 * Concrete Kernel type
 *
 * @author Stephan Jamieson
 * @version 8/3/15
 */
public class FCFSKernel implements Kernel {

    //I prefer using LinkedLists
    private LinkedList<ProcessControlBlock> readyQueue = new LinkedList<ProcessControlBlock>();;

    /**
     * Constructor method for an FCFS process-handling kernel
     */
    public FCFSKernel() {
    }

    /**
     * Puts a new process onto the eventQ, performing a context switch to do so
     * @return the process taken off of the q
     */
    private ProcessControlBlock dispatch() {
        CPU cpu = Config.getCPU();
        ProcessControlBlockImpl prev_process = (ProcessControlBlockImpl) cpu.getCurrentProcess();
        ProcessControlBlockImpl next_process;

        if(!readyQueue.isEmpty()) {
            next_process = (ProcessControlBlockImpl) readyQueue.poll();
            next_process.setState(READY);
            cpu.contextSwitch(next_process);
        }
        else{

            cpu.contextSwitch(null);
        }
        return prev_process;

    }

    /**
     * Kernel handler method for dealing with system calls
     * @param number specifies an enum which in turns specifies which syscall is being made
     * @param varargs other argumens which the syscall may or may not require
     * @return -1 for failure, 0 for success
     */
    public int syscall(int number, Object... varargs) {
        CPU cpu = Config.getCPU();
        int result = 0;
        switch (number) {
            case MAKE_DEVICE:
            {
                //create a new IO device with the specified deviceID and name
                IODevice device = new IODevice((Integer)varargs[0], (String)varargs[1]);
                //add the device to the list of those attached to the system
                Config.addDevice(device);
            }
            break;
            case EXECVE:
            {
                //load the program's instructions into a pcb
                ProcessControlBlock pcb = loadProgram((String)varargs[0]);
                if (!pcb.equals(null)) {
                    //add the loaded pcb onto the readyQ
                    readyQueue.add(pcb);
                    //if there isn't a program in execution, execute
                    if (cpu.isIdle()){
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
                //get device and pcb
                ProcessControlBlock pcb = cpu.getCurrentProcess();
                IODevice ioDevice = Config.getDevice((Integer)varargs[0]);
                //kick off the activity on the device
                ioDevice.requestIO((Integer)varargs[1], pcb, this);
                pcb.setState(WAITING);
                //send the progam for execution
                dispatch();
            }
            break;
            case TERMINATE_PROCESS:
            {
                //get the current process, set its status to terminated and execute it
                ProcessControlBlock pcb = cpu.getCurrentProcess();
                pcb.setState(TERMINATED);
                dispatch();
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
        switch (interruptType) {
            case TIME_OUT:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not support timeouts.");
            case WAKE_UP:
                //get the pcb from varags, add it to the ReadyQ and execute if something isn't executing right now
                ProcessControlBlock pcb = (ProcessControlBlock)varargs[1];
                readyQueue.add(pcb);
                if(cpu.isIdle()){
                    dispatch();
                }
                break;
            default:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): unknown type.");
        }
    }

    /**
     * Helper method for constructing a pcb with a program's instructions loaded into it
     * @param filename the name of the program who's instructions are to be loaded
     * @return a pcb representing the program
     */
    private static ProcessControlBlock loadProgram(String filename) {
        try {
            //returns a pcb with a program loaded into it
            return ProcessControlBlockImpl.loadProgram(filename);
        }
        catch (FileNotFoundException fileExp) {
            return null;
        }
        //isn't actually thrown as I'm using Scanner and File, but is required as part of the framework
        catch (IOException ioExp) {
            return null;
        }
    }
}
