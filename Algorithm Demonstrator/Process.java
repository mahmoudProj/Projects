
import java.awt.Color;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import javax.swing.JProgressBar;
import javax.swing.UIManager;


public class Process implements Cloneable{

	private String name;
	private int totalInstructions;
	private int finishedInstructions;
	private JProgressBar bar;
	Process next, prev;

	/**
	 * Instantiates a new process.
	 */
	public Process(String name, int totalIns) {
		if (name == null) throw new NullPointerException("name must not be null");
		this.name = name;
		this.totalInstructions = totalIns;
	}

	/**
	 * Creates an internal process to be used as a dummy node.
	 */
	private Process() {
		name = null;
		totalInstructions = 0;
	}

	/** Gets the name. */
	public String getName(){return name;}

	/** Gets the progress bar.*/
	public JProgressBar getBar(){
		if (bar == null) createProgressBar();
		return bar;
	}

	/** Gets the total amount of instructions in the process. */
	public int getTotal(){return totalInstructions;}

	/**
	 * Gets the amount of finished instructions.*/
	public int getFinished(){return finishedInstructions;}

	/**
	 * Checks if the process is completed. */
	public boolean isDone(){
		return finishedInstructions == totalInstructions;}

	/** Perform a single instruction of the process. */
	public void performInstruction(){
		if (!isDone())
			finishedInstructions++;
	}

	private void createProgressBar(){
		bar = new JProgressBar(0, totalInstructions);
		bar.setValue(finishedInstructions);
		UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
		bar.setString(bar.getValue()+"/"+bar.getMaximum());
		bar.setStringPainted(true);}

	protected void updateProgressBar(boolean active){
		getBar().setValue(finishedInstructions);
		//set color
		if (isDone() || !active) bar.setForeground(new Color(102,153,204));
		else bar.setForeground(new Color(102,204,204));
		//set label
		if (isDone()) bar.setString("Finished @ "+Driver.pulseCt);
		else bar.setString(finishedInstructions+"/"+totalInstructions);
		bar.repaint();
	}

	/**
	 *  Returns a clone of this process that is identical in every way
	 *  except that it has null links.
	 */
	@Override
	public Process clone(){
		Process copy = null; 
		try{
			copy = (Process) super.clone();
			copy.name = name;
			copy.totalInstructions=totalInstructions;
			copy.finishedInstructions=finishedInstructions;
			copy.next = copy.prev = null;
			copy.bar = null;
		}
		catch (CloneNotSupportedException e){
			throw new RuntimeException("forgot to make Cloneable?");
		}
		return copy;
	}

	/**
	 *  Checks for equality of this process with the parameter process.
	 *  It will check everything except for the links.
	 */
	@Override
	public boolean equals(Object other){
		if (!(other instanceof Process) || other == null) return false;
		Process p = (Process) other;

		return p.totalInstructions == totalInstructions &&
				p.finishedInstructions == finishedInstructions &&
				p.name.equals(name);
	}

	public static class Queue extends AbstractQueue<Process> implements Cloneable{

		private int manyItems;
		private int version;
		private Process dummy;


		/** Instantiates a new queue object.
		 */
		public Queue(){
			this.dummy = new Process(); //Dummy will be a new process
			dummy.prev = dummy.next = dummy; // with its links pointing to itsself
		}

		/** Adds a new process to the end of the queue*/
		@Override
		public boolean offer(Process p) {
			if(p == null ) { //Checking for nulls
				throw new NullPointerException("P is null");
			}else if(p.prev != null || p.next !=null) { //checking links, if theyre not null its not a part of this queue
				throw new IllegalArgumentException("P is in another queue");
			}
			p.prev = dummy.prev; //linking process's prev link to before the dummy
			dummy.prev.next = p; //linking the nodes behind the dummy's next to p
			p.next = dummy; //making p before dummy (end)
			dummy.prev = p; //making dummy's prev link p
			manyItems++;//added, so increment 
			version++;//changed DS, increment iterator version
			return true;
		}

