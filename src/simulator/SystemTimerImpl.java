package simulator;

/**
 * Created by Erin on 18/04/2016.
 */
public class SystemTimerImpl implements SystemTimer {
    private long systemTime;
    private long idleTime;
    private long userTime;
    private long kernelTime;
    private EventQueue eventQueue;
    TimeOutEvent timeOutEvent;

    /**
     * Constructor method
     * @param eventQueue The system timer requires a reference to the event queue to be able to schedule interrupts
     */
    public SystemTimerImpl(EventQueue eventQueue){
        this.eventQueue = eventQueue;
        systemTime = 0;
        idleTime = 0;
        userTime = 0;

    }

    private void advanceSystemTime(long time){
        systemTime += time;
    }

    private void advanceIdleTime(long time){
        idleTime += time;
        advanceSystemTime(time);
    }

    private void advanceUserTime(long time){
        userTime += time;
        advanceSystemTime(time);
    }

    private void advanceKernelTime(long time){
        kernelTime += time;
        advanceSystemTime(time);
    }

    @Override
    public long getSystemTime() {
        return systemTime;
    }

    @Override
    public long getIdleTime() {
        return idleTime;
    }

    @Override
    public long getUserTime() {
        return userTime;
    }

    @Override
    public long getKernelTime() {
        return kernelTime;
    }

    @Override
    public void scheduleInterrupt(int timeUnits, InterruptHandler handler, Object... varargs) {
        //if a timeout has already been scheduled, cancel it
        //this helps keep a limit on the number of interrupts being scheduled
        if(timeOutEvent!=null){
            cancelInterrupt(timeOutEvent.getProcessID());
        }
        //TODO:set the new timeout event
    }

    @Override
    public void cancelInterrupt(int pid) {
        //TODO: implement so that it uses the pid instead
        eventQueue.remove(timeOutEvent);
    }
}
