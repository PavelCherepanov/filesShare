import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Enumeration;

public class Client {

    public static void main(String[] args) throws IOException {

        /*Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                System.out.println(i.getHostAddress());
            }
        }*/



        final File[] fileToSend = new File[1];

        JFrame jmFrame = new JFrame();
        String getIp = JOptionPane.showInputDialog(jmFrame, "Client ip " + getMyIp() + "." + " Enter ip for server ", "192.168.0");

        JFrame jFrame = new JFrame("Client");
        jFrame.setSize(450, 450);
        ImageIcon img = new ImageIcon("icon.png");
        jFrame.setIconImage(img.getImage());
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel jlTitle = new JLabel("Client");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20,0,10,10));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlFileName = new JLabel("Choose a file to send.");
        jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
        jlFileName.setBorder(new EmptyBorder(50, 0, 0, 0));
        jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        //Кнопки
        JPanel jpButton = new JPanel();
        jpButton.setBorder(new EmptyBorder(75, 0, 10, 0));
        JButton jbSendFile = new JButton("Send File");
        jbSendFile.setPreferredSize(new Dimension(150, 75));
        jbSendFile.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbChooseFile = new JButton("Choose File");
        jbChooseFile.setPreferredSize(new Dimension(150, 75));
        jbChooseFile.setFont(new Font("Arial", Font.BOLD, 20));

        jpButton.add(jbSendFile);
        jpButton.add(jbChooseFile);


        // Действие кнопки для выбора файла.
        // Это внутренний класс, поэтому нам нужно, чтобы fileToSend был final
        jbChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Создаем диалоговое окно
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Choose a file to send.");
                //Если файл был выбран
                if (jFileChooser.showOpenDialog(null)  == JFileChooser.APPROVE_OPTION) {
                    // Получаем выбранный файл
                    fileToSend[0] = jFileChooser.getSelectedFile();
                    jlFileName.setText("The file you want to send is: " + fileToSend[0].getName());
                }
            }
        });


        // Отправляет файл при нажатии кнопки
        jbSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Если файл еще не выбран
                if (fileToSend[0] == null) {
                    jlFileName.setText("Please choose a file to send first!");
                } else {
                    try {
                        // Создаем входной поток в файл, который хотим отправить
                        FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath());

                        // Создайте сокетное соединение для соединения с сервером.
                        Socket socket = new Socket(getIp, 1234);
                        // Создаем сокетный поток
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        // Получаем имя файла, который вы хотите отправить
                        String fileName = fileToSend[0].getName();
                        // Преобразуем имя файла в массив байтов для отправки на сервер
                        byte[] fileNameBytes = fileName.getBytes();
                        // Создаем байтовый массив размером с файл
                        byte[] fileBytes = new byte[(int)fileToSend[0].length()];
                        // Помещаем содержимое файла в массив байтов для отправки на сервер
                        fileInputStream.read(fileBytes);
                        // Отправляем длину имени файла, чтобы сервер знал, когда прекратить чтение
                        dataOutputStream.writeInt(fileNameBytes.length);
                        // Отправляем имя файла
                        dataOutputStream.write(fileNameBytes);
                        // Отправляем длину массива байтов, чтобы сервер знал, когда прекратить чтение
                        dataOutputStream.writeInt(fileBytes.length);
                        // Отправляем файл.
                        dataOutputStream.write(fileBytes);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        jFrame.add(jlTitle);
        jFrame.add(jlFileName);
        jFrame.add(jpButton);

        jFrame.setVisible(true);

        ServerSocket serverSocket = new ServerSocket(1235);
        Socket socket = serverSocket.accept();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        // Прочтите размер имени файла, чтобы знать, когда прекратить чтение.
        int fileNameLength = dataInputStream.readInt();
        // Если файл существует
        if (fileNameLength > 0) {
            // Байтовый массив для хранения имени файла.
            byte[] fileNameBytes = new byte[fileNameLength];
            // Чтение из входного потока в массив байтов.
            dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
            // Создайте имя файла из массива байтов.
            String fileName = new String(fileNameBytes);
            File fileToDownload = new File("Client\\Downloads\\" + fileName);
            // Создайте поток для записи данных в файл.
            FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload.getAbsolutePath());
            byte[] buffer = new byte[(int)fileToDownload.length()];
            fileOutputStream.write(buffer);

            int fileContentLength = dataInputStream.readInt();

            // Массив для хранения данных файла
            byte[] fileContentBytes = new byte[fileContentLength];
            // Чтение из входного потока в массив fileContentBytes
            dataInputStream.readFully(fileContentBytes, 0, fileContentBytes.length);
            fileOutputStream.write(fileContentBytes);
            fileOutputStream.close();
        }
    }

    public static String getMyIp(){
        try {
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            while (nifs.hasMoreElements()) {
                NetworkInterface nif = nifs.nextElement();

                if (nif.getName().startsWith("wlan")) {
                    Enumeration<InetAddress> addresses = nif.getInetAddresses();

                    while (addresses.hasMoreElements()) {

                        InetAddress addr = addresses.nextElement();
                        if (addr.getAddress().length == 4) {
                            String ip = addr.getHostAddress();
                            System.out.println(ip);
                            return ip;
                        }
                    }
                }
            }

        } catch (SocketException ex) {
            ex.printStackTrace(System.err);
        }

        return null;
    }

    /*public static void checkHosts(String subnet) throws IOException {
        int timeout=1;
        for (int i=1;i<255;i++){
            String host=subnet + "." + i;
            if (InetAddress.getByName(host).isReachable(timeout)){
                System.out.println(host + " is reachable");
            }
        }
    }*/
}