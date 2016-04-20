package simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Erin on 4/18/2016.
 */
public class ProcessControlBlockImpl implements ProcessControlBlock {
    private static int PID = 1;
    private String programName;
    private int priority = -1;
    private Instruction curr_instruction;
    private LinkedList<Instruction> instructions  = new LinkedList<Instruction>();
    private int program_counter= 0;
    private State state;

    public ProcessControlBlockImpl (String name){
        this.PID = PID;
        programName = name;
    }

    public void start(){
        curr_instruction = instructions.get(program_counter);
    }

    public void add (Instruction in){
        instructions.add(in);
    }

    /**
     * Obtain process ID.
     */
    @Override
    public int getPID() {
        return PID;
    }

    /**
     * Obtain program name.
     *
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
     * Requires <code>getState()!=State.TERMINATED</code>.
     */
    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public String toString(){
        return "process(pid="+this.getPID()+", state="+this.getState()+", name=\""+this.getProgramName()+"\")";}


    public static ProcessControlBlock loadProgram(String filename) throws FileNotFoundException, IOException{
        ProcessControlBlockImpl pcb = new ProcessControlBlockImpl (filename);
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;

            while ((line=br.readLine())!= null) {
                char begin = line.charAt(0);
                //make sure the line isn't a comment or an empty line
                if (begin!='#'&&String.valueOf(begin)!=String.valueOf("")){
                    String[] line_entries = line.trim().split(" ");
                    //check if  CPU or IO instruction
                    if(line_entries[0].equalsIgnoreCase("CPU")){
                        int duration = Integer.parseInt(line_entries[1]);
                        pcb.add(new CPUInstruction(duration));
                    }else if(line_entries[0].equalsIgnoreCase("IO")){
                        int duration = Integer.parseInt(line_entries[1]);
                        int deviceID = Integer.parseInt(line_entries[2]);
                        IOInstruction tempInstruction = new IOInstruction(duration,deviceID);
                        pcb.add(tempInstruction);
                    }
                }
            }

        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException ie) {
            throw ie;
        }
        PID++;
        pcb.start();
        return pcb;
    }
}
