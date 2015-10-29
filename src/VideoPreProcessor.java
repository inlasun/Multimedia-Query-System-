// This file should read all the video files and preprocess their data
// and put their atributes into an xml file per video

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.Math;

import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

import javax.swing.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

public class VideoPreProcessor {

	File root; // root of where the videos are
	public static int[] byteIndicies; // keeps indexes where new frames start;
	public static byte[] bytes; // bytes from file
	public static byte[] soundBytes; // bytes from file
	public final int SOUND_INDEX = 7357;
	private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
	public static AudioInputStream audioInputStream;
	public static List<String> fileNames; // names of files
	public static Map<String, byte[]> fileBytes; // bytes of the files
	public static String[] color; // color content
	public static int[] motion; // motion content
	public static String audio; // audio content

	// constructor
	public VideoPreProcessor(String folderpath, int[] bi) {
		System.out.println("Video PreProcessor called");
		root = new File(folderpath);
		fileNames = new ArrayList<String>();
		fileBytes = new HashMap<String, byte[]>();
		// get where frames start
		byteIndicies = bi;
	}

	// goes through directories, finds files and processes them
	public void fileTraverse() {
		// System.out.println("down all the files");
		try {
			try {
				fileRecurse(root);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fileRecurse(File curr) throws XMLStreamException,
			FileNotFoundException {
		if (curr.isDirectory()) {
			// System.out.println(curr.getName());
			File contents[] = curr.listFiles();
			if (contents.length == 3 && contents[1].isFile()
					&& contents[2].isFile()) {
				// TODO This is a "video!" preprocess this
				fileNames.add(curr.getName());
				// System.out.println(contents[0]);
				// System.out.println(contents[1]);
				// System.out.println(contents[2]);
				// content[1] = flowers.rgb, content[2] = flowers.wav content[0]不知道是什么。
				processVideo(contents[1], contents[2]);
			} else {
				// recurse through other folders
				for (File f : contents) {
					fileRecurse(f);
				}
			}
		}
	}

	public List<String> getFileNames() {
		return fileNames;
	}
	
	//???
	public byte[] getFileBytes(String key) {
		return fileBytes.get(key);
	}

	// reads in this video file
	//将一个video 读到 一个bytes 这个数组中，一共有150（query clip）个frame 或 600个frame(database 里的video) 
	//每一个frame 有352*288个pixel， 每个pixel占用3个byte存储（r, g, b)
	private void readinFile(File video) throws IOException {
		InputStream is;
		is = new FileInputStream(video);

		long len = video.length();
		bytes = new byte[(int) len];

		// System.out.println("file length:"+ len);

		int offset = 0;
		int numRead = 0;
		// is.read()是指 从is里面往bytes里面读，从 offset这个下标开始，读进长度为bytes.length － offset
		// 之所以要用while 是因为 线程流 可能实际读进的小于 设定的长度，所以反复读，直到读完为止。
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// String key = (video.getName()).substring(0,
		// video.getName().indexOf("."));
		// System.out.println(key);
		// fileBytes.put(key, bytes);
	}

	// reads in sound file
	private void readinSoundFile(File sound) throws IOException {

		InputStream sis = null;
		try {
			sis = new FileInputStream(sound);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(sis == null);

		long slen = sound.length();

		soundBytes = new byte[(int) slen];

		// System.out.println("file length:"+ len);
		// audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(sound);
		} catch (UnsupportedAudioFileException e1) {
			System.out.println("fail1");
			new PlayWaveException(e1);
		} catch (IOException e1) {
			System.out.println("fail2");
			new PlayWaveException(e1);
		}
		/*
		 * System.out.println(audioInputStream == null); AudioFormat audioFormat
		 * = audioInputStream.getFormat(); Info info = new
		 * Info(SourceDataLine.class, audioFormat); System.out.println("hi");
		 * int available = 0; System.out.println(available);
		 * System.out.println("bye"); // opens the audio channel
		 * 
		 * SourceDataLine dataLine = null; dataLine = null; try { dataLine =
		 * (SourceDataLine) AudioSystem.getLine(info);
		 * dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE); } catch
		 * (LineUnavailableException e1) { new PlayWaveException(e1); }
		 * 
		 * dataLine.start();
		 */
	}

	// process the video!
	private void processVideo(File video, File audio)
			throws XMLStreamException, FileNotFoundException {
		if (!(video.getName().endsWith(".rgb") || audio.getName().endsWith(
				".wav"))) {
			System.out.println("these files aren't valid!");
			System.exit(1);
		}
		// read in the video file
		try {
			readinFile(video);
			readinSoundFile(audio);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// create XML file!!!!!!!
		String xml_name = video.getName().substring(0,
				video.getName().indexOf("."));
		xml_name += ".xml";
		// generate xml file
		XMLOutputFactory of = XMLOutputFactory.newInstance();
		XMLEventWriter ew = null;
		ew = of.createXMLEventWriter(new FileOutputStream(xml_name));

		XMLEventFactory ef = XMLEventFactory.newInstance();
		XMLEvent end = ef.createDTD("\n");
		XMLEvent tab = ef.createDTD("\t");

		// start tag
		StartDocument startDoc = ef.createStartDocument();
		ew.add(startDoc);
		ew.add(end);

		// <video>
		StartElement vidElem = ef.createStartElement("", "", "video");
		ew.add(vidElem);
		ew.add(end);

		// for each frame <frame no=i> </frame> 标记 第几个frame
		for (int i = 0; i < 600; ++i) {//此处处理database里的 video
			ew.add(tab);
			Attribute frameNo = ef.createAttribute("no", Integer.toString(i));
			List<Attribute> attrList = Arrays.asList(frameNo);
			List<Object> nsList = Arrays.asList();
			StartElement frameElem = ef.createStartElement("", "", "frame",
					attrList.iterator(), nsList.iterator());
			ew.add(frameElem);
			ew.add(end);
			// TODO: video descriptors
			extractVidDesc(ew, i);
			ew.add(tab);
			ew.add(ef.createEndElement("", "", "frame"));
			ew.add(end);
		}

		// </video>
		ew.add(ef.createEndElement("", "", "video"));
		ew.add(end);

		// end document
		ew.add(ef.createEndDocument());
		ew.close();
	}

	// extracting descriptors from the video and putting it into the xml file
	private void extractVidDesc(XMLEventWriter ew, int frameNo)
			throws XMLStreamException {

		// make frame
		int ind = byteIndicies[frameNo];
		System.out.println("curframe:" + frameNo);
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

		// Future Frame for Motion Search
		int ind2 = 0;
		if (frameNo < 599) {
			ind2 = byteIndicies[frameNo + 1]; // look 1 frames into the future
		}

		BufferedImage nextImg = new BufferedImage(352, 288,
				BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 288; y++) {
			for (int x = 0; x < 352; x++) {
				// System.out.println("i:" + ind);
				byte a = 0;
				byte r = bytes[ind2];
				byte g = bytes[ind2 + 288 * 352];
				byte b = bytes[ind2 + 288 * 352 * 2];

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8)
						| (b & 0xff);
				// int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				nextImg.setRGB(x, y, pix);
				ind2++;
			}
		}
		BufferedImage next[] = new BufferedImage[4];
		next[0] = nextImg.getSubimage(88, 72, 88, 72);
		next[1] = nextImg.getSubimage(176, 72, 88, 72);
		next[2] = nextImg.getSubimage(88, 144, 88, 72);
		next[3] = nextImg.getSubimage(176, 144, 88, 72);
		// Template image for motion search (take the 6th subimage, row-wise
		// from the main image, 3 rows 4 cols)
		// take 4rows and 4 cols each one 88,72/

		BufferedImage[] templateImg = new BufferedImage[4];

		templateImg[0] = img.getSubimage(88, 72, 88, 72);
		templateImg[1] = img.getSubimage(176, 72, 88, 72);
		templateImg[2] = img.getSubimage(88, 144, 88, 72);
		templateImg[3] = img.getSubimage(176, 144, 88, 72);

		motion = extractMotion(templateImg, next);
		color = extractColor(img);// current image
		audio = extractAudio(frameNo);// current image

		XMLEventFactory ef = XMLEventFactory.newInstance();
		XMLEvent end = ef.createDTD("\n");
		XMLEvent tab = ef.createDTD("\t");
		// Get dominant color of image
		// color = extractColor(img);
		ew.add(tab);
		ew.add(tab);

		ew.add(ef.createStartElement("", "", "color1"));
		Characters contentc0 = ef.createCharacters(color[0]);
		ew.add(contentc0);
		ew.add(ef.createEndElement("", "", "color1"));
		ew.add(end);
		ew.add(tab);
		ew.add(tab);

		ew.add(ef.createStartElement("", "", "color2"));
		Characters contentc1 = ef.createCharacters(color[1]);
		ew.add(contentc1);
		ew.add(ef.createEndElement("", "", "color2"));
		ew.add(end);
		ew.add(tab);
		ew.add(tab);

		ew.add(ef.createStartElement("", "", "color3"));
		Characters contentc2 = ef.createCharacters(color[2]);
		ew.add(contentc2);
		ew.add(ef.createEndElement("", "", "color3"));
		ew.add(end);
		ew.add(tab);
		ew.add(tab);

		ew.add(ef.createStartElement("", "", "motion1"));
		Characters contentm0 = ef.createCharacters(Integer.toString(motion[0]));
		ew.add(contentm0);
		ew.add(ef.createEndElement("", "", "motion"));
		ew.add(end);
		ew.add(tab);
		ew.add(tab);
		ew.add(ef.createStartElement("", "", "motion2"));
		Characters contentm1 = ef.createCharacters(Integer.toString(motion[1]));
		ew.add(contentm1);
		ew.add(ef.createEndElement("", "", "motion2"));
		ew.add(end);
		ew.add(tab);
		ew.add(tab);
		ew.add(ef.createStartElement("", "", "motion3"));
		Characters contentm2 = ef.createCharacters(Integer.toString(motion[2]));
		ew.add(contentm2);
		ew.add(ef.createEndElement("", "", "motion3"));
		ew.add(end);
		ew.add(tab);
		ew.add(tab);
		ew.add(ef.createStartElement("", "", "motion4"));
		Characters contentm3 = ef.createCharacters(Integer.toString(motion[3]));
		ew.add(contentm3);
		ew.add(ef.createEndElement("", "", "motion4"));
		ew.add(end);
		// Get relative audio
		// audio = extractAudio(frameNo);

		ew.add(ef.createStartElement("", "", "audio"));
		Characters contenta = ef.createCharacters(audio);
		ew.add(contenta);
		ew.add(ef.createEndElement("", "", "audio"));
		ew.add(end);

	}

	// extract dominant color of this frame ( or really just the hue)
	// shouldn't have to examine every pixel, could subsample?
	// use hues since that seems to be simple since it's one number
	public String[] extractColor(BufferedImage frame) {

		int color1Count = 0;
		int color2Count = 0;
		int color3Count = 0;
		int color4Count = 0;
		int color5Count = 0;
		int color6Count = 0;
		int color7Count = 0;
		int color8Count = 0;
		int[] colorCount = { 0, 0, 0, 0, 0, 0, 0, 0 };
		for (int j = 0; j < frame.getHeight(); ++j) {
			for (int i = 0; i < frame.getWidth(); i += 4) {
				// get rgb value of this pixel
				int rgb = frame.getRGB(i, j);
				int r = (rgb & 0x00FF0000) >>> 16;
				int g = (rgb & 0x0000FF00) >>> 8;
				int b = (rgb & 0x000000FF);

				// convert RGB to HSB
				float hsb[] = null;
				hsb = Color.RGBtoHSB(r, g, b, null);

				if ((hsb[0] >= 0) && (hsb[0] < 0.125)) {
					color1Count++;
					colorCount[0]++;
				} else if ((hsb[0] >= 0.125) && (hsb[0] < 0.25)) {
					color2Count++;
					colorCount[1]++;
				} else if ((hsb[0] >= 0.25) && (hsb[0] < 0.375)) {
					color3Count++;
					colorCount[2]++;
				} else if ((hsb[0] >= 0.375) && (hsb[0] < 0.5)) {
					color4Count++;
					colorCount[3]++;
				} else if ((hsb[0] >= 0.5) && (hsb[0] < 0.625)) {
					color5Count++;
					colorCount[4]++;
				} else if ((hsb[0] >= 0.625) && (hsb[0] < 0.75)) {
					color6Count++;
					colorCount[5]++;
				} else if ((hsb[0] >= 0.75) && (hsb[0] < 0.875)) {
					color7Count++;
					colorCount[6]++;
				} else if ((hsb[0] >= 0.875) && (hsb[0] < 1)) {
					color8Count++;
					colorCount[7]++;
				}
			}
		}
		// ---------------rank--------------------
		int len = colorCount.length;
		int[] sort = { 0, 0, 0, 0, 0, 0, 0, 0 };
		for (int x = 0; x < len - 1; x++) {
			for (int y = x + 1; y < len; y++) {
				if (colorCount[x] < colorCount[y]) {
					int temp = colorCount[x];
					colorCount[x] = colorCount[y];
					colorCount[y] = temp;
				}
			}
			sort[x] = colorCount[x];
		}
		sort[len - 1] = colorCount[len - 1];
		String[] rank = { "", "", "", "", "", "", "", "" };
		for (int i = 0; i < len; i++) {
			if (sort[i] == color1Count)
				rank[i] = "color1";
			else if (sort[i] == color2Count)
				rank[i] = "color2";
			else if (sort[i] == color3Count)
				rank[i] = "color3";
			else if (sort[i] == color4Count)
				rank[i] = "color4";
			else if (sort[i] == color5Count)
				rank[i] = "color5";
			else if (sort[i] == color6Count)
				rank[i] = "color6";
			else if (sort[i] == color7Count)
				rank[i] = "color7";
			else if (sort[i] == color8Count)
				rank[i] = "color8";
		}
		
		return rank;
	}

	// returns a string with a direction and a velocity (upfast, downslow,
	// leftmed, rightmed, etc.) using Template Matching
	// Sum of Absolute Differences
	private int[] extractMotion(BufferedImage[] templateFrame,
			BufferedImage[] futureFrame) {
		int[] motion = new int[4];

		// loop through the template image
		for (int j = 0; j < 72; j++) {
			for (int i = 0; i < 88; i++) {

				int color = futureFrame[0].getRGB(i, j);
				int templateColor = templateFrame[0].getRGB(i, j);
				if (Math.abs((double) color - (double) templateColor) > 10) {
					motion[0] += 1;
				}
				color = futureFrame[1].getRGB(i, j);
				templateColor = templateFrame[1].getRGB(i, j);
				if (Math.abs((double) color - (double) templateColor) > 10) {
					motion[1] += 1;
				}
				color = futureFrame[2].getRGB(i, j);
				templateColor = templateFrame[2].getRGB(i, j);
				if (Math.abs((double) color - (double) templateColor) > 10) {
					motion[2] += 1;
				}
				color = futureFrame[3].getRGB(i, j);
				templateColor = templateFrame[3].getRGB(i, j);
				if (Math.abs((double) color - (double) templateColor) > 10) {
					motion[3] += 1;
				}
			}
		}

		return motion;
	}

	private String extractAudio(int frameNo) {
		String audio = "";
		int sound = 0;
		int count = 0;
		int readBytes = 0;
		byte[] audioBuffer = new byte[SOUND_INDEX];

		try {
			if (readBytes != -1) {

				readBytes = audioInputStream.read(audioBuffer, 0,
						audioBuffer.length);
				if (readBytes >= 0) {
					sound = calculateRMSLevel(audioBuffer);
					// System.out.println("Sound:"+sound);
					// System.out.println("sound count?:"+count++);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "" + sound;
	}

	// Returns the Root Mean Square of the audio data at a given frame.
	public int calculateRMSLevel(byte[] audioData) {
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
}
