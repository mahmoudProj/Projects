import javax.sound.midi.*;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Jukebox implements MetaEventListener {

	/** Static Constants */
    private static final int TICKS_PER_QUARTER_NOTE = 256;
    public static final int END_OF_TRACK_MESSAGE = 47;

    /** GUI fields */
    private JFrame frame;
    private JMenuBar bar;
    private JMenu songMenu, instrumentMenu;
    private JProgressBar progressBar;
    private Timer progressUpdate;
    private JButton togglePlayback;
	private JComboBox<Transpose> transposeControl;
	private JComboBox<Stretch> stretchControl;

    /** MIDI fields */
    private Synthesizer synthesizer;
    private Sequencer sequencer;
    private float bpm;
    
    /**
     * Instantiates a new Jukebox.
     */
    public Jukebox() {
    	SwingUtilities.invokeLater( ()->createGUI() );
        createMidiComponents();
    }
    
    /**
     * Creates the MIDI components of the Jukebox.
     */
    private void createMidiComponents() {
    	try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            sequencer = MidiSystem.getSequencer(false);
            sequencer.open();
            sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
            sequencer.addMetaEventListener(this);
            progressUpdate = new Timer(5, event -> progressBar.setValue((int) sequencer.getTickPosition()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static enum Transpose {
    	OCT_HIGHER("octave higher",12),
    	FIFTH_HIGHER("fifth higher",7),
    	NO_CHANGE("no change",0),
    	FOURTH_LOWER("fourth lower",-5),
    	OCT_LOWER("octave lower",-12);
    	
    	public final String name;
    	public final int amount;
    	private Transpose(String n, int a) {
    		name = n;
    		amount = a;
    	}
    	
    	@Override
    	public String toString() { return name; }
    }
    
    private int getTranspose(boolean genError) {
    	Object choice = transposeControl.getSelectedItem();
    	if (choice instanceof Transpose) {
    		return ((Transpose)choice).amount;
    	}
    	String edited = (String)choice;
    	edited = edited.trim();
    	try {
    		if (edited.startsWith("+")) return Integer.parseInt(edited.substring(1));
    		else if (edited.startsWith("-")) return -Integer.parseInt(edited.substring(1));
    		else return Integer.parseInt(edited);
    	} catch (NumberFormatException ex) {
    		if (genError) {
    			JOptionPane.showMessageDialog(frame, "Expected +n or -n: " + ex, "Transpose Selection Error", JOptionPane.ERROR_MESSAGE);
    		}
    	}
    	return 0;
    }
    
    private static enum Stretch {
    	DOUBLE_SPEED("×2",0.5),
    	FASTER("×1.5",2.0/3),
    	NO_CHANGE("×1",1.0),
    	SLOWER("×0.75",4.0/3),
    	HALF_SPEED("×0.5",2.0);
    	
    	public final String name;
    	public final double amount;
    	private Stretch(String n, double a) {
    		name = n;
    		amount = a;
    	}
    	
    	@Override
    	public String toString() { return name; }    	
    }
    
    private double getStretch(boolean genError) {
    	Object choice = stretchControl.getSelectedItem();
    	if (choice instanceof Stretch) return ((Stretch)choice).amount;
    	String edited = (String)choice;
    	edited = edited.trim();
    	try {
    		if (edited.indexOf('-') >= 0) throw new ArithmeticException("negative not allowed");
    		if (edited.startsWith("×") || edited.startsWith("x") || edited.startsWith("X")) return 1.0/Double.parseDouble(edited.substring(1));
    		return 1.0/Double.parseDouble(edited);
    	} catch (NumberFormatException ex) {
    		if (genError) {
    			JOptionPane.showMessageDialog(frame, "Expected ×n or n: " + ex, "Playback Speed Selection Error", JOptionPane.ERROR_MESSAGE);
    		}
    	} catch (ArithmeticException ex) {
    		if (genError) {
    			JOptionPane.showMessageDialog(frame, "Playback speed must be positive", "Playback Speed Selection Error", JOptionPane.ERROR_MESSAGE);
    		}
    	}
    	return 1;
    }
    
    /**
     * Creates the GUI of the Jukebox.
     */
  	private void createGUI(){
  		frame = new JFrame("MIDI Jukebox");
  		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  		frame.setLocationRelativeTo(null);

  		bar = new JMenuBar();
  		
  		progressBar = new JProgressBar();
  		progressBar.setPreferredSize(new Dimension(300,20));
  		togglePlayback = new JButton("Play");
  		togglePlayback.addActionListener( event -> togglePlayback());
  		togglePlayback.setEnabled(false);
  		
  		frame.setLayout(new BorderLayout());
  		JPanel controlPanel = new JPanel();
  		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
  		
  		transposeControl = new JComboBox<>(Transpose.values());
  		transposeControl.getModel().setSelectedItem(Transpose.NO_CHANGE);
  		transposeControl.setEditable(true);
  		transposeControl.addActionListener((ae) -> {
  			getTranspose(true);
  		});
  		
  		stretchControl = new JComboBox<>(Stretch.values());
  		stretchControl.getModel().setSelectedItem(Stretch.NO_CHANGE);
  		stretchControl.setEditable(true);
  		stretchControl.addActionListener((ae) -> {
  			getStretch(true);
  		});
  		
  		controlPanel.add(new JLabel("Transpose:"));
  		controlPanel.add(transposeControl);
  		controlPanel.add(Box.createHorizontalGlue());
  		controlPanel.add(new JLabel("Playback:"));
  		controlPanel.add(stretchControl);
  		
  		frame.add(controlPanel,BorderLayout.NORTH);
  		JPanel playPanel = new JPanel();
  		playPanel.setLayout(new FlowLayout());
  		playPanel.add(progressBar);
  		playPanel.add(togglePlayback);
  		frame.add(playPanel,BorderLayout.CENTER);
 
  	   	readySongs(transposeControl, stretchControl);
    	readyInstruments();
    	frame.setJMenuBar(bar);
  		frame.pack();
  		frame.setVisible(true);
  	}
  	
  	/**
  	 * Toggles playback of the Jukebox.
  	 */
  	private void togglePlayback() {
  		if (togglePlayback.getText().equals("Play")) {
  			sequencer.start();
  			sequencer.setTempoInBPM(bpm);

  			songMenu.setEnabled(false);
  			togglePlayback.setText("Stop");
  			progressUpdate.start();
  		}
  		else {
  			sequencer.stop();
  			progressUpdate.stop();
  			songMenu.setEnabled(true);
  			togglePlayback.setText("Play");
  		}
  	}

  	
  	/**
  	 * Loads all songs from ./songs directory into Song objects, and maps them to menu items.
  	 */
  	private void readySongs(final JComboBox<Transpose> ctl1, final JComboBox<Stretch> ctl2){
  		songMenu = new JMenu("Songs");
  		for (final File songFile : new File("./songs").listFiles()) {
  			NewSong song = read(songFile);
  			JMenuItem menuItem = new JMenuItem(song.getName());
  			menuItem.addActionListener( event -> loadSong(song));
  			songMenu.add(menuItem);
  		}
  		bar.add(songMenu);
  	}
  	
  	/**
  	 * Loads the given Song into the Sequencer by first converting the song into a Sequence.
  	 */
  	private void loadSong(NewSong originalSong) {
  		// we are wanting to change the song, so we make a copy
  		NewSong song = originalSong.clone();

  		int transpose = getTranspose(false);
  		double stretch = getStretch(false);
  		try {
  			song.transpose(transpose);
  			song.stretch(stretch);
  		} catch (IllegalArgumentException ex) {
  			JOptionPane.showMessageDialog(null, "Song modification failed: " + ex, "Playback warning", JOptionPane.WARNING_MESSAGE);
  		}

  		try {
  			Sequence sequence = convert(song);
  			progressBar.setValue(0);
  			progressBar.setMaximum(toTicks(song.getDuration()));
  			progressBar.setStringPainted(true);
  			progressBar.setString(song.getName());
  			sequencer.setSequence(sequence);
  			bpm = song.getBPM();
  			togglePlayback.setEnabled(true);
  		}
  		catch (InvalidMidiDataException e) {
  			System.out.println("Unable to convert song: "+song.getName());
  		}
  	}
  	
  	/**
  	 * Reads and parses the given file into a Song object.
  	 */
    private NewSong read(File file) {
		NewSong song = null;
		
		try (Scanner s = new Scanner(file)) {
			String name = s.nextLine().trim();
			float bpm = Integer.parseInt(s.nextLine().trim().split(" ")[0]);
			song = new NewSong(name, (int)bpm);

  			while (s.hasNextLine()) {
  				String line = s.nextLine().trim();
  				if (line.equals("") || line.startsWith("[")) continue;
  				Note note = new Note(line);
  				song.add(note); //adding a note to the end of the song
  			}
  			System.out.print("Loaded \"" + song.getName() + "\" with " + song.size() + " notes. (beginning with ");
  			if(song.isEmpty()) { //if empty
  				//nothing
  			}else {
  				System.out.print(song.getData()[0].toString()); //first note from getData method in newSong
  			}
  		}
  		catch (FileNotFoundException e) { System.out.println("Cannot find file"); }

  		return song;
  	}
  	
    /**
     * Creates the menu of instruments, and maps each instrument's menu item to load that instrument.
     */
    private void readyInstruments() {
    	instrumentMenu = new JMenu("Instruments");
        // The instruments we will use for our Jukebox.
        ArrayList<String> names = new ArrayList<String>(Arrays.asList("kalimba", "honky tonk", "woodblock", "mutedtrumpet",
        		      "fingered bs.", "soprano sax", "steel-str.gt", "nylon-str.gt", "bass & lead", "bassoon", "bandoneon",
                            "gun shot", "piano 2", "solo vox", "piano 1", "piano 3", "agogo", "shakuhachi", "french horns",
                                "breath noise", "celesta", "whistle", "tuba", "vibraphone", "slap bass 1", "slap bass 2"));
        
        for (Instrument instrument : synthesizer.getAvailableInstruments()) {
            String name = instrument.getName().trim().toLowerCase();
            if (names.contains(name.toLowerCase())) {
            	JMenuItem menuItem = new JMenuItem(name);
            	menuItem.addActionListener( event -> loadInstrument(instrument) );
            	instrumentMenu.add(menuItem);
            	names.remove(name.toLowerCase());
            	if (name.equals("fingered bs."))
            		loadInstrument(instrument);
            }
        }
        bar.add(instrumentMenu);
    }
    
    /**
     * Loads the given instrument into the Jukebox's sequencer, first unloading any loaded instruments,
     * and then switching channel 0 to use that instrument's patch's program. For our initial version
     * of Jukebox, we will only use channel 0
     */
    private void loadInstrument(Instrument instrument) {
    	synthesizer.unloadAllInstruments(synthesizer.getDefaultSoundbank());
        synthesizer.loadInstrument(instrument);
        synthesizer.getChannels()[0].programChange(instrument.getPatch().getProgram());
    }

    @Override
    public void meta(MetaMessage msg) {
        if (msg.getType() == END_OF_TRACK_MESSAGE) {
            togglePlayback();
            progressBar.setValue(progressBar.getMaximum());
            sequencer.setTickPosition(0);
        }
    }

    private static int toTicks(double duration) {
    	return (int) Math.round(duration / 0.25 * TICKS_PER_QUARTER_NOTE);
    }
    
    /**
     * Converts a Song to a Sequence, which is playable by a Sequencer.
		*/
    private static Sequence convert(NewSong song) {
        Sequence sequence = null;
        try {
            sequence = new Sequence(Sequence.PPQ, TICKS_PER_QUARTER_NOTE);
            Track track = sequence.createTrack();
            long timestamp = 0;
            for (int i = 0; i < song.size(); i++) //for loop running through all the notes 
            {
                putNote(track, song.getData()[i], timestamp); // calling put note to have it go to the track
                timestamp = timestamp+ toTicks(song.getData()[i].getDuration()); // editing the time stamp
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return sequence;
    }

    /**
     * Puts the note into the track at the timestamp.
     */
    private static void putNote(Track track, Note note, long timestamp) throws InvalidMidiDataException {
    	
    	int midiPitch = note.getMidiPitch();
    	int intensity = Note.DEFAULT_INTENSITY;
    	
    	if (midiPitch == 128) {
    		midiPitch = 0; // rest note
    		intensity = 0;
    	}
    	
        ShortMessage noteOn = new ShortMessage(ShortMessage.NOTE_ON, 0, midiPitch, intensity);
        ShortMessage noteOff = new ShortMessage(ShortMessage.NOTE_OFF, 0, midiPitch, 0);
        track.add(new MidiEvent(noteOn, timestamp));
        track.add(new MidiEvent(noteOff, timestamp + toTicks(note.getDuration())));
    }
    
    /**
     * The main method: Instantiates a new Jukebox.
     */
    public static void main(String[] args) { new Jukebox(); }
}
