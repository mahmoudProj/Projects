
import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class NewSong extends AbstractCollection<Note> implements Cloneable {

	/** Static Constants */
	private static final String DEFAULT_NAME = "Untitled";
	private static final int DEFAULT_BPM = 60;
	private static final int INITIAL_CAPACITY = 1;
	public static final int MIN_BPM = 20, MAX_BPM = 1000;

	/** Fields */
	private String name;
	private int bpm;
	private Note[ ] data;
	private int manyItems;
	private int version;


	public NewSong( )
	{
		this(DEFAULT_NAME, DEFAULT_BPM, INITIAL_CAPACITY);//Initializing 
	}
	
	public NewSong(String name, int bpm)
	{
		this(name, bpm, INITIAL_CAPACITY); //Initializing 
	}
	
	public NewSong(String n, int beats, int initialCapacity)
	{
		if (n == null) {
			throw new IllegalArgumentException("Name is null"); //checking parameters 
		}

		if(beats < MIN_BPM || beats > MAX_BPM) { //checking parameters 
			throw new IllegalArgumentException("bpm is out of range");
		
		}
		if(initialCapacity<0) {//checking parameters 
			throw new OutOfMemoryError("INitail capacity invalid");
		}
		
		this.name = n; //Initializing 
		this.bpm = beats; //Initializing 
		this.data = new Note[initialCapacity]; //Initializing 
		manyItems = 0; //Initializing 
	}

	public String getName() {
		return name; //returning the name
	}

	
	public int getBPM() {
		return bpm;//returning the bpm
	}
	
	public Note[] getData() {
		return this.data; //returning the data
	}
	
	public double getDuration() {
		double result = 0; //new var for the sum
		for(int i = 0; i<manyItems; i++) { //for loop that adds all of the notes' duration into the result var
			if(this.data[i] != null) { //as long as the duration is not null
			result += this.data[i].getDuration(); //add the duration
			}
		}
		return result; //return the sum
	}

	public void stretch(double factor) {
		for(int i = 0; i<manyItems; i++) { //for loop going through all elements, stretching each one by the param
			if(this.data[i] != null) {
				data [i] = data[i].stretch(factor);
			}
		}
	}
	
	public void transpose(int interval) {
		for (int i = 0; i < manyItems; i++) { //for loop going through all elements, transposing each one by the param
			if (data[i] != null) { //checking conditions
				data[i] = data[i].transpose(interval); //transposing the element in the data at I
				}
			}
	}
	
	@Override //ovverriden due to the abstract collection not having add
	public int size( )
	{
		return manyItems; //returning the amount of elements that are present which happens to me manyItems

	}
	
	@Override //if we dont override itll just throw exceptions
	public boolean add(Note element) { //adding an element to the end 
		ensureCapacity(manyItems +1); //checking the size to make sure that there is enough space for the added element

		data[manyItems] = element; // making the last index the element 
		manyItems++;//increment item counter
		version++;//increment version because we are changing the data structure 
		return true;//return true if the element was added
		
	}
	
	public boolean remove (Note element) {
		boolean removed = false; //creating a boolean variable to keep track of if we removed something
		if(manyItems <= 0) { //making sure that the array has something for us to remove 
			return false; //if it doesnt we return false
		}
		
		if(manyItems == 1) { //If there is only one element
			if(data[0].equals(element)) {//and it is the element we want to remove
				data = new Note[1]; // we simply create a new array with one empty index
				manyItems--; //decrement item counter
				return true; // return true to say we removed
			}return false;//return false to say we didn't remove becuase the element is not the one we want
		}
		
		Note [] sol = new Note[manyItems];// new Note array with the length of the count of items
		if(element == null) { //if the element we want to remove is null 
			int j = 0; // j variable that is used to copy over data to a new array skipping an element 
			for(int i = 0; i<manyItems; i++) { //for loop going over all of the notes
				if(data[i] == null && removed == false) { //if we havent removed yet and the data at an index is null
					removed = true; //signifies that the element has been removed
					continue;//continue with the code, we only want to skip it once
				}
				sol[j++] = data[i]; //copying over while skipping
			}
			data = sol; //keeping new array in old array for param
			if(removed == true) {//if we removed
				manyItems--; //we have less items
				version++;//new version because we changed the data structure 
			}
			return removed;//return if we removed
		}
		int k = 0;//variable that is used to copy over data to a new array skipping an element 
		for(int i = 0; i<manyItems; i++) { //for loop to increment through array
			if(data[i].equals(element)&& removed == false) { //if the element at index i is the element that we want
				
				removed = true; //make boolean var true to say we removed
				continue;//continue with the code, we only want to skip it once
				 
			}sol[k++] = data[i];//we increment k to skip that element
		}data = sol;//keeping new array in old array for param
		if(removed == true) { //if we removed we want to 
			manyItems--; //decrement the item count
			version++;	//increment version because we are changing the data structure 
		}
		return removed;//return the boolean
	}
	
	private void ensureCapacity(int minimumCapacity)
	{
		// NB: do not check invariant
		if(minimumCapacity<=this.data.length) {  //smaller or equal to the current capacity (do nothing)
			return;
		}
		int newSize = data.length*2; //size *2
		if(newSize < minimumCapacity) { 
			newSize = minimumCapacity; //if newSize in smaller than the minCapacity, newsize = min
		}
		Note [] sol = new Note [newSize]; //new note array with new size
		for(int i = 0; i<manyItems; i++) { //copying over
			sol[i] = data[i];
		}
		data = sol;//setting data to new array 
	}
	
	public NewSong clone( ) { 
		NewSong answer; // NewSong var to hold our clone
		try
		{
			answer = (NewSong) super.clone( );//try to clone
		}
		catch (CloneNotSupportedException e)//if it fails catch the exception
		{  
			throw new RuntimeException("This class does not implement Cloneable");
		}
		answer.data = data.clone( ); // making the clone
		return answer; //returning clone
	}
	
	
	private class NotACounter implements Iterator<Note>{
		
		private int currentIndex; //var to keep track of where we are in the structure 
		private int versionn = version;//making the new version the old one in order to keep them the same 
		boolean isCurrent; //var to let me know if there is a current element or not 
		
		public NotACounter() { //contructor to set the current index -1 (this allows us to know if next is called)
			currentIndex = -1;
		}
		
		@Override //overridden because main classs implements cloneable
		public boolean hasNext() {
			if(versionn == version) { //if the versions of the iterator are the same 
				NotACounter sol = new NotACounter();//making a new iterator
				sol.currentIndex = currentIndex;//setting the iterator to where we are now 
				if(sol.currentIndex+1 < manyItems) {//and there is an element in the next index
					return true;
				}return false;
			} throw new ConcurrentModificationException("the versions are not the same");
			
		}
		
		@Override 
		public Note next() {//overridden because main classs implements cloneable
			Note temp;
			if(version != versionn) {
				throw new ConcurrentModificationException("the versions are not the same");
			}
			if(hasNext()) { //if there is a next element
				++currentIndex;
				isCurrent = true; //make currentElement true to signify that there is a currentElement 
				temp = data[currentIndex]; //return that element 
				
			}else {
			throw new NoSuchElementException("There is no next element"); // if there is no next, throw	
			}return temp;
			
		}
		
		@Override
		public void remove() {//overridden because main classs implements cloneable
			if(versionn != version) { //if the versions of the iterator are the same 
				throw new ConcurrentModificationException("the versions are not the same");
			}
			if(currentIndex == -1 || currentIndex >= manyItems-1) { //checking if next has been called and if there is enough space
				throw new IllegalStateException("cannot remove element from bag"); // if not throw 
			}
			if(isCurrent) {
				NotACounter sol = new NotACounter();//making a new iterator
				sol.currentIndex = currentIndex;//setting the iterator to where we are now 
				for(int i = sol.currentIndex; i<manyItems; i++) {//starting at the currentIndex (which we want to delete), until the end
					if(sol.hasNext()) {//if there is a next element
						data[i] = sol.next();//make it the current index and keep doing so in order to make the element disappear through writing over it
					}
				}
				manyItems--;//decrement item count
				version++;//increment version because we are changing the data structure 
				versionn++;//increment version because we are changing the data structure 
				currentIndex--;
				isCurrent = false; //there is no current element 
			}else {
				throw new IllegalStateException("cannot remove element from bag"); // if not throw
			}
			
			
		}
		
	}
	
	public Iterator<Note> iterator(){
		return new NotACounter();
	}
	
}
