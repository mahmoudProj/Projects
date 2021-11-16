
import Process.Queue;

public class Scheduler{

	private CPU cpu;
	private boolean roundRobin;
	private Queue ready, done;

	/** Instantiates a new scheduler with a boolean denoting type.	 */
	public Scheduler(boolean rr){
		cpu = new CPU();
		ready = new Queue();
		done = new Queue();
		roundRobin = rr;}

	/** Schedules a new process in the ready queue.	 */
	public void schedule(Process p){ready.offer(p);}

	/** Schedules all processes from the parameter queue into the ready queue.	 */
	public void scheduleAll(Queue pq){ready.takeAll(pq);}

	/** Gets the ready queue.	 */
	public Queue getReadyQueue(){return ready;}

	/** Gets the done queue.	 */
	public Queue getDoneQueue(){return done;}

	/** Gets the CPU.	 */
	public CPU getCPU(){return cpu;}

	/** Checks if there are any processes left in the ready queue.	 */
	public boolean isDone(){return ready.isEmpty() && !cpu.hasProcess();}

	/** Executed at every pulse of the driver's clock.
	 *  If the CPU has no process and there are some left in this
	 *  scheduler's ready queue, load the CPU with a process.
	 *  Call step on the CPU.
	 *  If the process in the CPU is done, then offer it to the
	 *  done queue. Otherwise, if this is a Round Robin scheduler,
	 *  unload it from the CPU and offer it back to ready queue.
	 */
	public void step(){

		if(cpu.hasProcess()) { //if theres a process in the cpu
			cpu.step();//Execute it 
		}else {
			cpu.load(ready.poll()); //if not, remove from ready and load into cpu
			cpu.step();//Execute it 
		}
		//have a 
		if(roundRobin) { //if we have a round robin 
			if(cpu.getProcess().isDone()) { //and the process in the CPU is done
				done.offer(cpu.unload()); //unload from CPU and move to done
			}else {
				ready.offer(cpu.unload());//if its not done offer it to the ready again 
			}

		}else {
			if(cpu.getProcess().isDone()) { // if not a rr and process is done 
				done.offer(cpu.unload());//unload from CPU and move to done
			}
		}
	}
}
