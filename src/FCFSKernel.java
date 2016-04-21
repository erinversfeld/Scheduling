import simulator.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import static simulator.ProcessControlBlock.State.*;

//

/**
 * Concrete Kernel type for the execution of a first come first serve simulation of process scheduling in an operating system.
 * @author Stephan Jamieson
 * @version 8/3/15
 */
public class FCFSKernel implements Kernel {

    //I prefer using LinkedLists
    private LinkedList<ProcessControlBlock> readyQueue = new LinkedList<ProcessControlBlock>();;

    public FCFSKernel() {
    }

    private ProcessControlBlock dispatch() {
        CPU cpu = Config.getCPU();
        ProcessControlBlock prev_process = cpu.getCurrentProcess();
        ProcessControlBlock next_process;

        if(!readyQueue.isEmpty()) {
            next_process = readyQueue.poll();
            next_process.setState(READY);
            cpu.contextSwitch(next_process);
        }
        else{

            cpu.contextSwitch(null);
        }
        return prev_process;

    }

    public int syscall(int number, Object... varargs) {
        CPU cpu = Config.getCPU();
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
                ProcessControlBlock pcb = loadProgram((String)varargs[0]);
                if (pcb!=null) {
                    readyQueue.add(pcb);
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
                ProcessControlBlock pcb = cpu.getCurrentProcess();
                //get the appropriate ioDevice
                IODevice ioDevice = Config.getDevice((Integer)varargs[0]);
                //process the ioRequest
                ioDevice.requestIO((Integer)varargs[1], pcb, this);
                pcb.setState(WAITING);
                dispatch();
            }
            break;
            case TERMINATE_PROCESS:
            {
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

    public void interrupt(int interruptType, Object... varargs){
        CPU cpu = Config.getCPU();
        switch (interruptType) {
            case TIME_OUT:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not support timeouts.");
            case WAKE_UP:
                //huh, who would've known that you would have to cast an object to a pcb...
                ProcessControlBlock pcb = (ProcessControlBlock)varargs[1];
                //put the process back onto the readyQ
                readyQueue.add(pcb);
                if(cpu.isIdle()){
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

