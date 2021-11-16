
public class CPU {
	
	/** The previous and active processes in the CPU. */
	private Process active, previous;
	
	/** Instantiates a new CPU */
	public CPU(){}
	
	/** Checks if there is an active process in the CPU.
	 * */
	public boolean hasProcess(){return active != null;}
	
	/**Gets the active process of the CPU.* */
	public Process getProcess(){return active;}
	
	/** Loads a new process into the CPU*/
	public void load(Process p){
		if (previous != null)
			previous.updateProgressBar(false);
		active = p;
		active.updateProgressBar(true);}
	
	/** Unload the active process of the CPU.*/
	public Process unload(){
		previous = active;
		active = null;
		return previous;}
	
	/** Performs one instruction of the active process. */
	public void step(){
		active.performInstruction();
		active.updateProgressBar(true);}
}
