package simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import static simulator.ProcessControlBlock.State.*;

//

/**
 * Concrete Kernel type
 *
 * @author Stephan Jamieson
 * @version 8/3/15
 */
public class FCFSKernel implements Kernel {

    //I prefer using LinkedLists
    private LinkedList<ProcessControlBlock> readyQueue = new LinkedList<ProcessControlBlock>();;

    public FCFSKernel() {
    }

    private ProcessControlBlock dispatch() {
        ProcessControlBlockImpl prev_process = (ProcessControlBlockImpl) Config.getCPU().getCurrentProcess();
        ProcessControlBlockImpl next_process;

        if(!readyQueue.isEmpty()) {
            next_process = (ProcessControlBlockImpl) readyQueue.poll();
            next_process.setState(READY);
            Config.getCPU().contextSwitch(next_process);
        }
        else{

            Config.getCPU().contextSwitch(null);
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
                ProcessControlBlock pcb = loadProgram((String)varargs[0]);
                if (pcb!=null) {
                    readyQueue.add(pcb);
                    if (Config.getCPU().isIdle()){
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
                ProcessControlBlock pcb = Config.getCPU().getCurrentProcess();
                IODevice ioDevice = Config.getDevice((Integer)varargs[0]);
                ioDevice.requestIO((Integer)varargs[1], pcb, this);
                pcb.setState(WAITING);
                dispatch();
            }
            break;
            case TERMINATE_PROCESS:
            {
                Config.getCPU().getCurrentProcess().setState(TERMINATED);
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
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not support timeouts.");
            case WAKE_UP:
                ProcessControlBlockImpl pcb = (ProcessControlBlockImpl)varargs[1];
                readyQueue.add(pcb);
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
            ProcessControlBlockImpl pcb = new ProcessControlBlockImpl(filename);
            return pcb.loadProgram(filename);
        }
        catch (FileNotFoundException fileExp) {
            return null;
        }
        catch (IOException ioExp) {
            return null;
        }
    }
}

