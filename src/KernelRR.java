import simulator.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import static simulator.ProcessControlBlock.State.*;

//

/**
 * Concrete Kernel type for the execution of a round robin simulation of process scheduling in an operating system.
 */
public class KernelRR implements Kernel {
    //I prefer using LinkedLists
    private LinkedList<ProcessControlBlock> readyQueue = new LinkedList<ProcessControlBlock>();;
    private int slice;

    public KernelRR(int slice) {
        this.slice = slice;
    }

    private ProcessControlBlock dispatch() {
        CPU cpu = Config.getCPU();
        ProcessControlBlockImpl prev_process = (ProcessControlBlockImpl) cpu.getCurrentProcess();
        ProcessControlBlock next_process;

        //Checks if the ready queue isn't empty
        if(!readyQueue.isEmpty()) {
            next_process = readyQueue.poll();
            //Switch cpu content to next setup
            cpu.contextSwitch(next_process);
            //Set state to running
            next_process.setState(RUNNING);
        }else{
            cpu.contextSwitch(null);
        }
        return prev_process;
	}

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
                    ProcessControlBlock pcb = loadProgram((String)varargs[0]);
                    if (!pcb.equals(null)) {
                        pcb.setPriority((Integer)varargs[1]);
                        readyQueue.add(pcb);
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
                    ProcessControlBlock pcb = cpu.getCurrentProcess();
                    IODevice ioDevice = Config.getDevice((Integer)varargs[0]);
                    ioDevice.requestIO((Integer)varargs[1], pcb, this);
                    pcb.setState(WAITING);
                    if(!pcb.equals(null)){
                        int pid = pcb.getPID();
                        systemTimer.cancelInterrupt(pid);
                    }
                    dispatch();
                    if(cpu.getCurrentProcess()!=null){
                        int pid = cpu.getCurrentProcess().getPID();
                        systemTimer.scheduleInterrupt(slice, this, pid);
                    }
                }
                break;
            case TERMINATE_PROCESS:
                {
                    ProcessControlBlock pcb = (ProcessControlBlock)cpu.getCurrentProcess();
                    pcb.setState(TERMINATED);
                    if(!pcb.equals(null)){
                        systemTimer.cancelInterrupt(pcb.getPID());
                    }
                    dispatch();
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

    public void interrupt(int interruptType, Object... varargs){
        CPU cpu = Config.getCPU();
        SystemTimer systemTimer = Config.getSystemTimer();

        switch (interruptType) {
            case TIME_OUT:
                if(!Config.getCPU().isIdle()){
                    ProcessControlBlock pcb = (ProcessControlBlock)cpu.getCurrentProcess();
                    pcb.setState(READY);
                    readyQueue.add(pcb);
                    cpu.contextSwitch(readyQueue.poll());
                    ProcessControlBlock curr = cpu.getCurrentProcess();
                    curr.setState(RUNNING);
                    int pid = curr.getPID();
                    systemTimer.scheduleInterrupt(slice, this, pid);
                }
                break;
            case WAKE_UP:
                ProcessControlBlock pcb = (ProcessControlBlock)varargs[1];
                readyQueue.add(pcb);
                pcb.setState(READY);
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
