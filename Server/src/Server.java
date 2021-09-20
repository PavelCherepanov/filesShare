import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

public class Server {

    // Список массивов для хранения информации о полученных файлах
    static ArrayList<MyFile> myFiles = new ArrayList<>();
    // Узнаем список файлов в папке Downloads на севере
    public static File f = new File("Downloads\\");
    public static ArrayList<String> Dir = new ArrayList<String>(Arrays.asList(f.list()));


    public static boolean isDir;

    public static void main(String[] args) throws IOException {

        int fileId = 0;


        JFrame jFrame = new JFrame("Server");
        jFrame.setSize(500, 500);
        ImageIcon img = new ImageIcon("server.png");
        jFrame.setIconImage(img.getImage());
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создаем панель, которая будет содержать заголовок и другие jpanel
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel jlTitle = new JLabel("Server");
        jlTitle.setFont(new Font("Arial", Font.ITALIC, 25));
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);


        jFrame.add(jlTitle);
        jFrame.add(jScrollPane);
        // Показываем весь GUI
        jFrame.setVisible(true);

        // Создаем серверный сокет, который сервер будет слушать
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println(serverSocket.getInetAddress());


        // Показываем этот список файлов
        for (String name: Dir) {
            if (Dir.size() != 0){
                isDir = true;
            }
            JPanel jpFileOldRow = new JPanel();
            jpFileOldRow.setLayout(new BoxLayout(jpFileOldRow, BoxLayout.X_AXIS));
            // Задаем имя файла
            JLabel jlFileOldName = new JLabel(name);
            jlFileOldName.setFont(new Font("Arial", Font.BOLD, 20));
            jlFileOldName.setBorder(new EmptyBorder(10,0, 10,0));
            jpFileOldRow.setName((String.valueOf(fileId)));
            jpFileOldRow.addMouseListener(getMyMouseListener());
            jpFileOldRow.add(jlFileOldName);
            jPanel.add(jpFileOldRow);
            // Проверяем, что все компоненты расположены в контейнере как надо
            jFrame.validate();
            fileId++;
        }

