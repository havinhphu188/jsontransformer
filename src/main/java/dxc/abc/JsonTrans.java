package dxc.abc;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import javax.swing.JLabel;

public class JsonTrans extends JFrame {
	class EditingPoint {
		public String path;
		public String tagValue;
	}
	
	private JPanel contentPane;
	private String jsonStringOnProcess ; 
	private String path = "$.store.book[2].isbn";
	private String transformRule;
	/**
	 * Launch the application.
	 */
	
	
	private ArrayList<EditingPoint>editingList = new ArrayList<EditingPoint>(); 
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JsonTrans frame = new JsonTrans();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public JsonTrans() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		setTitle("Json Transformer");
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnNewButton = new JButton("Add transform rules");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearNotification();
				JFileChooser transformRuleFile = fileChooser("Rule set", "xml");
				transformRule = getFileContent(transformRuleFile);
			}
		});
		btnNewButton.setBounds(10, 11, 145, 43);
		contentPane.add(btnNewButton);

		JButton button = new JButton("Json");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearNotification();
				JFileChooser jsonFile = fileChooser("Digesting message", "json");
				jsonStringOnProcess = getFileContent(jsonFile);
			}
		});
		button.setBounds(10, 66, 145, 43);
		contentPane.add(button);
		
		JButton btnNewButton_1 = new JButton("Execute");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TransformJsonMessage();
				copyToClipBoard();
				JLabel labelNotification = (JLabel) contentPane.getComponent(4);
				labelNotification.setText("Result has been copy to clip board");
			}
		});
		btnNewButton_1.setBounds(10, 120, 145, 43);
		contentPane.add(btnNewButton_1);
		
		JLabel lblPhacsccom = new JLabel("pha5@csc.com");
		lblPhacsccom.setBounds(338, 17, 86, 31);
		contentPane.add(lblPhacsccom);
		
		JLabel lblNotification = new JLabel("Notification");
		lblNotification.setBounds(10, 174, 414, 76);
		contentPane.add(lblNotification);
	}

	protected void copyToClipBoard() {
		// TODO Auto-generated method stub
		Toolkit.getDefaultToolkit()
        .getSystemClipboard()
        .setContents(
                new StringSelection(jsonStringOnProcess),
                null
        );
		
	}

	private void clearNotification() {
		JLabel labelNotification = (JLabel) contentPane.getComponent(4);
		labelNotification.setText("");
	}
	
	protected void TransformJsonMessage() {
		// TODO Auto-generated method stub
		getTransformRuleFromXML();
		for (EditingPoint ep: editingList) {
			jsonStringOnProcess = jsonEditByPath(jsonStringOnProcess,ep.path, ep.tagValue);
		}
	}

	private void getTransformRuleFromXML() {
		// TODO Auto-generated method stub
		try {
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(new InputSource(new StringReader(transformRule)));
	         doc.getDocumentElement().normalize();
	         System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	         NodeList nList = doc.getElementsByTagName("SetValueOnPath");
	         System.out.println("----------------------------");
	         
	         for (int temp = 0; temp < nList.getLength(); temp++) {
	            Node nNode = nList.item(temp);
	            System.out.println("\nCurrent Element :" + nNode.getNodeName());
	            
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;
	               EditingPoint ep = new EditingPoint();
	               ep.path =  eElement
	 	                  .getElementsByTagName("Path")
		                  .item(0)
		                  .getTextContent();
	               ep.tagValue = eElement
	 	                  .getElementsByTagName("ValueTag")
		                  .item(0)
		                  .getTextContent();
	               editingList.add(ep);
	            }
	         }
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	}
	
	protected String getFileContent(JFileChooser fc) {
		// TODO Auto-generated method stub
		String line = null;
		StringBuilder sb = new StringBuilder();
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fc.getSelectedFile());

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

			// Always close files.
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fc.getSelectedFile().getName() + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fc.getSelectedFile().getName() + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
		return sb.toString();
	}

	private JFileChooser fileChooser(String description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(contentPane);
		return chooser;
	}
	
	private String jsonEditByPath(String jsonString, String path, String tagValue) {
		Configuration conf = Configuration.defaultConfiguration();
		DocumentContext json = JsonPath.using(conf).parse(jsonString);
	    return json.set(path, tagValue).jsonString();
	}
}
