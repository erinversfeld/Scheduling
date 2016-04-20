package simulator; 
/**
 * Interface for handling the processing of events
 * @author Stephan Jamieson
 * @version 8/3/15
 */
interface EventHandler<E extends Event> {

    void process(E event);
}