        while (true) {
            try {
                // Ждем когда клиент подключится и когда он создаст сокет для связи с сервером
                Socket socket = serverSocket.accept();

                // Stream для получения данных от клиента через сокет
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                System.out.println(socket.getInetAddress());;
                // Считаем размер имени файла, чтобы знать, когда прекратить чтение.
                int fileNameLength = dataInputStream.readInt();
                // Если файл существует
                if (fileNameLength > 0) {
                    // Создаем байтовый массив для хранения имени файла
                    byte[] fileNameBytes = new byte[fileNameLength];
                    // Чтение из входного потока в массив байтов
                    dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                    // Создаем имя файла из массива байтов
                    String fileName = new String(fileNameBytes);
                    // Сколько данных ожидать от содержимого файла
                    int fileContentLength = dataInputStream.readInt();
                    if (fileContentLength > 0) {
                        // Создаем массив для хранения данных файла.
                        byte[] fileContentBytes = new byte[fileContentLength];
                        // Чтение из входного потока в массив байтов
                        dataInputStream.readFully(fileContentBytes, 0, fileContentBytes.length);
                        // Панель для хранения изображения и имени файла.
                        JPanel jpFileRow = new JPanel();
                        jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.X_AXIS));

                        JLabel jlFileName = new JLabel(fileName);
                        jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
                        jlFileName.setBorder(new EmptyBorder(10,0, 10,0));
                        if (getFileExtension(fileName).equalsIgnoreCase("txt")) {
                            // Задаем имя fileId, чтобы вы могли получить нужный файл из панели
                            jpFileRow.setName((String.valueOf(fileId)));
                            // Добавляем слушателя мыши, чтобы при щелчке по нему появлялось всплывающее окно
                            jpFileRow.addMouseListener(getMyMouseListener());
                            // Добавляем все
                            jpFileRow.add(jlFileName);
                            jPanel.add(jpFileRow);
                            jFrame.validate();
                        } else {
                            // Задаем имя fileId, чтобы вы могли получить нужный файл из панели
                            jpFileRow.setName((String.valueOf(fileId)));
                            // Добавляем слушателя мыши, чтобы при щелчке по нему появлялось всплывающее окно
                            jpFileRow.addMouseListener(getMyMouseListener());
                            // Добавляем имя файла и тип изображения на панель, а затем добавляем панель на родительскую панель
                            jpFileRow.add(jlFileName);
                            jPanel.add(jpFileRow);

                            jFrame.validate();
                        }

                        // Добавляем новый файл в список
                        myFiles.add(new MyFile(fileId, fileName, fileContentBytes, getFileExtension(fileName)));
                        Dir.add(fileName);
                        // Увеличиваем fileId для следующего файла
                        fileId++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param fileName
     * @return Тип расширения файла.
     */
    public static String getFileExtension(String fileName) {
        // Получаем тип файла
        int i = fileName.lastIndexOf('.');
        // Если есть расширение
        if (i > 0) {
            // Установите расширение для имени файла
            return fileName.substring(i + 1);
        } else {
            return "Расширение не найдено. ";
        }
    }

    /**
            * При нажатии на jpanel появляется всплывающее окно, чтобы сказать, хочет ли пользователь загрузить
      * выбранный документ
            *
            * @return Mouselistener, который используется jpanel.
      */
    public static MouseListener getMyMouseListener() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPanel jPanel = (JPanel) e.getSource();
                // Получаем идентификатор файла
                int fileId = Integer.parseInt(jPanel.getName());
                System.out.println(fileId >= Dir.size()-1);
                System.out.println(fileId);
                System.out.println(Dir.size());
                // Узнаем какой файл выбран
                if (fileId >= Dir.size()-1) {

                    for (MyFile myFile : myFiles) {
                        if (myFile.getId() == fileId) {
                            JFrame jfPreview = createFrame2(myFile.getName(), myFile.getData(), myFile.getFileExtension());
                            jfPreview.setVisible(true);

                        }

                    }


                }else{
                    for (int i = 0; i < Dir.size(); i++) {
                        if (i == fileId) {

                            Path p1 = Paths.get("Downloads\\" + Dir.get(i));
                            JFrame jfPreview = null;
                            try {
                                jfPreview = createFrame(Dir.get(i), Files.readAllBytes(p1), getFileExtension(Dir.get(i)));
                                jfPreview.setVisible(true);

                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                }

            }


            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
    }

    public static JFrame createFrame(String fileName, byte[] fileData, String fileExtension) {

        JFrame jFrame = new JFrame("File Downloader");
        jFrame.setSize(400, 400);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JLabel jlTitle = new JLabel("Server");
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));

        JLabel jlPrompt = new JLabel("Вы уверены, что хотите сохранить " + fileName + "?");
        jlPrompt.setFont(new Font("Arial", Font.BOLD, 20));
        jlPrompt.setBorder(new EmptyBorder(20,0,10,0));
        jlPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кнопка да
        JButton jbYes = new JButton("Да");
        jbYes.setPreferredSize(new Dimension(150, 75));
        jbYes.setFont(new Font("Arial", Font.BOLD, 20));
        // Кнопка нет
        JButton jbNo = new JButton("Нет");
        jbNo.setPreferredSize(new Dimension(150, 75));
        jbNo.setFont(new Font("Arial", Font.BOLD, 20));

        // Панель для кнопок
        JLabel jlFileContent = new JLabel();
        jlFileContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButtons = new JPanel();
        jpButtons.setBorder(new EmptyBorder(20, 0, 10, 0));

        jpButtons.add(jbYes);
        jpButtons.add(jbNo);

        // Если файл текстовый, отображаем текст
        if (fileExtension.equalsIgnoreCase("txt")) {
            jlFileContent.setText("<html>" + "<p>" + new String(fileData) + "</p>"+ "</html>");
            //  Если файл не текстовый, сделаем его изображением
        } else {
            jlFileContent.setIcon(new ImageIcon(fileData));
        }

        // Скачиваем файл
        jbYes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    //Если пользователь нажал на существующий файл

                        try {
                            System.out.println("Уже есть");
                            JFrame jmFrame = new JFrame();
                            String getIp = JOptionPane.showInputDialog(jmFrame, "Server ip " + getMyIp() + "Enter ip for client", "localhost");
                            File fileToDownload = new File("Downloads\\" + fileName);
                            // Создаем входной поток в файл, который хотим отправить
                            FileInputStream fileInputStream = new FileInputStream(fileToDownload.getAbsolutePath());
                            // Создаем сокетное соединение для соединения с сервером
                            Socket socket = new Socket(getIp, 1235);
                            System.out.println(socket.getInetAddress());
                            // Создаем выходной поток для записи на сервер через соединение сокета
                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            // Получаем имя файла, которое мы хотите отправить и сохраняем его
                            String fileName = fileToDownload.getName();
                            // Преобразуйте имя файла в массив байтов для отправки на сервер
                            byte[] fileNameBytes = fileName.getBytes();
                            // Создаем байтовый массив размером с файл
                            byte[] fileBytes = new byte[(int) fileToDownload.length()];
                            // Помещаем содержимое файла в массив байтов для отправки, чтобы эти байты можно было отправить на сервер.
                            fileInputStream.read(fileBytes);
                            // Отправляем длину имени файла, чтобы сервер знал, когда прекратить чтение
                            dataOutputStream.writeInt(fileNameBytes.length);
                            // Отправляем имя файла
                            dataOutputStream.write(fileNameBytes);
                            // Отправляем длину массива байтов, чтобы сервер знал, когда прекратить чтение
                            dataOutputStream.writeInt(fileBytes.length);
                            // Отправляем файл
                            dataOutputStream.write(fileBytes);
                            jFrame.dispose();
                            /*socket.close();*/
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }



            }
        });

        // Кнопка нет
        jbNo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Закрываем окно
                jFrame.dispose();
            }
        });

        // Добавляем все элементы
        jPanel.add(jlTitle);
        jPanel.add(jlPrompt);
        jPanel.add(jlFileContent);
        jPanel.add(jpButtons);

        jFrame.add(jPanel);

        return jFrame;
    }

    public static JFrame createFrame2(String fileName, byte[] fileData, String fileExtension) {

        JFrame jFrame = new JFrame("File Downloader");
        jFrame.setSize(400, 400);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JLabel jlTitle = new JLabel("Server");
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));

        JLabel jlPrompt = new JLabel("Вы уверены, что хотите сохранить " + fileName + "?");
        jlPrompt.setFont(new Font("Arial", Font.BOLD, 20));
        jlPrompt.setBorder(new EmptyBorder(20,0,10,0));
        jlPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кнопка да
        JButton jbYes = new JButton("Да");
        jbYes.setPreferredSize(new Dimension(150, 75));
        jbYes.setFont(new Font("Arial", Font.BOLD, 20));
        // Кнопка нет
        JButton jbNo = new JButton("Нет");
        jbNo.setPreferredSize(new Dimension(150, 75));
        jbNo.setFont(new Font("Arial", Font.BOLD, 20));

        // Панель для кнопок
        JLabel jlFileContent = new JLabel();
        jlFileContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButtons = new JPanel();
        jpButtons.setBorder(new EmptyBorder(20, 0, 10, 0));

        jpButtons.add(jbYes);
        jpButtons.add(jbNo);

        // Если файл текстовый, отображаем текст
        if (fileExtension.equalsIgnoreCase("txt")) {
            jlFileContent.setText("<html>" + "<p>" + new String(fileData) + "</p>"+ "</html>");
            //  Если файл не текстовый, сделаем его изображением
        } else {
            jlFileContent.setIcon(new ImageIcon(fileData));
        }

        // Скачиваем файл
        jbYes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Если пользователь нажал на существующий файл

                    try {
                        System.out.println(" еще нет ");
                        File fileToDownload = new File("Downloads\\" + fileName);

                        // Создаем поток для записи данных в файл.
                        FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload);
                        // Записываем данные файла в файл
                        fileOutputStream.write(fileData);
                        // Закрываем поток
                        fileOutputStream.close();

                        // Закрываем окно
                        jFrame.dispose();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }


            }
        });

        // Кнопка нет
        jbNo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Закрываем окно
                jFrame.dispose();
            }
        });

        // Добавляем все элементы
        jPanel.add(jlTitle);
        jPanel.add(jlPrompt);
        jPanel.add(jlFileContent);
        jPanel.add(jpButtons);

        jFrame.add(jPanel);

        return jFrame;
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
}