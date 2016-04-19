package simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Created by Erin on 19/04/2016.
 */
public class ProcessControlBlockImpl implements ProcessControlBlock {
    private String programName;
    private int priority;
    private int pid;
    private Instruction current_instruction;
    private State state;
    private LinkedList<Instruction> instructions;

    public ProcessControlBlockImpl(String filename, int pid){
        this.programName = filename;
        this.pid = pid;
    }

    public static ProcessControlBlockImpl loadProgram(String filename, int pid) throws FileNotFoundException, IOException{
        ProcessControlBlockImpl pcb = new ProcessControlBlockImpl(filename, pid);
        pcb.instructions = new LinkedList<Instruction>();
        try{
            Scanner in = new Scanner(new File(filename));
            while(in.hasNext()){
                String[] line_entries = in.nextLine().trim().split(" ");
                String begin = line_entries[0];
                if(begin.equals("#")){
                    continue;
                }
                else if(begin.equals("CPU")){
                    for(int i = 0; i<line_entries.length; i++){
                    System.out.println(line_entries[i]);}
                    pcb.instructions.add(new CPUInstruction(Integer.parseInt(line_entries[1])));
                }
                else if(begin.equals("IO")){
                    pcb.instructions.add(new IOInstruction(Integer.parseInt(line_entries[1]), Integer.parseInt(line_entries[2])));
                }
                else{
                    System.out.println("Something is wrong, this line is not parse-able: "+line_entries);
                    System.exit(1);
                }
            }
        }
        catch (FileNotFoundException fnfe){
            throw fnfe;
        }
        catch (IOException ioe){
            throw ioe;
        }
        pcb.current_instruction = pcb.instructions.getFirst();
        return pcb;
    }

    @Override
    public int getPID() {
        return this.pid;
    }

    @Override
    public String getProgramName() {
        return this.programName;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public int setPriority(int value) {
        int old_priority = this.priority;
        this.priority = value;
        return old_priority;
    }

    @Override
    public Instruction getInstruction() {
        return current_instruction;
    }

    @Override
    public boolean hasNextInstruction() {
        return instructions.isEmpty();
    }

    @Override
    public void nextInstruction() {
        if(instructions.isEmpty()){
            state = State.TERMINATED;
        }
        else{
            current_instruction = instructions.poll();
            if(current_instruction instanceof IOInstruction){
                state = State.WAITING;
            }
            else{
                state = State.READY;
            }
        }
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }
}