package simulator;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Created by Erin on 4/18/2016.
 */
public class ProcessControlBlockImpl implements ProcessControlBlock {
    private static int pid = 0;
    private String programName;
    private int priority = -1;
    private Instruction curr_instruction;
    private LinkedList<Instruction> instructions  = new LinkedList<Instruction>();
    private int program_counter= 0;
    private State state;

    public ProcessControlBlockImpl (String name){
        this.pid = pid;
        programName = name;
    }

    /**
     * Obtain process ID.
     */
    @Override
    public int getPID() {
        return pid;
    }

    /**
     * Obtain program name.
     */
    @Override
    public String getProgramName() {
        return programName;
    }

    /**
     * Obtain process priority();
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * Set process priority(), returning the old value.
     */
    @Override
    public int setPriority(int value) {
        int temp = priority;
        priority = value;
        return temp;
    }

    /**
     * Obtain current program 'instruction'.
     */
    @Override
    public Instruction getInstruction() {
        return curr_instruction;
    }

    /**
     * Determine if there are any more instructions.
     */
    @Override
    public boolean hasNextInstruction() {
        if(instructions.size()> program_counter +1){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Advance to next instruction.
     */
    @Override
    public void nextInstruction() {
        if(hasNextInstruction()) {
            program_counter++;
            curr_instruction = instructions.get(program_counter);
        }
        else{
            curr_instruction = null;
        }
    }

    /**
     * Obtain process state.
     */
    @Override
    public State getState() {
        return state;
    }

    /**
     * Set process state.
     * Requires getState()!=State.TERMINATED.
     */
    @Override
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Obtain a string representation of a pcb
     * @return String representation of the process according to the spec given in the design brief
     */
    @Override
    public String toString(){
        return "process(pid="+this.getPID()+", state="+this.getState()+", name=\""+this.getProgramName()+"\")";}


    /**
     * Load a program's instructions into a pcb
     * @param filename the name of the program to be loaded
     * @return the pcb with the loaded instructions
     * @throws FileNotFoundException thrown by the scanner if the program file does not exist
     * @throws IOException
     */
    public static ProcessControlBlock loadProgram(String filename) throws FileNotFoundException, IOException{
        ProcessControlBlockImpl pcb = new ProcessControlBlockImpl (filename);
        try {
            Scanner input = new Scanner(new File(filename));
            String line;

            while (input.hasNext()) {
                line = input.nextLine();
                char begin = line.charAt(0);
                //make sure the line isn't a comment or an empty line
                if (begin!='#'&&String.valueOf(begin)!=String.valueOf("")){
                    String[] line_entries = line.trim().split(" ");
                    //check if  CPU or IO instruction
                    if(line_entries[0].equalsIgnoreCase("CPU")){
                        int duration = Integer.parseInt(line_entries[1]);
                        pcb.instructions.add(new CPUInstruction(duration));
                    }else if(line_entries[0].equalsIgnoreCase("IO")){
                        int duration = Integer.parseInt(line_entries[1]);
                        int deviceID = Integer.parseInt(line_entries[2]);
                        IOInstruction tempInstruction = new IOInstruction(duration,deviceID);
                        pcb.instructions.add(tempInstruction);
                    }
                }
            }

        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException ie) {
            throw ie;
        }
        //polish up the pcb before returning
        pid++;
        pcb.curr_instruction = pcb.instructions.get(pcb.program_counter);
        return pcb;
    }
}
