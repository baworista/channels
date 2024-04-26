ChooserFrame Class
The ChooserFrame class represents a Swing GUI application for choosing and processing files containing names and links. It provides buttons for selecting files, confirms the selection, and triggers the conversion and download process.

Features
File Selection: Allows users to select files containing names and links using separate buttons (namesButton and linksButton).
Confirmation: The confirmButton confirms file selection and triggers the conversion process.
Conversion and Download: Converts links to images and downloads them to specified directories based on the provided files.
Error Handling: Handles situations where links are not found or not formatted correctly.
actionPerformed Method
The actionPerformed method handles button clicks and file selections. It responds to clicks on the names and links buttons, confirms the selection, and executes the conversion and download process.

ConvertAndDownload Method
The ConvertAndDownload method processes the selected files, converts links to images, downloads them, and updates the GUI accordingly.

FileToChannelArray Method
The FileToChannelArray method reads data from the selected files, parses information such as IDs, names, and links, and creates Channel objects to represent each entry.

Channel Class
The Channel class represents a data structure for storing ID, name, and link information for each channel.

Dependencies
The code uses Swing components (JFrame, JButton, JLabel) for GUI, file handling classes (File, BufferedReader) for file operations, and image processing classes (ImageIO, BufferedImage) for image handling.
