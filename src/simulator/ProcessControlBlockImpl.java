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
    private State state;
    private LinkedList<Instruction> instructions = new LinkedList<Instruction>();

    public ProcessControlBlockImpl(String filename, int pid){
        this.programName = filename;
        this.pid = pid;
    }

    @Override
    public String toString(){
        return "process(pid="+this.getPID()+", state="+this.getState()+", name=\""+this.getProgramName()+"\")";
    }

    public static ProcessControlBlockImpl loadProgram(String filename, int pid) throws FileNotFoundException, IOException{
        ProcessControlBlockImpl pcb = new ProcessControlBlockImpl(filename, pid);

        try{
            Scanner in = new Scanner(new File(filename));

            while(in.hasNext()){
                String line = in.nextLine();
                char begin = line.charAt(0);
                if (begin == '#') {
                    continue;
                }
                else{
                    String[] line_entries = line.trim().split(" ");
                    String first_word = line_entries[0];
                    if(first_word.equalsIgnoreCase("CPU")){
                        pcb.instructions.add(new CPUInstruction(Integer.parseInt(line_entries[1])));
                    }
                    else if(first_word.equalsIgnoreCase("IO")){
                        pcb.instructions.add(new IOInstruction(Integer.parseInt(line_entries[1]), Integer.parseInt(line_entries[2])));
                    }
                    else{
                        System.out.println("Something is wrong, this line is not parse-able: "+line_entries);
                        System.exit(1);
                    }

                }
            }
            return pcb;
        }
        catch (FileNotFoundException fnfe){
            throw fnfe;
        }
        catch (IOException ioe){
            throw ioe;
        }
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
        return instructions.peek();
    }

    @Override
    public boolean hasNextInstruction() {
        return instructions.isEmpty();
    }

    @Override
    public void nextInstruction() {
        instructions.poll();
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
