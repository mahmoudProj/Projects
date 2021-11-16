import java.awt.Color;
import java.util.NoSuchElementException;

public class Triangle {
	private Point3D p1, p2, p3;
	private final Color color;
	
	/**
	 * Return a triangle with the given points and color.
	 */
	public Triangle(Point3D p1, Point3D p2, Point3D p3, Color c) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		color = c;
	}
	
	/**
	 * Return a point of the triangle.
	 */
	public Point3D getPoint(int i) {
		switch (i) {
		case 0: return p1;
		case 1: return p2;
		case 2: return p3;
		default: throw new IllegalArgumentException("getPoint(" + i + ") requires [0,3)");
		}
	}
	
	/**
	 * Get the base (undimmed) color of this triangle.
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Get the largest Y value of any point on the triangle.
	 */
	public double getMaxY() {
		return Math.max(p1.y, Math.max(p2.y,p3.y));
	}
	
	/**
	 * Get the smallest Y value of any point on the triangle.

	 */
	public double getMinY() {
		return Math.min(p1.y, Math.min(p2.y,p3.y));
	}
	
	private static double interpolate(double x0, double x1, double y0, double y1, double x) {
		return (y0 * (x1 - x) + y1 * (x - x0))/(x1 - x0);
	}
	
	private double minX, maxX;
	private double firstZ, lastZ;
	
	private boolean checkLine(double scanY, Point3D a, Point3D b, boolean started) {
		double da = a.y - scanY;
		double db = b.y - scanY;
		if (da < 0 && db < 0 || da > 0 && db > 0) return started;
		double x = interpolate(a.y,b.y,a.x,b.x,scanY);
		double z = interpolate(a.y,b.y,a.z,b.z,scanY);
		/*if (Double.toString(scanY).endsWith("0.0")) {
			System.out.println("For Y = " + scanY + ", a = " + a + ", b = " + b + ", x = " + x + ", z = " + z);
		}*/
		if (!started) {
			minX = maxX = x;
			firstZ = lastZ = z;
		} else if (x < minX) {
			minX = x; firstZ = z;
		} else if (x > maxX) {
			maxX = x; lastZ = z;
		}
		return true;
	}
	
	/**
	 * Set the current scan line.
	 */
	public boolean setScan(double y) {
		return checkLine(y,p1,p2,checkLine(y,p2,p3,checkLine(y,p3,p1,false)));
	}
	
	/**
	 * Return the minimum x value of this triangle along the current scan line.
	 */
	public double getMinX() { return minX; }
	
	/**
	 * Return the maximum x value of this triangle along the current scan line.
	 */
	public double getMaxX() { return maxX; }
	
	/**
	 * Given an X coordinate on the (implicit) scan Y, return the Z coordinate
	 * of this triangle (the closest point)
	 */
	public double getZ(double x) {
		if (minX == maxX) return firstZ;
		return interpolate(minX,maxX,firstZ,lastZ,x);
	}
	
	/**
	 * Return color for this triangle moving toward the background color.
	 */
	public Color getColor(double x, Color background) {
		float z = (float)getZ(x);
		if (z <= 0.0) return color;
		if (z >= 1.0) return background;
		float m = 1-(float)z;
		int r = (int)(color.getRed() * m + background.getRed() * z);
		int g = (int)(color.getGreen() * m + background.getGreen() * z);
		int b = (int)(color.getBlue() * m + background.getBlue() * z);
		Color result = new Color(r,g,b);
		return result;
	}
	
	@Override
	public String toString() {
		return "Triangle(" + p1 + ";" + p2 + ";" + p3 + ";" + color.getRGB() + ")";
	}
	
	/**
	 * Return a triangle parsed from the given string, which
	 */
	public static Triangle fromString(String s) throws FormatException {
		if (!s.startsWith("Triangle(") || !s.endsWith(")")) throw new FormatException("Not of required form Triangle(...)");
		String[] args = s.substring("Triangle(".length(),s.length()-1).split(";");
		if (args.length != 4) throw new FormatException("Triangle requires exactly four args, got " + args.length);
		return new Triangle(Point3D.fromString(args[0]),Point3D.fromString(args[1]),Point3D.fromString(args[2]),
					Color.decode(args[3]));
	}

	private Triangle prev, next;
	private Group group;
	
	/**
	 * Return a sort key for the triangle.
	 */
	public static interface Key {
		/**
		 * Return the sort key for this triangle.
		 */
		public double apply(Triangle t);
	}
	
	public static final Key defaultKey = new Key() {
		public double apply(Triangle t) {
			return t.getPoint(0).x;
		}
	};
	


	/**
	 * A grouping of triangle objects.
	 * Each triangle can be in at most one group.
	 */
	public static class Group {
		private Triangle first;
		private Triangle last;
		private Key key;

		/**
		 * Creating a group of triangles to be sorted using the given key.
		 */
		public Group(Key k) {
			if (k == null) throw new IllegalArgumentException("sort key must not be null");
			key = k;
		}
		
		/**
		 * Determing whether this group is empty.
		 * This is a constant-time operation.
		 */
		public boolean isEmpty() {
			if(this.first == null && this.last == null ) {
				return true;
			}return false;
		}
		
		/**
		 * Returning the first triangle in this group, or null if empty.
		 * This is a constant-time operation.
		 */
		public Triangle getFirst() {
			if(first == null) {
				return null;
			}return first;
		}
		
		
		/**
		 * Adding this triangle at the end of the current group.
		 * This is a constant-time operation.

		 Case 1: if the triangle is null or already in a group, throw

		 Case 2: if there are no triangles in the group
		 First and last will end up be t
		 Only t will be in the group

		 Case 3: if there is only one triangle in the group
		 First and last are already the same
		 put t in between them and make cyclic

		 Case 4: if there are 2+ triangles in the group
		 add to last and make cyclic
		 */
		public void add(Triangle t) {
			if(t == null || t.group != null) {
				throw new IllegalArgumentException();//if the triangle is null or already in a group, throw
			}else if(first == null && last == null) { //if there are no elements 
				t.group = this; //set the group
				first = last = t;//first and last will be the same 
				first.prev = null;//first.prev always needs to be null 
				last.next = null;//last.next always need to be null
			}else { //more than 2 
				t.group = this;//set the group
				t.prev = last; //make the prev link to the last cause we add at tail
				last.next = t;//connecting the old last to the new last
				last = t;//making added element our last
				last.next = null;//last.next always need to be null
			}
		}
		
		/**
		 * Removing and return the first triangle in the group.
		 *
		Case 1: if the there are no triangles in a group, throw
		
		Case 2: if there is only one triangle in the group
			Remove that one element, make everything null
			
		Case 3: if there are 2+ triangles in the group
			move result to first then move first to the triangle after and disconnect the first and result
		 */
		public Triangle removeFirst() throws NoSuchElementException {
			Triangle result;
			if(first == null) { //If there are no triangles, throw
				throw new NoSuchElementException("Nothing to remove");
			}else if(first.next == null) {  // if there is only one triangle
				result = first;//make the t we are going to remove the first since theres only one
				first = null;//make first null
				last = null;//if first == null, last == null
				result.next = null;//make the next null because it is also the last 
				result.group = null;//remove from group
			}else {
				result = first;//making the the one we want to remove (First)
				first = first.next; //moving first pointer to the next element
				first.prev = null;//unlinking the old First element by removing next elements prev pointer
				result.next = null;//unlinking old First next pointer
				result.group = null;//remove from group
			}
			return result;
		}
		
		/**
		 * Removeing the triangle from this group.
		 * This is a constant-time operation.

		 * Case 1: the triangle is empty or triangle already in group
		 * Case 2: only one triangle is in the group
		 * Case 3: More than one triangle
		 * 		At first
		 * 		At last
		 * 		Middle
		 */
		public void remove(Triangle t) throws IllegalArgumentException {

			if(t == null || t.group == null) { //conditions
				throw new IllegalArgumentException("the triangle is empty or triangle already in group");
			}
			if(first == last) { //if theres only one element
				if(first != t) {//and its not the one we want, throw
					throw new IllegalArgumentException("nothing to remove");
				}
				first = null; //if it is the one we want, everything is null
				last = null;
				t.group = null;
			}else if(t == first) {//if the element is at the first 
				first = first.next;//point first to the 2nd triangle
				first.prev = null;//unlink new first's prev from old first
				t.group = null;//remove old first from group
			}else if(t == last) {//if the element is at the last 
				last = last.prev;//point last to the one before the last
				last.next = null;//unlink the element by making the new last's next null
				t.group = null;//remove from group
			}else {
				t.next.prev = t.prev;//unlinking from next
				t.prev.next = t.next;//unlinking from prev
				t.group = null;//removing from group
				last.next = null;	
				}
		}
		
		/**
		 * Sort the group starting from this triangle.
		 *
		 * when we add the newest element will be at the end
		 * Case 1: t == null
		 * Case 2: While loop works and T is automatically sorted
		 * Case 3: T is not automatically sorted (T is the smallest)
		 * Case 4: T is neither the largest nor the smallest it is somewhere in between
		 * 		Link together the pointer and the current after pointer
		 */
		private void sortForward(Triangle t) {
			while(t!=null) {
				Triangle sol = t.prev;//our pointer 
				//keep checking if triangle before this one is bigger than the one added, 
				//if its bigger, sol will will keep moving until down until it is either pointing to 
				//null or on the element before we want it
				while(sol != null && key.apply(sol)>key.apply(t)) { 
					sol = sol.prev;
				}
				
				if(sol == t.prev) { //if our pointer is before the added triangle, it is sorted. Move on to next t.
					t = t.next;
				}else {// if the pointer isnt already pointing at the one before the added (did not sort) We want to swap
					t.prev.next = t.next;//move the old to after the new, disconnecting the prev node of t
					
					//breaks the next node of t
					if(t.next != null) {//if t is not your last element 
						t.next.prev = t.prev;//next triangle's prev link pointing back to the triangle being considered to skip
					}else {//if t is the last element while the previous element is bigger
						last = t.prev;//make the old your last element 
					}
					
					//(connecting ts based off of sol)
					if(sol != null) {//linking t to after sol to place it in the right spot
						t.next = sol.next; //set ts next link to whatever is after the pointer
						t.prev = sol; //moving t prev link to sol
						sol.next.prev = t;//linking t's prev to itself 
						sol.next = t;//linking cursor with new element sorting it correctly
					}else {  //if sol is null because its in the beginning which means t is the smallest so connect it to the front
						first.prev = t; //make the t the first index
						t.next = first; //set the triangle after that to the front 
						t.prev = null;//if first null then first.prev needs to be null
						first = t;//setting our new (smaller) element as the first
					}
				}
			}
		}
		/**
		 * Adding this triangle in the correct position.
		 */
		public void insert(Triangle t) {
			add(t);//adding
			sortForward(t);//then sorting (insertion sort)
		}
		
		/**
		 * Sort all the triangles in this group according to the key.
		 */
		public void sort() {
			sortForward(first);
		}
		
	}

	}
}
