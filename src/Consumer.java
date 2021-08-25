import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Consumer{
    private ArrayList<File> mp3Files;
    private static MediaPlayer mediaPlayer;
    private Scanner scanner;
    private Socket requestSocket;
    private static int[] brokerPorts = {8900,8901,8902}; //ports of the brokers
    private int brokerPort;
    private SongRequest ar;
    private boolean wrongArtist;
    private boolean wrongSong;
    private boolean streamOrDownload; //true if we want to stream or false if we want to download the music
    private String path;

    private InputStream inputStream;
    private ObjectInputStream objectInputStream;

    public Consumer(){
        scanner = new Scanner(System.in);
        init();
    }

    public static void main(String[] args){

        //this is so that the mediaplayer has time to get initialized
        try {
            //wait to initialize toolkit
            final CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                new JFXPanel(); // initializes JavaFX environment
                latch.countDown();
            });
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Consumer consumer = new Consumer();
    }

    public void init() {
        ar = new SongRequest();
        mp3Files = new ArrayList<>();
        streamOrDownload = true;
        wrongSong = false;
        //we reach out to a random broker
        Random r = new Random();
        brokerPort = brokerPorts[r.nextInt(3)];
        getRequest();
        connect();
    }

    //ask the consumer for the song and the artist
    private void getRequest() {
        System.out.print("Enter song: ");
        ar.setSong(scanner.nextLine());
        //if the artist was correct but the song title wasn't we don't need to ask for the artist again
        if(wrongSong)
            return;
        System.out.print("Enter artist: ");
        ar.setArtist(scanner.nextLine());
    }

    private void try_again(){
        getRequest();
        connect();
    }

    public synchronized void connect()  {
        requestSocket = null;
        //DataOutputStream  out = null;
        //DataInputStream in = null;
        byte[] song;
        try {
            //connect to the broker
            requestSocket = new Socket("localhost", brokerPort);
            inputStream = requestSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            // get the port of the broker thread that handle our request
            int threadPort = dataInputStream.readInt();
            requestSocket.close();
            //connect with the thread
            requestSocket = new Socket("localhost", threadPort);
            OutputStream outputStream = requestSocket.getOutputStream();
            inputStream = requestSocket.getInputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            //send the request
            objectOutputStream.writeObject(ar);
            objectOutputStream.flush(); // send the message

            dataInputStream = new DataInputStream(inputStream);
            //get an int that is either the port of the thread or a "code"
            int newBrokerPort = dataInputStream.readInt();
            objectInputStream = new ObjectInputStream(inputStream);

            //if the "code" is 100 then we have the wrong artist
            if(newBrokerPort==100){
                ArrayList<ArtistName> allArtists = (ArrayList<ArtistName>) objectInputStream.readObject();
                requestSocket.close();
                wrongArtist = true; //checks if any broker has the artist

                //if we have the artist but the requests for that artists are handled
                //by another broker then we get the broker port of the correct broker
                for(ArtistName artistName : allArtists){
                    if(artistName.getArtistName().equals(this.ar.getArtist())){
                        brokerPort = artistName.getBroker().getPortNumber();
                        wrongArtist=false;
                    }
                }
                if(wrongArtist) {
                    //if no brokers have the artist we want then we print all the artists available and let the
                    //consumer enter another request
                    System.out.println("You entered a wrong artist. These are the available artists : ");
                    ArrayList<String> allArtistsStrings = new ArrayList<>();
                    for(ArtistName an : allArtists){
                        allArtistsStrings.add(an.getArtistName());
                    }
                    printList(allArtistsStrings);
                    try_again();
                }
                else {
                    connect();
                }
                return;
            }
            else if(newBrokerPort==200){
                //if "code" is 200 we have a wrong song title
                //in this case we print all the available songs from the artist the consumer
                //requested and we let the consumer to add another request(only the song this time)
                wrongSong = true;
                ArrayList<String> allSongs = (ArrayList<String>) objectInputStream.readObject();
                System.out.println("You entered a wrong song. These are the available songs : ");
                printList(allSongs);
                try_again();
                return;
            }
            else{
                //in case the "code" is the broker thread's port we can wait for the chunks to be sent by the broker
                byte[] chunk = new byte[512];
                Value value = (Value) objectInputStream.readObject();
                song = new byte[value.getTotalBytes()];
                int i=0;
                //we need to reassemple the array with the chucks to get the full song
                while(!value.getTrackName().equals("last_value")){
                    System.out.println("Packet "+i);
                    chunk = value.getMusicFileExtract();
                    System.arraycopy(chunk,0, song,i*512,512);
                    value = (Value) objectInputStream.readObject();
                    i++;
                }
            }
            requestSocket.close();

            //then we ask if the consumer want to download the song or directly stream it
            System.out.println("Download(1) or Stream(2)? Enter 1 or 2.");
            String choice  = scanner.nextLine();
            //if the consumer want to download it we need a path to save it(path need to be in this form "G:\projects_katanemimena\test")
            if(choice.equals("1")) {
                streamOrDownload = false;
                System.out.println("Please enter the path of the new file:");
                path = scanner.nextLine();
            }
            else if(choice.equals("2"))
                streamOrDownload = true;

            //if we want to stream the song we create a temporary file and we play the song
            if(streamOrDownload){
                // create temp file that will hold byte array
                File tempMp3 = File.createTempFile("temp", "mp3");
                tempMp3.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(tempMp3);
                fos.write(song);
                fos.close();
                playMp3(tempMp3);
            }
            else{
                //if we want to download it we add the song title as the name of the file and we save it
                path = path.concat("\\"+ar.getSong()+".mp3");
                System.out.println(path);
                File newFile = new File(path);
                FileOutputStream fos = new FileOutputStream(newFile);
                fos.write(song);
                fos.close();
                mp3Files.add(newFile);
                //then we ask if the consumer wants to hear it, if yes we play it
                System.out.println("Want to hear the song? Y/N");
                String hearTheSong = scanner.nextLine();
                if(hearTheSong.equals("Y"))
                    playMp3(newFile);
                else
                    nextSongRequest();
            }
            //we wait for any playing song to finish in order to ask the consumer if he/she want to listen another song
            mediaPlayer.setOnEndOfMedia(() -> {
                nextSongRequest();
            });



        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void nextSongRequest() {
        System.out.println("Want to hear another song? Y/N");
        String anotherSong = scanner.nextLine();
        if(anotherSong.equals("Y"))
            init();
        else
            System.exit(0);
    }

    //print everything in a arraylist with an increasing number in the front of the names
    private void printList(ArrayList<String> allSongs) {
        int i=1;
        for(String s : allSongs){
            System.out.println((i++)+". "+s);
        }
    }

    //how the mediaPlayer plays the file
    public static void playMp3(File mp3File) {
        System.out.println("PLAY SONG");
        Media hit = new Media(mp3File.toURI().toString());
        mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }

}