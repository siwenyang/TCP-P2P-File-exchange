import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
public class client {
    public static void main(String args[]) throws Exception
    {
        if (args.length != 3 && args.length != 5 && args.length != 6){
            System.out.println("Must have 3 or 5 or 6 arguments as input");
            System.exit(0);
        }

        String serverAddress = (String) args[0];
        InetAddress IPAddress = InetAddress.getByName(serverAddress);
        int portNumber = Integer.parseInt(args[1]);
        System.out.println("port is" + portNumber + '\n');

        Socket clientSocket = new Socket(IPAddress, portNumber);
        OutputStream os = clientSocket.getOutputStream();
        DataOutputStream outToServer = new DataOutputStream(os);

        //command could be F, G, P
        String command = (String) args[2];
        String commandOption = command.substring(0,1);

        //if command is F , terminate, must have 3 arguments
        if(args.length != 3 && commandOption.equals("F")){
            System.out.println("Must have 3 arguments for upload!");
            System.exit(0);
        }
        //if command is P , upload, must have 6 arguments
        if(args.length != 6 && commandOption.equals("P")){
            System.out.println("Must have 6 arguments for upload!");
            System.exit(0);
        }
        //if command is G , download, must have 5 arguments
        if(args.length != 5 && commandOption.equals("G")){
            System.out.println("Must have 5 arguments for upload!");
            System.exit(0);
        }

        //command is F, terminate server
        if(commandOption.equals("F")){
            System.out.println("close");
            String sentence = "F";
            outToServer.writeBytes(sentence + '\n');
            clientSocket.close();
        }

        //command is G, download
        else if(commandOption.equals("G")){
            String key= command.substring(1,8);
            String fileName = (String) args[3];
            int DownloadSize = Integer.parseInt(args[4]);

            //send key to sever, ask for matching
            outToServer.writeBytes(command + '\n');
            System.out.println("send download request to server");

            //download the file, write to local
            InputStream is = clientSocket.getInputStream();
            byte[] tmp = new byte[DownloadSize];
            int bytesRead=0;
            System.out.println("Start to download");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileName);
                System.out.println("Start to write to file");
                while((bytesRead=is.read(tmp, 0, tmp.length)) != -1){
                    fos.write(tmp, 0 ,bytesRead);
                }
                fos.close();
                is.close();
                System.out.println("download successful");
            } catch (IOException ex) {
                System.out.println("fail to write to local");
            }
        }

        //command is P, upload
        else if(commandOption.equals("P")){
            String fileName = (String) args[3];
            String key= command.substring(1,8);
            int upLoadSize = Integer.parseInt(args[4]);
            int waitTime = Integer.parseInt(args[5]);

            //send key for match
            outToServer.writeBytes(command + '\n');
            System.out.println("send upload request to server");

            //upload the local file
            File uploadFile = new File(fileName);
            System.out.println(fileName);
            FileInputStream fis = new FileInputStream(uploadFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] contents;
            long fileLength = uploadFile.length();
            long current = 0;
            while(current!=fileLength){
                if(fileLength - current >= upLoadSize)
                    current += upLoadSize;
                else{
                    upLoadSize = (int)(fileLength - current);
                    current = fileLength;
                }
                contents = new byte[upLoadSize];
                bis.read(contents, 0, upLoadSize);
                os.write(contents);
                Thread.sleep(waitTime);
            }
            os.flush();
            bis.close();
            fis.close();
            clientSocket.close();
            System.out.print("uploading file completed");
        }

        //else command not acceptable
        else{
            System.out.println("Must use F, G, or P as command");
            System.exit(0);
        }
    }
}
