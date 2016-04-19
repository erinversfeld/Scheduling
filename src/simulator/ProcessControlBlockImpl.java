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
    private LinkedList<Instruction> instructions = new LinkedList<Instruction>();

    public ProcessControlBlockImpl(int pid){
        this.pid = pid;
    }

    public ProcessControlBlockImpl loadProgram(String filename) throws FileNotFoundException, IOException{
        try{
            Scanner in = new Scanner(new File(filename));
            this.programName = in.nextLine().replace("#", "").trim();

            while(in.hasNext()){
                String[] line_entries = in.nextLine().trim().split(" ");
                String begin = line_entries[0];
                if(begin.equals("#")){
                    continue;
                }
                else if(begin.equalsIgnoreCase("CPU")){
                    for(int i = 0; i<line_entries.length; i++){
                    System.out.println(line_entries[i]);}
                    this.instructions.add(new CPUInstruction(Integer.parseInt(line_entries[1])));
                }
                else if(begin.equalsIgnoreCase("IO")){
                    this.instructions.add(new IOInstruction(Integer.parseInt(line_entries[1]), Integer.parseInt(line_entries[2])));
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
        return this;
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
