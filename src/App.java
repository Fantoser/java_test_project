import java.awt.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class App {

     //VARIABLES
     int windowWidth = 600;
     int windowHeight = 500;
 
     //INITIALIZATION OF GUI ELEMENTS
     JFrame frame = new JFrame("Java Test Project");
     JPanel panel = new JPanel();
     JLabel fieldLabel = new JLabel("URL");
     JTextField textField = new JTextField("https://jsonplaceholder.typicode.com/users");
     JButton inputBtn = new JButton("Bevitel");
     JTextArea textArea = new JTextArea(8, 20);
     JScrollPane areaScrollPane = new JScrollPane(textArea);
     JScrollBar scrollBar = areaScrollPane.getVerticalScrollBar();

     public App() {

        //SET UP GRIDBAGLAYOUT
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 30, 5, 5);
        
        //"URL" LABEL
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.weighty = 0.1;
        gbc.weighty = 0;
        panel.add(fieldLabel);

        //INPUT FIELD
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(textField, gbc);
        

        gbc.insets = new Insets(5, 5, 5, 30);
        
        //INPUT BUTTON
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        panel.add(inputBtn, gbc);
        
        gbc.insets = new Insets(5, 30, 5, 30);

        //TEXTAREA WRAPPED IN SCROLLPANE
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        textArea.setEditable(false);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(areaScrollPane, gbc);

        //SET UP FRAME
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(windowWidth, windowHeight);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

     }
     
    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App();
            }
        });
    }
}
