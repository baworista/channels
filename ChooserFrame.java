import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChooserFrame extends JFrame implements ActionListener {
    boolean isChosedFirst;
    boolean isChosedSecond;
    JButton namesButton;
    JButton linksButton;
    static JButton confirmButton;
    JLabel namesPassText = new JLabel();
    JLabel linksPassText = new JLabel();
    static JLabel informationText = new JLabel();
    String namesPass;
    String linksPass;
    static String notImagePass;
    static String picturesPass;

    public ChooserFrame() {
        isChosedFirst = false;
        isChosedSecond = false;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new GridLayout(3,3));
        this.setPreferredSize(new Dimension(800,300));

        namesButton = new JButton("Select file with names");
        namesButton.addActionListener(this);
        linksButton = new JButton("Select file with links");
        linksButton.addActionListener(this);
        confirmButton = new JButton("Confirm button");
        confirmButton.addActionListener(this);

        namesPassText.setText("Nothing chose");
        linksPassText.setText("Nothing chose");
        informationText.setText("No convertation made yet");

        this.add(namesButton);
        this.add(namesPassText);
        this.add(linksButton);
        this.add(linksPassText);
        this.add(confirmButton);
        this.add(informationText);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==namesButton){
            JFileChooser namesChooser = new JFileChooser();
            int response = namesChooser.showOpenDialog(null); //select file to open
            if (response == JFileChooser.APPROVE_OPTION){
                namesPass = namesChooser.getSelectedFile().getAbsolutePath();
                namesPassText.setText(namesPass);
                isChosedFirst = true;
            }
        }
        if (e.getSource()==linksButton){
            JFileChooser linksChooser = new JFileChooser();
            int response = linksChooser.showOpenDialog(null); //select file to open
            if (response == JFileChooser.APPROVE_OPTION){
                linksPass = linksChooser.getSelectedFile().getAbsolutePath();
                linksPassText.setText(linksPass);
                isChosedSecond = true;
            }
        }
        if (e.getSource()==confirmButton){
            if (isChosedFirst && isChosedSecond){
                isChosedFirst = false;
                isChosedSecond = false;
                ConvertAndDownload(namesPass, linksPass);
            } else
                informationText.setText("You didn't chose one or two passes");
        }
    }

    public void ConvertAndDownload(String namePass, String linksPass) {
        picturesPass = "..\\pictures\\";
        notImagePass = "..\\notFoundImage.png";
        ArrayList<Channel> channels = new ArrayList<>();
        try {
            channels = FileToChannelArray(namePass, linksPass);
        } catch(Exception exc) {
            System.out.println("Creation is unsuccessful: " + exc);
        }
        //===================================================================================================
        downloadPictures(channels);
        namesPassText.setText("Nothing chose now, previous pass: " + namesPass);
        linksPassText.setText("Nothing chose now, previous pass: " + linksPass);
        informationText.setText("Converting and downloading finished");
    }

    public static void downloadPictures(ArrayList<Channel> channels){
        File output;
        BufferedImage imageNotFound = null;
        BufferedImage newBack;
        Image srcImage;
        Image tmp;
        BufferedImage dimg;
        try {
            imageNotFound = ImageIO.read(new File(notImagePass));
        } catch (IOException e) {
            System.out.println("There is no picture for not founded file OR no void picture");
        }
        //==============================
        //==============================Making pictures and checking if link exists
        //==============================
        for (Channel channel : channels) {
            if (!channel.ifLinkExists()) {  //=====link exists
                try (InputStream in = URI.create(channel.getLink()).toURL().openStream()){
                    srcImage = ImageIO.read(in);
                    if ((srcImage.getWidth(null)/(double)(srcImage.getHeight(null))) != 1.6666666666666667){
                        tmp = srcImage.getScaledInstance(75,75,Image.SCALE_DEFAULT);
                        dimg = new BufferedImage(75,75,BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = dimg.createGraphics();
                        g2d.drawImage(tmp, 0, 0, null);
                        g2d.dispose();
                        newBack = new BufferedImage(125, 75, BufferedImage.BITMASK);
                        newBack.createGraphics().drawImage(dimg,25,0,null);
                        ImageIO.write(newBack, "png", new File(picturesPass + channel.getName() + ".png"));
                    } else {
                        dimg = new BufferedImage(srcImage.getWidth(null),srcImage.getHeight(null),BufferedImage.TYPE_INT_ARGB);
                        dimg.getGraphics().drawImage(srcImage,0,0,null);
                        ImageIO.write(dimg, "png", new File(picturesPass + channel.getName() + ".png"));
                    }
                } catch (IOException e) {  //=====link exists but source not founded
                    output = new File(picturesPass + channel.getName() + ".png");
                    try {
                        ImageIO.write(imageNotFound, "png",output);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {  //=====link doesnt exists
                output = new File(picturesPass + channel.getName() + ".png");
                try {
                    ImageIO.write(imageNotFound, "png",output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static ArrayList<Channel> FileToChannelArray(String passNames, String passLinks) throws IOException {
        //==============================
        //==============================Creating tmp arrays and array of channel objects
        //==============================
        ArrayList<Channel> channelsList = new ArrayList<>();
        ArrayList<String> tmpId = new ArrayList<>();
        ArrayList<String> tmpNames = new ArrayList<>();
        //==============================
        //==============================Reading ids and links from first file to the tmp arrays
        //==============================
        String line;
        String channelInfo = null;
        BufferedReader br;
        br = new BufferedReader(new FileReader(passNames));
        Pattern patternId = Pattern.compile("(?<=id=\")(.*?)(?=\">)");
        Pattern patternNameFirst = Pattern.compile("(?<=>)(.*?)(?=:<)");
        Pattern patternNameSecond = Pattern.compile("(?<=>)(.*?)(?=:http)");
        while ((line = br.readLine()) != null){
            if (line.matches("(.*)(?=<channel id)(.*)")) {
                channelInfo = line;
                while (!line.matches("(.*)(?=<.channel)(.*)")) {
                    channelInfo = br.readLine();
                }
                Matcher matcherNameFirst = patternNameFirst.matcher(line);
                Matcher matcherNameSecond = patternNameSecond.matcher(line);
                Matcher matcherNameThird = patternNameSecond.matcher(line);
                Matcher matcherId = patternId.matcher(line);
                if (matcherId.find()){
                    tmpId.add(matcherId.group());
                    if (matcherNameFirst.find()){
                        tmpNames.add(matcherNameFirst.group().replace(':','_').toUpperCase());
                    }
                    else if (matcherNameSecond.find()){
                        tmpNames.add(matcherNameSecond.group().replace(':','_').toUpperCase());
                    }
                    else if (matcherNameThird.find()){
                        tmpNames.add(matcherNameSecond.group().replace(':','_').toUpperCase());
                    }
                    else System.out.println(matcherId.group());
                }
            }
        }
        //==============================
        //==============================Adding ids and links to main channels list
        //==============================
        for (int i = 0; i < tmpId.size(); i++) {
            channelsList.add(new Channel(tmpId.get(i), tmpNames.get(i)));
        }
        //==============================
        //==============================Reading names for existing ids
        //==============================
        br = new BufferedReader(new FileReader(passLinks));
        Pattern patternLink = Pattern.compile("(?<=src=\")(.*?)(?=\")");
        while (((line = br.readLine()) != null)) {
            if (line.matches("(.*)(?=<channel id)(.*)")) {
                channelInfo = line;
                while (!line.matches("(.*)(?=<.channel)(.*)")) {
                    line = br.readLine();
                    channelInfo += line;
                }
                Matcher matcherId = patternId.matcher(channelInfo);
                Matcher matcherLink = patternLink.matcher(channelInfo);
                if (matcherId.find()) {   //if found id in the second file line
                    if (matcherLink.find()) {   //if found link
                        for (Channel channel : channelsList) {
                            if (matcherId.group().equals(channel.getId()))
                                channel.setLink(matcherLink.group());
                        }
                    }
                }
            }
        }
        //==============================
        //==============================Returning
        //==============================
        return channelsList;
    }
}