		/** Add all processes from parameter queue into the back of this queue**/
		public void takeAll(Queue pq) {
			if(pq == this) { //if the parameter is the same as this,nothing happens
				return;
			}
			if(pq == null) { //if pq is null
				throw new IllegalStateException("Cannot remove null");
			}
			if(pq.manyItems == 0) { //if theres nothing to remove
				return;
			}
			pq.dummy.next.prev = dummy.prev;//making PQs first element link to the last element 
			dummy.prev.next = pq.dummy.next;//making que end link to pq
			pq.dummy.prev.next = dummy; //linking the end of pq to queues dummy
			dummy.prev = pq.dummy.prev; //making pqs last element the last element in queue
			pq.dummy.next = pq.dummy.prev = pq.dummy; // making pq dummy links point to itself

			manyItems += pq.manyItems; //incrementing count by ammount we added
			pq.manyItems = 0;//making the PQ size 0 since we removed everything from it 
			version++; //incrementing iterator version
			pq.version++;//incrementing pq iterator version
		}

		/** Returns the next process to be polled from this queue.*/
		@Override
		public Process peek(){
			Process p = dummy.next;
			if(p == dummy) {//since dummy points at itself we know if the element after the dummy is the dummy, we only have the dummy
				return null;
			}
			return p; //else, return p
		}

		/** Removes and returns the process at the start of this queue, null if empty.*/
		@Override
		public Process poll() {
			Process result = null;
			if(dummy.next == dummy) {//since dummy points at itself we know if the element after the dummy is the dummy, we only have the dummy
				return null;
			}
			result = dummy.next;//saving node to return 
			dummy.next = dummy.next.next; //skipping over result
			dummy.next.prev = dummy;//setting prev links to fully remove result
			result.next = result.prev = null;//setting result links to null to show its removal from the queue
			manyItems--;//we removed, decrement to show that
			version++;//Increment the version 
			return result;
		}


		/** Returns the number of non-dummy processes in this queue.*/
		@Override
		public int size() {
			return manyItems;//returning amount of items
		}

		/** Returns a new copy of this queue.*/
		@Override
		public Queue clone(){

			Queue copy = new Queue();

			try{ copy = (Queue) super.clone();}
			catch(CloneNotSupportedException e){
				// should not happen
			}

			Process newDummy = new Process();//newDummy
			Process prev = newDummy;//lag method

			for(Process p = dummy.next; p.next!= dummy.next; p = p.next) { // for loop going through all the elements
				Process clone = new Process(p.name, p.totalInstructions);//msking a copy of the node we are on 
				prev.next = clone;//making clone the one after the one we just copied
				clone.prev = prev;//setting the prev links of above operation 
				prev = clone;//incrementing prev
			}
			prev.next = newDummy;//after we copy we add new dummy
			newDummy.prev = prev; //setting prev links
			copy.dummy = newDummy;//making the dummy NewDummy
			copy.manyItems = manyItems;//copying over the ammount of items 
			return copy;
		}

		/** Returns a new (remove-less) iterator over this queue.*/
		@Override
		public Iterator<Process> iterator(){
			return new MyIterator();}


		private class MyIterator implements Iterator<Process>{
			private Process cursor;
			private int myVersion;
			// do not add or remove fields: only myVersion and cursor

				// Invariant for iterator:
				// 1. Outer invariant holds
					return report("something is wrong");
				}
				// Only check 2 and 3 if versions match...
				// 2. cursor is never null
				// 3. cursor is in the list
				Boolean list = false;//for checking if cursor is at tail
				if(myVersion == version) {
					for(Process sol = dummy; sol.next != dummy; sol = sol.next) {//for loop from dummy --> tail)
						if(cursor == null) { // Case 2
							return report("cursor is null");
						}else if(cursor == sol) {//if cursor is in list we are good
							list = true;
						}
					}if(list == false) {//if its not in list yet
						if(cursor != dummy.prev) {//check tail
							return report("cursor is not in list");//if not, throw
						}
					}
				}

				return true; // if nothing else is wrong, we are good
			}

			/** Instantiates a new iterator */
			public MyIterator(){
				cursor = dummy; //making cursor my dummy
				myVersion = version; // setting versions to each other

			}

			// do not change this - used for JUnit tests
			private MyIterator(boolean ignore){}


			/** Returns whether there are more processes to be returned.*/
			public boolean hasNext() {
				if(version != myVersion) { //checking versions
					throw new ConcurrentModificationException("versions dont match");
				}
				if(cursor.next != dummy) { // if the next isnt the dummy, we still have a next
					return true;
				}return false;

			}

			/** Returns the next process in this queue.  */
			public Process next() {
				if(hasNext()) {//if we have a next
					cursor =cursor.next;//then we set our cursor to it and return it 
					return cursor;
				}
				throw new NoSuchElementException();
			}
		}



	}
}
