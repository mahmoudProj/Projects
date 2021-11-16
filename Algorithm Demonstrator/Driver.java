import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import Process.Queue;


public class Driver{

	/** GUI components  */
	private JFrame frame;
	private JPanel[] panels;
	private JMenuBar menuBar;
	private JMenu load;
	private JMenuItem run, pause;
	
	/**
	 * clockCP acts as the clock of our virtual systems.
	 *  Calls pulse() method at regular intervals.
	 * 
	 *  Adjust PULSE_PER_SEC for your own machine. (50-1000 is a good range)
	 *    Scenario "Few Large" should take around 20 seconds to complete.   
	 */
	private static final int PULSE_PER_SEC = 100;
	private Timer clockCP;
	public static int pulseCt;
	
	/** Scenarios */
	private List<Scenario> scenarios;
	private Scheduler[] schedulers;
	
	
	/** Instantiates a new driver. */
	public Driver(){
		loadScenarios();
		createGUI();}
	
	private void createGUI(){
	  	frame = new JFrame("Scheduler Simulation");
	  	createMenu();
	  	createPanels();
		frame.pack();
		frame.setSize(700,frame.getHeight());
		frame.setResizable(false);
		frame.setLocation(300, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);}
	
	private void createMenu(){
		// Create load menu
		load = new JMenu("Load Scenario");
	    for (Scenario s: scenarios){
	    	JMenuItem m = new JMenuItem(s.name);
	    	m.addActionListener((ae)->{s.unpack();});
	    	load.add(m);}
	    
	    // Create run and pause buttons
	    run = new JMenuItem("Run");
	    run.setPreferredSize(new Dimension(10,run.getHeight()));
	    run.setEnabled(false);
	    run.addActionListener((ae)-> {
	    	run.setEnabled(false);
	    	pause.setEnabled(true);
	    	clockCP.start();});
	    pause = new JMenuItem("Pause");
	    pause.setPreferredSize(new Dimension(10,pause.getHeight()));
	    pause.setEnabled(false);
	    pause.addActionListener((ae)-> {
	    	run.setEnabled(true);
	    	pause.setEnabled(false);
	    	clockCP.stop();});
	    
	    // Combine menu
	  	menuBar = new JMenuBar();
	  	menuBar.add(load);
	  	menuBar.add(run);
	  	menuBar.add(pause);
		frame.setJMenuBar(menuBar);}
	
	
	private void createPanels(){
		// Create scheduler labels
		JLabel rrLabel = new JLabel("Strict FIFO");
		rrLabel.setHorizontalAlignment(JLabel.CENTER);
		rrLabel.setForeground(Color.WHITE);
		rrLabel.setBackground(Color.DARK_GRAY);
		rrLabel.setOpaque(true);
		JLabel sfLabel = new JLabel("Round Robin");
		sfLabel.setHorizontalAlignment(JLabel.CENTER);
		sfLabel.setForeground(Color.WHITE);
		sfLabel.setBackground(Color.DARK_GRAY);
		sfLabel.setOpaque(true);
		
		// Create scheduler panels
		panels = new JPanel[3];
		for (int i=0;i<3;i++)
			panels[i] = new JPanel();
		
		// Assemble in master panel
		panels[0].setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0;c.insets = new Insets(4,2,4,2);
		panels[0].add(rrLabel, c);
		c.gridx = 1;
		panels[0].add(sfLabel, c);
		c.gridx = 0; c.gridy = 1;c.insets = new Insets(0,0,0,0);
		panels[0].add(panels[1], c);
		c.gridx = 1;
		panels[0].add(panels[2], c);}
	
	
	/** Triggered by the clock of our virtual systems.
	 *  Delivers pulses to the schedulers which then load and unload
	 *  processes in the CPU and cause it to execute an instruction.
	 *  When the schedulers have no processes left it will stop. */
	private void pulse(){
		for (Scheduler s: schedulers)
			if (!s.isDone())
				s.step();
		
		if (schedulers[0].isDone() && schedulers[1].isDone()){
			run.setEnabled(false);
			pause.setEnabled(false);
			clockCP.stop();}}
	
	/** Container for various process queues linked with names. 
	 *  We will keep these handy to quickly load various scenarios
	 *  to test the functionality of the two schedulers. */
	private class Scenario{
		private String name;
		private Process.Queue readyQueue;
		private Scenario(String n){name = n;readyQueue = new Queue();}
		private void addProcess(Process p){readyQueue.offer(p);}
		
		/** Load this scenario into the schedulers and resets state of the driver */
		private void unpack(){
			pulseCt=0;
    		run.setEnabled(true);
    		pause.setEnabled(false);
    		
			// Reset schedulers and load with processes of this scenario
    		schedulers = new Scheduler[] {new Scheduler(false), new Scheduler(true)};
    		schedulers[0].scheduleAll(readyQueue.clone());
    		schedulers[1].scheduleAll(readyQueue.clone());
			
			// Create GUI for schedulers and their processes
			for (int i=1; i< 3; i++){
				panels[i].removeAll();
				panels[i].setLayout(new GridLayout(schedulers[0].getReadyQueue().size()+1,2));
				for (Process p: schedulers[i-1].getReadyQueue()){
					JLabel label = new JLabel(p.getName());
					label.setHorizontalAlignment(JLabel.CENTER);
					panels[i].add(label);
					panels[i].add(p.getBar());
				}
			}
			frame.add(panels[0]);
			frame.pack();
			frame.repaint();
		}
	}
	
	/** Parses and instantiates all scenarios from scenario file. */
	private void loadScenarios(){
		try (Scanner s = new Scanner(new File("scenarios"))){
			String str;
			Scenario newestScenario = null;
			scenarios = new ArrayList<>();
			
			// Read file
			while (s.hasNextLine()){
				str = s.nextLine();
				
				// Create new scenario at keyword: "scenario"
				if (str.startsWith("scenario")){
					if (newestScenario != null && newestScenario.readyQueue.isEmpty())
						throw new ParseException("Cannot create scenario with no processes.",0);
					else
						scenarios.add(newestScenario = new Scenario(str.substring(9)));}
				
				// Create new process at keyword: "process"
				else if (str.startsWith("process")){
					String totalInsString = str.split(" ")[str.split(" ").length-1];
					int totalIns;
					if ((totalIns = Integer.decode(totalInsString)) < 1)
						throw new ParseException("Cannot create process where totalInstructions = "+totalInsString,0);
					String processName = str.substring(8, str.length()-totalInsString.length());
					newestScenario.addProcess(new Process(processName,totalIns));
				}
			}
			if (newestScenario.readyQueue.isEmpty())
				throw new ParseException("Cannot create scenario with no processes.",0);
			else if (scenarios.isEmpty())
				throw new ParseException("No scenarios in file.",0);
		}
		catch(ParseException e){System.out.println("Malformed scenario file: "+e.getMessage());System.exit(1);}
		catch(Exception e){System.out.println("\nUnexpected exception when reading scenario file...\n");e.printStackTrace();System.exit(1);}
		clockCP = new Timer(1000/PULSE_PER_SEC, (ae)->{pulseCt++;pulse();});
	}
	
	/** The main method to create a new driver. */
	public static void main(String[] args){
		new Driver();
	}
}
