
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.management.modelmbean.XMLParseException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
//import org.jfree.chart.JFreeChart;
//import org.jfree.data.category.CategoryDataset;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class VideoSearch implements MouseListener, MouseMotionListener {

	public static void main(String[] args) {
		String fileName = args[0];
		String soundFileName = args[1];
		int width = 352;
		int height = 288;
		// String fileName = "../image1.rgb";

		// find the index of a particular frame, initialize it to 720 frames, which is larger than 60 frames
		byteIndicies = new int[600];
		for (int b = 0; b < 600; ++b) {
			byteIndicies[b] = b * 304128;
		}

		// preprocess 转化成xml
		// VideoPreProcessor vpp = new VideoPreProcessor("vdos", byteIndicies);
		// vpp.fileTraverse();
		// fileNames = (vpp.getFileNames()).toArray(new String[0]);

		// temp so i don't have to preprocess every fucking time I run this
		// String[] temp = {"vdo3", "vdo4", "vdo6"};
		// fileNames = temp;

		VideoSearch ir = new VideoSearch(width, height, fileName, soundFileName);
		// for video!
		if (vidFlag) {
			ir.fps.start();
			ir.fps1.start();
		}
	}

	public static VideoPreProcessor vpp;
	public static BufferedImage img; // img
	public static BufferedImage img1; // img1
	public static JFrame frame; // frame for UI
	public static JPanel panel; // panel to be shown
	public static JPanel panel1; // panel to be shown
	public static JPanel panel2; // panel to be shown
	public static boolean vidFlag;
	public static int currFrame = 0; // current frame
	public static int currFrame1 = 0; // current frame
	public static byte[] bytes; // bytes from file
	public static byte[] bytes1; // bytes1 from file
	public static int[] byteIndicies; // keeps indexes where new frames start;
	public static int[] byteIndicies1; // keeps indexes where new frames start;
	public static int state; // 0 = play, 1 = pause, 2 = stop?
	public static int state1; // 0 = play, 1 = pause, 2 = stop?

	public static String currVid; // name of current video that is playing
	public static String[] fileNames; // names of all the vid files
	public static String search1; // name of video to be searched
	public static byte[] searchBytes1; // the bytes of the video that is being
										// searched

	public static String color; // the color to search for
	public static int[] colorFrames; // frames that go in the first search strip
	// public static String color; // color to match

	public static String motion; // the motion to search for
	public static int[] motionFrames; // frames of matched motion

	public static String audio; // audio to be matched
	public static int[] audioFrames; // frames of matched audio

	public static File soundFile;
	public static File soundFile1;
	public static File soundFile2;
	public static FileInputStream sound;
	public static boolean dataLineFlushed;
	public static byte[] soundBytes;
	public static byte[] soundBytes1;
	public static byte[] soundBytes2;
	// public final int soundByteCount = 25518752;
	public static int[] soundByteIndicies;
	public static InputStream waveStream;
	private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
	public final int SOUND_INDEX = 7357;
	// private final int EXTERNAL_BUFFER_SIZE = 500000;
	public static SourceDataLine dataLine;
	public static SourceDataLine dataLine1;
	public static AudioInputStream audioInputStream;
	public static AudioInputStream audioInputStream1;
	public static AudioInputStream audioInputStream2;
	public static boolean isAlreadyPlaying = false;
	Thread soundThread;
	Thread soundThread1;
	Timer fps;
	Timer fps1;
	JSlider slider;
	JTextArea textA;
	JList list;
	public static String[] choices = { "flowers", "interview", "starcraft",
			"movie", "musicvideo", "sports", "traffic" };
	public static String selection;
	public static BufferedImage blankFrame;
	JProgressBar progressBar;
	GridBagLayout layout;
	String path = "/Users/yinli/Documents/workspace/VideoSearch/src/vdos/";

	public void init(String f) throws IOException {
		String fileName = path + f + "/" + f + ".rgb";
		String soundFileName = path + f + "/" + f + ".wav";
		File file = new File(fileName);
		InputStream is = new FileInputStream(file);

		soundFile = new File(soundFileName);

		InputStream sis = new FileInputStream(soundFile);

		long len = file.length();
		long slen = soundFile.length();

		bytes = new byte[(int) len];
		soundBytes = new byte[(int) slen];
		audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e1) {
			new PlayWaveException(e1);
		} catch (IOException e1) {
			new PlayWaveException(e1);
		}

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// if file longer then height*width*3 or whatever the length is then it
		// is a video!
		// len = 304128 for single picture
		vidFlag = false;
		// this is a video!
		// if (len > 304128) {
		// vidFlag = true;
		fps = new Timer(30, new refreshFrame());
		fps.setInitialDelay(0);

		// }

		int height = 288;
		int width = 352;

		int ind = 0;
		// get first frame
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind + height * width];
				byte b = bytes[ind + height * width * 2];

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8)
						| (b & 0xff);
				// int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x, y, pix);
				ind++;
			}
		}

	}

	public VideoSearch(int width, int height, String fileName, String soundFileName) {
            state = 2;
		// o_width = width;
		// o_height = height;

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// Reading File
		try {
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);

			soundFile = new File(soundFileName);

			InputStream sis = new FileInputStream(soundFile);

			long len = file.length();
			long slen = soundFile.length();

			bytes = new byte[(int) len];
			soundBytes = new byte[(int) slen];

			System.out.println("file length:" + len);
			audioInputStream = null;
			try {
				audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			} catch (UnsupportedAudioFileException e1) {
				new PlayWaveException(e1);
			} catch (IOException e1) {
				new PlayWaveException(e1);
			}
			System.out.println(audioInputStream == null);
			// is this a full path name?
			int slash = fileName.lastIndexOf("\\");
			int dot = fileName.indexOf(".");
			if (slash == -1) { // this was just a file name
				currVid = fileName.substring(0, dot);
			} else {
				currVid = fileName.substring(slash + 1, dot);
			}
			System.out.println(currVid);
			// bytes = vpp.getFileBytes(currVid);

			// // System.out.println(currVid);

			// System.out.println("file length:"+ len);

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			// if file longer then height*width*3 or whatever the length is then
			// it is a video!
			// len = 304128 for single picture
			vidFlag = false;
			// this is a video!
			// if (len > 304128) {
			// vidFlag = true;
			fps = new Timer(30, new refreshFrame());
			fps.setInitialDelay(0);
			fps1 = new Timer(30, new refreshFrame1());
			fps1.setInitialDelay(0);
			// }

			int ind = 0;
			// get first frame
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind + height * width];
					byte b = bytes[ind + height * width * 2];

					int pix = 0xff000000 | ((r & 0xff) << 16)
							| ((g & 0xff) << 8) | (b & 0xff);
					// int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x, y, pix);
					ind++;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Make a blank tile, for use when there's no search and when there's
		// less matches then are shown though i think that's really unlikely
		blankFrame = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < blankFrame.getHeight(); ++y) {
			for (int x = 0; x < blankFrame.getWidth(); ++x) {
				blankFrame.setRGB(x, y, 0x00FFFFFF);
			}
		}
		// blankFrame = scaleImage(blankFrame, width, height, .2);
		// strip1Start = 0;
		// strip2Start = 0;
		// strip3Start = 0;
		// Debuggin'
		// System.out.println("image dimensions");
		// System.out.println(width);
		// System.out.println(height);
		// System.out.println("number of tiles");
		// System.out.println(wTiles);
		// System.out.println(hTiles);

		// splitImage();

		// Use a label to display the image
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		layout = new GridBagLayout();
		frame.setLayout(layout);
		panel = new JPanel();
		panel1 = new JPanel();
		panel2 = new JPanel();
		// show original image
		// showImage(frame.getContentPane());
		// showImage(frame.getContentPane());

		panel.setLayout(new BorderLayout());
		JLabel label1 = new JLabel(new ImageIcon(img));
		label1.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

		// original.add(label, BorderLayout.CENTER);
		panel.add(label1, BorderLayout.CENTER);

		panel.revalidate();
		panel.repaint();

		panel2.setLayout(new BorderLayout());

		JLabel label2 = new JLabel(new ImageIcon(img));
		label2.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

		textA = new JTextArea(1, 1);

		list = new JList(choices); // data has type Object[]
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		// list.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(1, 6));

		panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
		panel1.setPreferredSize(new Dimension(50, 100));
		panel1.add(Box.createRigidArea(new Dimension(25, 25)));
		textA.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel1.add(Box.createRigidArea(new Dimension(25, 25)));
		panel1.add(textA);

		list.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel1.add(Box.createRigidArea(new Dimension(500, 25)));
		panel1.add(list);
		panel1.add(Box.createRigidArea(new Dimension(25, 25)));
		panel1.revalidate();
		panel1.repaint();

		// original.add(label, BorderLayout.CENTER);

		panel2.add(label2, BorderLayout.SOUTH);
		panel2.revalidate();
		panel2.repaint();

		// Buttons

		// Where the GUI is constructed:
		progressBar = new JProgressBar(0, 600);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setPreferredSize(new Dimension(200, 400));

		buttonPanel.add(Box.createRigidArea(new Dimension(25, 0))); // Spacing

		// progressBar.setAlignmentY(Component.CENTER_ALIGNMENT);

		MyButton playButton = new MyButton("Play");
		playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		buttonPanel.add(playButton);

		buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing

		MyButton pauseButton = new MyButton("Pause");
		pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(pauseButton);

		buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing

		MyButton searchButton = new MyButton("Stop");
		searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(searchButton);

		buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing

		MyButton closeButton = new MyButton("Search");
		closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(closeButton);

		MyButton playButton2 = new MyButton("Play1");
		closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(playButton2);
		MyButton pauseButton2 = new MyButton("Pause1");
		closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(pauseButton2);
		MyButton stopButton2 = new MyButton("Stop1");
		closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(stopButton2);

		slider = new JSlider(JSlider.HORIZONTAL, 0, 600, 10);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int idx = ((JSlider) e.getSource()).getValue();
				// refreshFrame(idx);
			}
		});

		JPanel panelmid = new JPanel();
		JPanel panelfront = new JPanel();
		JPanel panelleft = new JPanel();
		frame.add(panelfront);
		frame.add(textA);
		frame.add(panelmid);
		frame.add(list);
		frame.add(slider);
		// frame.add(progressBar);
		frame.add(panel);
		frame.add(panel2);
		frame.add(buttonPanel);

		GridBagConstraints s = new GridBagConstraints();
		s.fill = GridBagConstraints.BOTH;
		s.gridwidth = 1;
		s.weightx = 0;
		s.weighty = 0;
		layout.setConstraints(panelfront, s);
		s.gridwidth = 2;
		s.weightx = 0;
		s.weighty = 0;
		layout.setConstraints(textA, s);
		s.gridwidth = 1;
		s.weightx = 0;
		s.weighty = 0;
		layout.setConstraints(panelmid, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 0;
		layout.setConstraints(list, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 0;
		layout.setConstraints(slider, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 0;
		// layout.setConstraints(progressBar, s);
		s.gridwidth = 3;
		s.weightx = 0;
		s.weighty = 0;
		layout.setConstraints(panel, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 0;
		layout.setConstraints(panel2, s);
		s.gridwidth = 0;
		s.weightx = 0;
		s.weighty = 0;
		layout.setConstraints(buttonPanel, s);
		frame.pack();
		frame.setVisible(true);

		ListSelectionModel listSelectionModel = list.getSelectionModel();
		listSelectionModel
				.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						currFrame1 = 0;
						progressBar.setValue(0);
						ListSelectionModel lsm = (ListSelectionModel) e
								.getSource();
						int minIndex = lsm.getMinSelectionIndex();
						selection = choices[minIndex];
						System.out.println(selection);
						File file = new File(path + selection + "/" + selection
								+ ".rgb");
						try {
							InputStream is = new FileInputStream(file);

							soundFile1 = new File(path + selection + "/"
									+ selection + ".wav");

							InputStream sis = new FileInputStream(soundFile1);

							long len = file.length();
							long slen = soundFile1.length();

							bytes1 = new byte[(int) len];
							soundBytes1 = new byte[(int) slen];
							int offset = 0;
							int numRead = 0;
							while (offset < bytes1.length
									&& (numRead = is.read(bytes1, offset,
											bytes1.length - offset)) >= 0) {
								offset += numRead;
							}

						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						audioInputStream1 = null;
						try {
							audioInputStream1 = AudioSystem
									.getAudioInputStream(soundFile1);
						} catch (UnsupportedAudioFileException e1) {
							new PlayWaveException(e1);
						} catch (IOException e1) {
							new PlayWaveException(e1);
						}
						int ind = 0;
						// get first frame
						int height = 288;
						int width = 352;
						img1 = new BufferedImage(width, height,
								BufferedImage.TYPE_INT_RGB);
						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								byte a = 0;
								byte r = bytes1[ind];
								byte g = bytes1[ind + height * width];
								byte b = bytes1[ind + height * width * 2];
								System.out.println(r);
								int pix = 0xff000000 | ((r & 0xff) << 16)
										| ((g & 0xff) << 8) | (b & 0xff);
								// int pix = ((a << 24) + (r << 16) + (g << 8) +
								// b);

								img1.setRGB(x, y, pix);
								ind++;
							}
						}

					}
				});

	}

	// Function calls
	// ///////////////////////////////////////////////////
	// / VIDEO STUFF
	// /////////////////////////////////////////////////////

	// show original image
	public void showImage(Container pane) {

		// panel.removeAll();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
		label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		// original.add(label, BorderLayout.CENTER);
		panel.add(label, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
		pane.add(panel, BorderLayout.CENTER);

	}

	// get next frame for the main video
	public BufferedImage refreshFrame(int currFrame) {
		// get new picture

		int ind = byteIndicies[currFrame];
		// System.out.println(currFrame + " is at " + ind);
		BufferedImage img = new BufferedImage(352, 288,
				BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 288; y++) {
			for (int x = 0; x < 352; x++) {
				// System.out.println("i:" + ind);
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind + 288 * 352];
				byte b = bytes[ind + 288 * 352 * 2];

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8)
						| (b & 0xff);
				// int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x, y, pix);
				ind++;
			}
		}
		// img = scaleImage(img, 352, 288, scale);
		return img;
	}

	public BufferedImage refreshFrame1(int currFrame) {
		// get new picture

		int ind = byteIndicies[currFrame1];
		// System.out.println(currFrame + " is at " + ind);
		BufferedImage img = new BufferedImage(352, 288,
				BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 288; y++) {
			for (int x = 0; x < 352; x++) {
				// System.out.println("i:" + ind);
				byte a = 0;
				byte r = bytes1[ind];
				byte g = bytes1[ind + 288 * 352];
				byte b = bytes1[ind + 288 * 352 * 2];

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8)
						| (b & 0xff);
				// int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x, y, pix);
				ind++;
			}
		}
		// img = scaleImage(img, 352, 288, scale);
		return img;
	}

	// refresh original pane
	public void videoOriginal(JPanel panel, BufferedImage img) {
		// update image
		panel.removeAll();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
		label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		panel.add(label, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
	}

	// show the actual frames that match a certain parameter

	// ///////////////////////////////////////////////////////
	// / CONTROLLER STUFF
	// ///////////////////////////////////////////////////////////
	// buttons
	public void buttonPressed(String name) throws IOException {
		if (name.equals("Play")) { // Play

			String filename = textA.getText();
			init(filename);
			currFrame = 0;
			System.out.println("loaded");
			state = 0;
			soundThread = null;
			soundThread = new Thread(new RefreshSound());
			fps.start();
			soundThread.start();
			isAlreadyPlaying = true;
			// dataLineFlushed = false;
		} else if (name.equals("Pause")) { // Pause
			state = 1;
			fps.stop();
			try {
				audioInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			soundThread.interrupt();
			dataLine.stop();
			dataLine.flush();
			dataLine.close();
		} else if (name.equals("Search")) {
			// search only if not playing
			System.out.println("begin search");
			if (state != 0) {
				// extract what to match

				String filename = textA.getText();
				String curpath = path + filename + ".xml";
				String dbpath = path + selection + ".xml";
				System.out.println(curpath);
				System.out.println(dbpath);
				int[] finalscore;
				// System.out.println(curpath);
				// System.out.println(dbpath);
				SearchVideo sv = new SearchVideo();
				finalscore = sv.BeginSearch(curpath, dbpath);
				System.out.println(finalscore);
				Chart c = new Chart();
				CategoryDataset dataset = c.createDataset();
				JFreeChart freeChart = c.createChart(dataset);
				ChartPanel chartpanel = new ChartPanel(freeChart);
				chartpanel.setVisible(true);
				frame.add(chartpanel);
				GridBagConstraints s = new GridBagConstraints();
				s.fill = GridBagConstraints.BOTH;
				s.gridwidth = 0;
				s.weightx = 0;
				s.weighty = 1;
				layout.setConstraints(chartpanel, s);
				frame.pack();

				/*
				 * try { extractSearchParams(); } catch
				 * (XPathExpressionException | FileNotFoundException e) { //
				 * TODO Auto-generated catch block e.printStackTrace(); } catch
				 * (IOException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */
			}

		} else if (name.equals("Stop")) { // close
			state = 2;
			// fps.stop();
			currFrame = 0;
			BufferedImage f = refreshFrame(currFrame);
			// if (view == 0) {
			videoOriginal(panel, f);
			isAlreadyPlaying = false;
			soundThread.interrupt();
			dataLine.stop();
			dataLine.flush();
			dataLine.close();
		} else if (name.equals("Play1")) { // close
			state = 0;
			soundThread1 = null;
			soundThread1 = new Thread(new RefreshSound1());
			fps1.start();
			soundThread1.start();
			isAlreadyPlaying = true;
		} else if (name.equals("Pause1")) { // close
			state = 1;
			fps1.stop();
			try {
				audioInputStream1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			soundThread1.interrupt();
			dataLine1.stop();
			dataLine1.flush();
			dataLine1.close();
		} else if (name.equals("Stop1")) { // close
			state1 = 2;
			// fps.stop();
			currFrame1 = 0;
			BufferedImage f = refreshFrame1(currFrame1);
			// if (view == 0) {
			videoOriginal(panel2, f);
			isAlreadyPlaying = false;
			soundThread1.interrupt();
			dataLine1.stop();
			dataLine1.flush();
			dataLine1.close();
		}
	}

	class MyButton extends JButton {
		MyButton(String label) {
			setFont(new Font("Helvetica", Font.BOLD, 10));
			setText(label);
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					try {
						buttonPressed(getText());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		MyButton(String label, ImageIcon icon) {
			Image img = icon.getImage();
			Image scaleimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			setIcon(new ImageIcon(scaleimg));
			setName(label);
			// setFont(new Font("Helvetica", Font.PLAIN, 0));
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					try {
						buttonPressed(getName());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}
	}

	class refreshFrame implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (state == 0) { // play
				++currFrame;
				if (currFrame == 150) {
					currFrame = 0;
					soundThread.interrupt();
					dataLine.stop();
					dataLine.flush();
					dataLine.close();
					soundThread = null;
					soundThread = new Thread(new RefreshSound());
					soundThread.start();
				}
				// BufferedImage f
				img = refreshFrame(currFrame);
				// if (view == 0) {
				videoOriginal(panel, img);
			} else if (state == 1) { // pause
				BufferedImage f = refreshFrame(currFrame);
				// if (view == 0) {
				videoOriginal(panel, f);
				fps.stop();
			} else if (state == 2) { // stop
			// currFrame = 0;
				BufferedImage f = refreshFrame(currFrame);
				// if (view == 0) {
				videoOriginal(panel, f);
				fps.stop();
			}

			// System.out.println("Frame:" + currFrame);

		}
	}

	class refreshFrame1 implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (state1 == 0) { // play
				++currFrame1;
				if (currFrame1 == 600) {
					currFrame1 = 0;
					soundThread1.interrupt();
					dataLine1.stop();
					dataLine1.flush();
					dataLine1.close();
					soundThread1 = null;
					soundThread1 = new Thread(new RefreshSound1());
					soundThread1.start();
				}
				// progressBar.setValue(currFrame1);
				slider.setValue(currFrame1);
				// BufferedImage f
				img1 = refreshFrame1(currFrame1);
				// if (view == 0) {
				videoOriginal(panel2, img1);
			} else if (state == 1) { // pause
				BufferedImage f = refreshFrame(currFrame1);
				// if (view == 0) {
				videoOriginal(panel2, f);
				fps1.stop();
			} else if (state == 2) { // stop
			// currFrame = 0;
				BufferedImage f = refreshFrame(currFrame1);
				// if (view == 0) {
				videoOriginal(panel2, f);
				fps1.stop();
			}

			// System.out.println("Frame:" + currFrame);

		}
	}

	protected int calculateRMSLevel(byte[] audioData) { // audioData might be
														// buffered data read
														// from a data line
		long lSum = 0;
		for (int i = 0; i < audioData.length; i++)
			lSum = lSum + audioData[i];

		double dAvg = lSum / audioData.length;

		double sumMeanSquare = 0d;
		for (int j = 0; j < audioData.length; j++)
			sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);

		double averageMeanSquare = sumMeanSquare / audioData.length;
		return (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
	}

	public class RefreshSound implements Runnable {
		public void run() {
			// Obtain the information about the AudioInputStream

			InputStream sis = null;
			try {
				// soundFile = new File("vdos/" + currVid + "/" + currVid +
				// ".wav");
				sis = new FileInputStream(soundFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long slen = soundFile.length();
			// System.out.println(sis);
			soundBytes = new byte[(int) slen];

			// System.out.println("file length:"+ len);
			audioInputStream = null;
			try {
				audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			} catch (UnsupportedAudioFileException e1) {
				new PlayWaveException(e1);
				// System.out.println("fail1");
			} catch (IOException e1) {
				new PlayWaveException(e1);
				// System.out.println("fail 2");
			}
			// System.out.println(audioInputStream);
			AudioFormat audioFormat = audioInputStream.getFormat();
			Info info = new Info(SourceDataLine.class, audioFormat);
			// System.out.println(audioFormat == null);
			// System.out.println(info == null);
			// opens the audio channel

			dataLine = null;
			try {
				dataLine = (SourceDataLine) AudioSystem.getLine(info);
				dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
			} catch (LineUnavailableException e1) {
				new PlayWaveException(e1);
			}

			dataLine.start();

			int readBytes = 0;
			byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
			try {
				audioInputStream.skip((long) (currFrame * 7357.0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				while (readBytes != -1) {

					readBytes = audioInputStream.read(audioBuffer, 0,
							audioBuffer.length);
					// System.out.println(readBytes);

					if (readBytes >= 0) {
						dataLine.write(audioBuffer, 0, readBytes);
						System.out.println(dataLine.getLevel());
						// System.out.print("Number of bytes read this round:");System.out.println(readBytes);
						System.out.println("hi");
						int level = 0;
						level = calculateRMSLevel(audioBuffer);
						System.out.println(level);
						System.out.println("bye");
						AudioFormat af = dataLine.getFormat();
						float afloaz = af.getFrameRate();
						float samples = af.getSampleRate();
						int channels = af.getChannels();
						System.out.println(afloaz);
						System.out.println(samples);
						System.out.println(channels);
					}
					// if(readBytes<)
				}
				// dataLine.stop();
			} catch (IOException e1) {
				new PlayWaveException(e1);
			}

		}
	}

	public class RefreshSound1 implements Runnable {
		public void run() {
			// Obtain the information about the AudioInputStream

			InputStream sis = null;
			try {
				// soundFile = new File("vdos/" + currVid + "/" + currVid +
				// ".wav");
				sis = new FileInputStream(soundFile1);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long slen = soundFile1.length();
			// System.out.println(sis);
			soundBytes1 = new byte[(int) slen];

			// System.out.println("file length:"+ len);
			audioInputStream1 = null;
			try {
				audioInputStream1 = AudioSystem.getAudioInputStream(soundFile1);
			} catch (UnsupportedAudioFileException e1) {
				new PlayWaveException(e1);
				// System.out.println("fail1");
			} catch (IOException e1) {
				new PlayWaveException(e1);
				// System.out.println("fail 2");
			}
			// System.out.println(audioInputStream);
			AudioFormat audioFormat = audioInputStream1.getFormat();
			Info info = new Info(SourceDataLine.class, audioFormat);
			// System.out.println(audioFormat == null);
			// System.out.println(info == null);
			// opens the audio channel

			dataLine1 = null;
			try {
				dataLine1 = (SourceDataLine) AudioSystem.getLine(info);
				dataLine1.open(audioFormat, EXTERNAL_BUFFER_SIZE);
			} catch (LineUnavailableException e1) {
				new PlayWaveException(e1);
			}

			dataLine1.start();

			int readBytes = 0;
			byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
			try {
				audioInputStream1.skip((long) (currFrame1 * 7357.0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				while (readBytes != -1) {

					readBytes = audioInputStream1.read(audioBuffer, 0,
							audioBuffer.length);
					// System.out.println(readBytes);

					if (readBytes >= 0) {
						dataLine1.write(audioBuffer, 0, readBytes);
						System.out.println(dataLine1.getLevel());
						// System.out.print("Number of bytes read this round:");System.out.println(readBytes);
						System.out.println("hi");
						int level = 0;
						level = calculateRMSLevel(audioBuffer);
						System.out.println(level);
						System.out.println("bye");
						AudioFormat af = dataLine1.getFormat();
						float afloaz = af.getFrameRate();
						float samples = af.getSampleRate();
						int channels = af.getChannels();
						System.out.println(afloaz);
						System.out.println(samples);
						System.out.println(channels);
					}
					// if(readBytes<)
				}
				// dataLine.stop();
			} catch (IOException e1) {
				new PlayWaveException(e1);
			}

		}
	}

	public class PlayWaveException extends Exception {

		public PlayWaveException(String message) {
			super(message);
		}

		public PlayWaveException(Throwable cause) {
			super(cause);
		}

		public PlayWaveException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	// EXTRACTIONS
	public String extractColor(BufferedImage frame) {
		String color = "";
		int redCount = 0, greenCount = 0, blueCount = 0;
		Map<String, Integer> colorHistogram = new HashMap<String, Integer>();
		for (int j = 0; j < frame.getHeight(); ++j) {
			for (int i = 0; i < frame.getWidth(); i += 4) { // subsample
				// get rgb value of this pixel
				int rgb = frame.getRGB(i, j);
				int r = (rgb & 0x00FF0000) >>> 16;
				int g = (rgb & 0x0000FF00) >>> 8;
				int b = (rgb & 0x000000FF);
				// convert to hue
				float hsb[] = null;
				hsb = Color.RGBtoHSB(r, g, b, null);
				// System.out.println(hsb[0]);
				// stick it in hue histogram
				// buckets [300-360, 0-60], [60-180], [180-300]
				// this is red
				if ((hsb[0] < 0.17) || (hsb[0] >= 0.83)) {
					// System.out.println("red pixel");
					colorHistogram.put("red", new Integer(++redCount));
					// this is green
				} else if (hsb[0] >= 0.17 && hsb[0] < 0.5) {
					// System.out.println("green pixel");
					colorHistogram.put("green", new Integer(++greenCount));
					// this is blue
				} else if (hsb[0] >= 0.5 && hsb[0] < 0.83) {
					// System.out.println("blue Pixel");
					colorHistogram.put("blue", new Integer(++blueCount));
				}
			}
		}
		// which color is dominant
		// int max = colorHistogram.get("red");
		// color = "red";
		Map.Entry<String, Integer> dominantColor = null;
		for (Map.Entry<String, Integer> e : colorHistogram.entrySet()) {
			if (dominantColor == null
					|| e.getValue().compareTo(dominantColor.getValue()) > 0) {
				dominantColor = e;
			}
		}
		color = dominantColor.getKey();

		return color;
	}

	// returns a string with a direction and a velocity (upfast, downslow,
	// leftmed, rightmed, etc.) using Template Matching
	// Sum of Absolute Differences
	private String extractMotion(BufferedImage templateFrame,
			BufferedImage futureFrame) {
		String motion = "";
		double minRedSAD = 999999999;
		double minGreenSAD = 0;
		double minBlueSAD = 0;
		double SAD = 0.0;
		double redSAD = 0.0;
		double greenSAD = 0.0;
		double blueSAD = 0.0;
		int bestRedRow = 0;
		int bestRedCol = 0;
		int bestBlueRow = 0;
		int bestBlueCol = 0;
		int bestGreenRow = 0;
		int bestGreenCol = 0;
		double bestRedSAD = 0.0;
		double bestBlueSAD = 0.0;
		double bestGreenSAD = 0.0;

		// loop through the search image
		for (int y = 0; y < 288 - 96; y++) {
			for (int x = 0; x < 352 - 88; x++) {
				// System.out.println(y);
				redSAD = 0.0;
				greenSAD = 0.0;
				blueSAD = 0.0;

				// loop through the template image
				for (int j = 0; j < 96; j++) {
					for (int i = 0; i < 88; i++) {

						int color = futureFrame.getRGB(x + i, y + j);
						int red = (color & 0x00ff0000) >> 16;
						// int green = (color & 0x0000ff00) >> 8;
						// int blue = color & 0x000000ff;

						int templateColor = templateFrame.getRGB(i, j);
						int templateRed = (templateColor & 0x00ff0000) >> 16;
						// int templateGreen = (color & 0x0000ff00) >> 8;
						// int templateBlue = color & 0x000000ff;

						// int pixel1 p_SearchIMG = futureFrame[x+i][y+j];
						// int pixel2 p_TemplateIMG = frame[i][j];

						redSAD += Math.abs((double) red - (double) templateRed);

						// greenSAD += abs( (double)green -
						// (double)templateGreen );
						// blueSAD += abs( (double)blue - (double)templateBlue
						// );
					}
				}

				// save the best found position
				// System.out.println(redSAD);
				if (minRedSAD > redSAD) {
					minRedSAD = redSAD;
					// give me VALUE_MAX
					bestRedRow = x;
					bestRedCol = y;
					bestRedSAD = redSAD;
				}
				/*
				 * if ( minGreenSAD > greenSAD ) { minGreenSAD = greenSAD; //
				 * give me VALUE_MAX bestGreenRow = x; bestGreenCol = y;
				 * bestGreenSAD = greenSAD; } if ( minBlueSAD > blueSAD ) {
				 * minBlueSAD = blueSAD; // give me VALUE_MAX bestBlueRow = x;
				 * bestBlueCol = y; bestBlueSAD = blueSAD; }
				 */

			}
		}

		System.out.println(bestRedRow);
		System.out.println(bestRedCol);

		// compute only red (gray) intensity for now

		if (bestRedRow < 144 - 96 && bestRedCol < 176 - 88) {
			motion = "UpLeft";
			System.out.println(motion);
		} else if (bestRedRow > 144 - 96 && bestRedCol < 176 - 88) {
			motion = "DownLeft";
			System.out.println(motion);
		} else if (bestRedRow < 144 - 96 && bestRedCol > 176 - 88) {
			motion = "UpRight";
			System.out.println(motion);
		} else if (bestRedRow > 144 - 96 && bestRedCol > 176 - 88) {
			motion = "DownRight";
			System.out.println(motion);
		}

		return motion;
	}

	// Returns a number 0-100
	private String extractAudio(int frameNo) {
		String audio = "";
		int sound = 0;
		int count = 0;
		/*
		 * try { audioInputStream.skip((long) (frameNo*SOUND_INDEX)); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * 
		 * try { if (readBytes != -1) { readBytes =
		 * audioInputStream.read(audioBuffer, (frameNo*SOUND_INDEX),
		 * audioBuffer.length); audio = calculateRMSLevel(audioBuffer); } }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		int readBytes = 0;
		byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
		audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e1) {
			new PlayWaveException(e1);
		} catch (IOException e1) {
			new PlayWaveException(e1);
		}

		try {
			if (readBytes != -1) {

				readBytes = audioInputStream.read(audioBuffer, 0,
						audioBuffer.length);
				// System.out.println(readBytes);

				if (readBytes >= 0) {

					// System.out.println("hi");
					// int level = 0;
					sound = calculateRMSLevel(audioBuffer);
					System.out.println(sound);
					System.out.println(count++);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "" + sound;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}