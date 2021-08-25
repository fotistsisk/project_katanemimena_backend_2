import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Broker implements Serializable{

    private ArrayList<ArtistName> artists;
    private Queue<Integer> threadPorts;
    private static ArrayList<ArtistName> allArtists;
    private int portNumber;
    private int hash;
    private String localhostip = "127.0.0.1";
    private ArrayList<String> allArtistsStrings;
    //broker 1 8002 - 8299
    //broker 2 8300 - 8599
    //broker 3 8600 - 8899

    public static void main(String[] args) throws UnknownHostException {
        //With every run we create a new broker
        Broker broker = new Broker();
    }

    public Broker(){
        artists = new ArrayList<>();
        allArtists = new ArrayList<>();
        allArtistsStrings = new ArrayList<>();
        portNumber=getPortFromFile(); //get port from a queue from a file
        System.out.println(portNumber);
        threadPorts = getThreadPorts();
        //we generate the hash for the broker
        hash = new BigInteger(DigestUtils.sha1Hex(localhostip+Integer.toString(portNumber)),16).mod(new BigInteger("100")).intValue();
        connect(); //get all the artist
        for(ArtistName a : allArtists){
            allArtistsStrings.add(a.getArtistName());
        }
        checkArtists(); //add to the artist arrayList only the artist that this broker is responsible for
        handleRequests(); //add an endless loop where the broker handles requests
    }

    private Queue<Integer> getThreadPorts() {
        String filepath = "C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//broker_ports_";
        Queue<Integer> ports = null;
        if(portNumber==8900){
            filepath = filepath + "1";
        }
        else if(portNumber==8901){
            filepath = filepath + "2";
        }
        else if(portNumber==8902){
            filepath = filepath + "3";
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

    //we create a new thread and we send the port the port
    //of the thread to the consumer so he knows what port to connect to
    private void handleRequests() {

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while(true){
                Socket socket = serverSocket.accept();
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                int newPort = threadPorts.poll();
                Thread thread = new Thread(new BrokerThread(newPort));
                thread.start();

                dataOutputStream.writeInt(newPort);
                dataOutputStream.flush();
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public void connect() {
        for(int i=0;i<2;i++){
            //runs this code for every publisher
            try{
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket connection = serverSocket.accept(); //connects with a publisher
                OutputStream outputStream = connection.getOutputStream();
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(this); //sends itself(the broker item)
                objectOutputStream.flush();
                connection.close();
                connection = serverSocket.accept(); //connects again with the broker after the hashing from the publisher's part is done
                InputStream inputStream = connection.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                allArtists.addAll((ArrayList<ArtistName>)objectInputStream.readObject()); //adds the artists that this publisher has in an arraylist
                //allArtists is an arraylist with every artist every publisher has.
                connection.close();
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //the broker adds to the arraylist artists the artists that he is responsible for
    private void checkArtists() {
        for(ArtistName ar : allArtists){
            if(ar.getBroker().getPortNumber()==portNumber){
                artists.add(ar);
            }
        }
    }

    //get the port from a queue in file ports.
    public int getPortFromFile(){
        int port = 0;

        String filepath = "C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//broker_ports";
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

    public int getPortNumber() {
        return portNumber;
    }

    public int getHash() {
        return hash;
    }

    //class of broker threads
    //this class implements runnable
    public class BrokerThread implements Runnable{
        ServerSocket serverSocket;
        Socket customer_connection,publisher_connection;
        private int port;
        InputStream inputStream;
        ObjectInputStream objectInputStream;
        OutputStream outputStream;
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;
        ObjectOutputStream objectOutputStream;
        private Queue<Value> chunksToSend; //queue which saves what chunks we have to send in order to send them in the correct order

        public BrokerThread(int p) {
            port = p;
            chunksToSend = new LinkedList<>();
        }


        //code that runs when we start a new thread
        @Override
        public void run() {
            try {
                System.out.println("new thread : "+port);
                serverSocket = new ServerSocket(port);
                customer_connection = serverSocket.accept(); //we wait for the consumer to send us the request
                inputStream = customer_connection.getInputStream();
                outputStream = customer_connection.getOutputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                String[] songRequestArray = (String[]) objectInputStream.readObject(); //we read the request from the port
                String song_requested = songRequestArray[1];
                String artist_requested = songRequestArray[0];
                System.out.println("Artist : "+artist_requested+" \nSong: "+song_requested);
                SongRequest sr = new SongRequest(artist_requested,song_requested);
                dataOutputStream = new DataOutputStream(outputStream);
                //if we only need to send the artist strings
                if(artist_requested.equals("all_artists")){
                    System.out.println("all_artists");
                    objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(allArtistsStrings);
                    objectOutputStream.flush();
                    disconnect();
                    return;
                }
                boolean registered = false;
                boolean right_song =false;

                ArtistName an = null;

                //we check if the broker is responsible for the artist
                for(ArtistName a : artists){
                    if(artist_requested.equals(a.getArtistName())) {
                        an = a;
                        registered = true;
                        break;
                    }
                }

                if(song_requested.equals("all_songs")){
                    if(registered) {
                        dataOutputStream.writeInt(port); //ack
                        dataOutputStream.flush();
                        objectOutputStream = new ObjectOutputStream(outputStream);
                        objectOutputStream.writeObject(an.getSongs());
                        objectOutputStream.flush();
                        disconnect();
                        return;
                    }
                    else{
                        for(ArtistName artistName : allArtists){
                            if(artistName.getArtistName().equals(artist_requested)){
                                dataOutputStream.writeInt(artistName.getBroker().getPortNumber()); //return correct broker port
                                dataOutputStream.flush();
                                disconnect();
                                return;
                            }
                        }
                    }
                }

                //if it is then we check if the song title is correct
                if(registered){
                    right_song = an.checkSong(song_requested);
                    if(right_song) {
                        dataOutputStream.writeInt(port); //ack
                        dataOutputStream.flush();
                        //if the song and the artist are correct we
                        // pull data from the publisher and we send them to the consumer
                        System.out.println("PULL");
                        pull(sr, an.getPublisher());
                    }
                    else{
                        //if the song is wrong send code 200 and then send all the songs or the artist that are available
                        dataOutputStream.writeInt(200);
                        dataOutputStream.flush(); // send the message
                        objectOutputStream = new ObjectOutputStream(outputStream);
                        objectOutputStream.writeObject(an.getSongs());
                        objectOutputStream.flush();
                    }
                }
                else{
                    //if the artist is wrong send code 100 and then all the available artist from all the brokers
                    dataOutputStream.writeInt(100);
                    dataOutputStream.flush(); // send the message
                    objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(Broker.allArtists);
                    objectOutputStream.flush();
                }

                disconnect();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void disconnect() throws IOException {
            customer_connection.close();

            //add port back to queue
            threadPorts.add(port);
        }

        public synchronized void pull(SongRequest ar, Publisher p) {
            try {
                //connect with the publisher that has the song we need
                publisher_connection = new Socket("localhost", p.getPortNumber());
                inputStream = publisher_connection.getInputStream();
                dataInputStream = new DataInputStream(inputStream);
                //get the port of the thread of the publisher that will handle the request
                int newPort = dataInputStream.readInt();
                publisher_connection.close();

                //connect with the thread
                System.out.println("new port\n"+"Song : "+ar.getSong()+"\nArtist : "+ar.getArtist());
                publisher_connection = new Socket("localhost",newPort); //thread
                inputStream = publisher_connection.getInputStream();
                outputStream = publisher_connection.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                //send the request
                objectOutputStream.writeObject(ar);
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(inputStream);
                //get all the chunks from the publisher
                Value v = (Value) objectInputStream.readObject();
                int n = 0;
                while(!v.getTrackName().equals("last_value")) { //checks if we found the end of the chunks
                    chunksToSend.add(v);
                    n++;
                    v = (Value) objectInputStream.readObject();
                }
                publisher_connection.close();

                outputStream = customer_connection.getOutputStream();
                dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeInt(n);
                objectOutputStream = new ObjectOutputStream(outputStream);
                //send all the chunks to the consumer
                int i=0;
                while(!chunksToSend.isEmpty()){
                    objectOutputStream.writeObject(chunksToSend.poll().getMusicFileExtract());
                    objectOutputStream.flush();
                    i++;
                }
                System.out.println("ALL SENT times "+i);
                customer_connection.close();


            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}


