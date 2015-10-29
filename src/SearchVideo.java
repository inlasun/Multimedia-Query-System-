import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class SearchVideo {

	public static int frameNum1;
	public static String[][] color1 = new String[150][3];
	public static String[][] motion1 = new String[150][4];
	public static String[] audio1 = new String[150];

	public static int frameNum2;
	public static String[][] color2 = new String[600][3];
	public static String[][] motion2 = new String[600][4];
	public static String[] audio2 = new String[600];

	public static double[] colorScore = new double[450];
	public static double domainColor1 = 60;
	public static double domainColor2 = 30;
	public static double domainColor3 = 10;

	public static double[] audioScore = new double[450];
	public static int[] finalScore = new int[450];
	public static double[] proportion = { 0.8, 0.1, 0.1 };

	public static double[] motionScore = new double[450];

	public int[] BeginSearch(String argv0, String argv1) {

		try {
			String fileName1 = argv0;
			File fXmlFile1 = new File(fileName1);
			String fileName2 = argv1;
			File fXmlFile2 = new File(fileName2);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc1 = dBuilder.parse(fXmlFile1);
			Document doc2 = dBuilder.parse(fXmlFile2);

			doc1.getDocumentElement().normalize();
			doc2.getDocumentElement().normalize();

			// System.out.println("Root element :" +
			// doc1.getDocumentElement().getNodeName());

			NodeList nList1 = doc1.getElementsByTagName("frame");
			NodeList nList2 = doc2.getElementsByTagName("frame");

			frameNum1 = nList1.getLength();
			frameNum2 = nList2.getLength();

			// System.out.println("----------------------------");

			for (int temp = 0; temp < frameNum1; temp++) {

				Node nNode = nList1.item(temp);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					color1[temp][0] = eElement.getElementsByTagName("color1")
							.item(0).getTextContent();
					color1[temp][1] = eElement.getElementsByTagName("color2")
							.item(0).getTextContent();
					color1[temp][2] = eElement.getElementsByTagName("color3")
							.item(0).getTextContent();
					motion1[temp][0] = eElement.getElementsByTagName("motion1")
							.item(0).getTextContent();
					motion1[temp][1] = eElement.getElementsByTagName("motion2")
							.item(0).getTextContent();
					motion1[temp][2] = eElement.getElementsByTagName("motion3")
							.item(0).getTextContent();
					motion1[temp][3] = eElement.getElementsByTagName("motion4")
							.item(0).getTextContent();
					audio1[temp] = eElement.getElementsByTagName("audio")
							.item(0).getTextContent();
				}
			}
			for (int temp = 0; temp < frameNum2; temp++) {

				Node nNode = nList2.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					color2[temp][0] = eElement.getElementsByTagName("color1")
							.item(0).getTextContent();
					color2[temp][1] = eElement.getElementsByTagName("color2")
							.item(0).getTextContent();
					color2[temp][2] = eElement.getElementsByTagName("color3")
							.item(0).getTextContent();
					motion2[temp][0] = eElement.getElementsByTagName("motion1")
							.item(0).getTextContent();
					motion2[temp][1] = eElement.getElementsByTagName("motion2")
							.item(0).getTextContent();
					motion2[temp][2] = eElement.getElementsByTagName("motion3")
							.item(0).getTextContent();
					motion2[temp][3] = eElement.getElementsByTagName("motion4")
							.item(0).getTextContent();
					audio2[temp] = eElement.getElementsByTagName("audio")
							.item(0).getTextContent();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// call score function
		getColorScore();
		getAudioScore();
		getMotionScore();
		getFinalScore();
		return finalScore;

	}

	public static void getColorScore() {

		for (int i = 0; i < (frameNum2 - frameNum1); i++) {
			double sum = 0;
			for (int j = 0; j < frameNum1; j++) {
				if (color1[j][0].equals(color2[i + j][0])) {
					sum = sum + domainColor1;
				}
				if (color1[j][1].equals(color2[i + j][1])) {
					sum = sum + domainColor2;
				}
				if (color1[j][2].equals(color2[i + j][2])) {
					sum = sum + domainColor3;
				}
			}
			colorScore[i] = sum / frameNum1;
		}
	}

	public static void getAudioScore() {

		for (int i = 0; i < (frameNum2 - frameNum1); i++) {
			double sum = 0;
			for (int j = 0; j < frameNum1; j++) {
				if (audio1[j].equals(audio2[i + j])) {
					sum++;
				}
			}
			audioScore[i] = sum / 150 * 100;
			// System.out.println("audioScore["+i+"] = "+audioScore[i]);
		}
	}

	public static void getMotionScore() {
		int index = 0;
		int inner = 0;
		int outer = 0;
		double sum = 0.0;
		double score = 0.0;
		double fn = 0.0;

		for (outer = 0; outer < 450; outer++) {
			score = 0.0;
			for (inner = 0; inner < 149; inner++) {

				sum = Math
						.sqrt(Math.pow(
								(Double.parseDouble(motion1[inner][0]) - Double
										.parseDouble(motion2[inner + outer][0])),
								2)
								+ Math.pow(
										(Double.parseDouble(motion1[inner][1]) - Double
												.parseDouble(motion2[inner
														+ outer][1])), 2)
								+ Math.pow(
										(Double.parseDouble(motion1[inner][2]) - Double
												.parseDouble(motion2[inner
														+ outer][2])), 2)
								+ Math.pow(
										(Double.parseDouble(motion1[inner][3]) - Double
												.parseDouble(motion2[inner
														+ outer][3])), 2));
				// System.out.println(sum);
				score += (88.0 * 72.0 * 2.0) - sum;
				// System.out.println(score);
			}

			motionScore[outer] = score * 100.0 / (6336.0 * 2.0 * 149.0);//88 * 72
			// System.out.println("motionScore["+outer+"] = "+motionScore[outer]);
		}

	}

	public static void getFinalScore() {
		for (int i = 0; i < (frameNum2 - frameNum1); i++) {
			finalScore[i] = (int) (proportion[0] * colorScore[i]
					+ proportion[1] * audioScore[i] + proportion[2]
					* motionScore[i]);
		//	System.out.println("finalScore[" + i + "] = " + finalScore[i]);
		}
	}
}