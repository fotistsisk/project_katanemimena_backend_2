import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.ID3v1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Publisher implements Serializable{
    private int port;
    private ArrayList<MusicFile> songs = new ArrayList<>();
    private ArrayList<ArtistName> artists = new ArrayList<>();
    private ArrayList<Broker> brokers = new ArrayList<>();
    private ArrayList<Integer> brokerHashes = new ArrayList<>();
    private Queue<Integer> threadPorts = new LinkedList<>();

    private int i;

    public Publisher(){
        init();
    }

    public static void main(String[] args){
        Publisher p = new Publisher();
    }

    private int getPublisherFromFile(){
        String filepath = "C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//publisher_counter";
        try {
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Queue<Integer> publisher_counter = (Queue<Integer>) objectIn.readObject();
            objectIn.close();

            i = publisher_counter.poll();

            File file = new File(filepath);
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(f);
            oos.writeObject(publisher_counter);
            oos.flush();
            oos.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return i;

    }


    public void init() {
        port = getPortFromFile(); //get port from the file ports
        System.out.println(port);
        threadPorts = getThreadPorts(); //get thread ports
        i=getPublisherFromFile(); //get the number of the publisher from a file
        connect(); //get brokers and hashed of brokers



        //Load songs
        File folder = null;
        if(i==1)
            folder = new File("G:\\projects_katanemimena\\dataset\\Producer1");
        else if(i==2)
            folder = new File("G:\\projects_katanemimena\\dataset\\Producer2");
        else if(i==3)
            folder = new File("G:\\projects_katanemimena\\dataset\\Producer3");
        try{
            for(File f : folder.listFiles()){
                FileInputStream is = new FileInputStream(f);
                byte[] byte_array = new byte[(int) f.length()];
                is.read(byte_array);

                //get the metadata
                MP3File mp3file = new MP3File(f);
                ID3v1 id3v1 = mp3file.getID3v1Tag();
                String songTitle = id3v1.getSongTitle();
                String artistString = id3v1.getArtist();
                MusicFile musicFile = new MusicFile(songTitle,artistString,id3v1.getAlbumTitle(),byte_array);
                ArtistName an = findArtist(artistString);
                //if the new file is from an artist we don't have in artists arraylist we add the artist to it
                if(an==null) {
                    ArtistName artistNameObject = new ArtistName(artistString);
                    artists.add(artistNameObject);
                    artistNameObject.addSong(songTitle);
                }
                else //if not we just add the song to the artistName object
                    an.addSong(songTitle);
                songs.add(musicFile);
                is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        }

        //we sort the hashes of the brokers
        Collections.sort(brokerHashes);

        //calculate which broker has which artist
        for(ArtistName artistName : artists){
            artistName.setPublisher(this);
            int artistHash = artistName.getHash();
            //get the maxHash
            int maxHash = brokerHashes.get(brokerHashes.size()-1); //brokerHashes is sorted so we get the last element
            //if the artist hash is larger that the max hash then do artisthash mod maxhash
            if(artistHash>maxHash)
                artistHash = artistHash%maxHash;
            //sort artists into the brokers
            for(i=0;i<brokerHashes.size();i++){
                if(artistHash<=brokerHashes.get(i)){
                    addBrokerToArtist(brokerHashes.get(i),artistName); //add to object artistName the correct broker
                    break;
                }
            }
        }

        //for every broker run the following code
        for(int j=8900;j<8903;j++) {
            try {
                Socket connection = new Socket("localhost", j); //connect with the broker
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(artists); //send all the artistsNames to all brokers
                objectOutputStream.flush();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Producer initialized"); //ack message
        //we need to wait for a publisher to be initialized in order to begin the proccess of initializing a new publisher

        for(ArtistName an : artists){
            System.out.println(an.getArtistName());
        }

        //go into a while loop in order to handle requests from brokers constantly
        handleRequests();
    }

    public int getPortFromFile(){
        int port = 0;

        String filepath = "C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//publisher_ports";
        try {
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Queue<Integer> ports = (Queue<Integer>) objectIn.readObject();
            objectIn.close();

            port = ports.poll();

            File file = new File(filepath);
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(f);
            oos.writeObject(ports);
            oos.flush();
            oos.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return port;

    }

    //find the object ArtistName that has as artistname a string we give the function
    private ArtistName findArtist(String artistString) {
        for(ArtistName an :artists){
            if(an.getArtistName().equals(artistString))
                return an;
        }
        return null;
    }

    public void addBrokerToArtist(int hash, ArtistName artistName){
        for(Broker b : brokers){
            if(b.getHash()==hash){
                artistName.setBroker(b);
            }
        }
    }

    //get ports for new thread and for new publishers from the file ports.
    private Queue<Integer> getThreadPorts() {
        String filepath = "C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//publisher_ports_";
        Queue<Integer> ports = null;
        if(port==8903){
            filepath = filepath + "1";
        }
        else if(port==8904){
            filepath = filepath + "2";
        }
        try {
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            ports = (Queue<Integer>) objectIn.readObject();
            objectIn.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return ports;
    }

    private void handleRequests() {

        try {
            ServerSocket serverSocket = new ServerSocket(port); //create a serversocket to connect with brokers
            while(true){
                Socket socket = serverSocket.accept(); //accept the connection
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                //get a new port from the file ports create a new thread with that port and start it
                int newPort = threadPorts.poll();
                System.out.println(newPort);
                Thread thread = new Thread(new Publisher.PublisherThread(newPort));
                thread.start(); //the new thread will handle the request of the broker

                dataOutputStream.writeInt(newPort); //send the port of the thread
                dataOutputStream.flush();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getPortNumber(){
        return port;
    }


    public void connect(){
        //for every broker we run the following code
        for(int j=8900;j<8903;j++){
            try {
                Socket connection = new Socket("localhost",j); //we connect with the broker
                InputStream inputStream = connection.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                //we read the broker object
                Broker b = (Broker) objectInputStream.readObject();
                //we add the object to an arraylist and the hash of the object to an arraylist
                brokers.add(b);
                brokerHashes.add(b.getHash());
                connection.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public class PublisherThread implements Runnable{
        ServerSocket serverSocket;
        Socket connection;
        int port;
        InputStream inputStream;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;
        OutputStream outputStream;
        DataOutputStream dataOutputStream;

        public PublisherThread(int port){
            this.port = port;
        }

        //runs everytime we staRT A NEW THREAD
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port); //create a server socket and wait for a new connection with a broker(a broker thread)
                connection = serverSocket.accept();  //accept the connection
                inputStream = connection.getInputStream();
                outputStream = connection.getOutputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                SongRequest ar = (SongRequest) objectInputStream.readObject(); //get the request from the broker
                System.out.println("Song : "+ar.getSong()+"\nArtist : "+ar.getArtist());
                //search for the object MusicFile in order to find the correct song
                MusicFile musicFile = null;
                for(MusicFile m : songs){
                    if(ar.getSong().equals(m.getTrackName())){
                        System.out.println(m.getTrackName());
                        musicFile = m;
                        break;
                    }
                }

                //send the chunks
                objectOutputStream = new ObjectOutputStream(outputStream);
                i=0;
                int numOfBytes = musicFile.getNumOfBytes();
                while(i<(musicFile.getNumOfBytes())-512) {
                    try {
                        Value v = new Value(musicFile,i); //for every 512 bytes remaining we get a value which is a chunk of 512 bytes of the song
                        v.setTotalBytes(numOfBytes); //we need this in order to reconstruct the array in the consumer
                        objectOutputStream.writeObject(v);
                        objectOutputStream.flush();
                        i=i+512;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                //send a new value with trackName last_value in order to make the broker realize that the last one was the last chunk of the song
                objectOutputStream.writeObject(new Value(new MusicFile("last_value","","", musicFile.getMusicFileExtract()),0));
                objectOutputStream.flush();
                connection.close();
                serverSocket.close();

                threadPorts.add(port);
                System.out.println(port+" DONE");

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
