import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

public class ArtistName implements Serializable {
    private ArrayList<String> songs;
    private String artistName;
    private int hash;
    private Broker broker;
    private Publisher publisher;

    public String getArtistName(){
        return artistName;
    }

    public int getHash(){
        return hash;
    }

    public void setArtistName(String name){
        artistName = name;
        calculateHash(); //everytime we set the artist we calculate the hash
        broker = null;
        publisher = null;
        songs=new ArrayList<>();
    }

    public void addSong(String song){
        songs.add(song);
    }

    public ArrayList<String> getSongs(){
        return songs;
    }

    public ArtistName(String name){
        this.setArtistName(name);
    }

    public void calculateHash(){
        hash = new BigInteger(DigestUtils.sha1Hex(artistName),16).mod(new BigInteger("100")).intValue();
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public boolean checkSong(String song) {
        for(String s : songs){
            if(song.equals(s))
                return true;
        }
        return false;
    }
}