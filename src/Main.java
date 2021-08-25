import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
    public static void main(String[] args) {
        Queue<Integer> publisher_counter = new LinkedList<>();
        Queue<Integer> broker_ports = new LinkedList<>();
        Queue<Integer> broker_1_ports = new LinkedList<>();
        Queue<Integer> broker_2_ports = new LinkedList<>();
        Queue<Integer> broker_3_ports = new LinkedList<>();
        Queue<Integer> publisher_ports = new LinkedList<>();
        Queue<Integer> publisher_1_ports = new LinkedList<>();
        Queue<Integer> publisher_2_ports = new LinkedList<>();
        int i;
        for (i = 1; i < 3; i++) {
            publisher_counter.add(i);
        }


        //brokers 8900-8902
        for (i = 8900; i < 8903; i++) {
            broker_ports.add(i);
        }
        //publishers 8903-8904
        for (i = 8903; i < 8905; i++) {
            publisher_ports.add(i);
        }

        //broker 1 threads 8000 - 8299
        //broker 2 threads 8300 - 8599
        //broker 3 threads 8600 - 8899

        for (i = 8000; i < 8299; i++) {
            broker_1_ports.add(i);
        }
        for (i = 8300; i < 8599; i++) {
            broker_2_ports.add(i);
        }
        for (i = 10000; i < 10100; i++) {
            broker_3_ports.add(i);
        }

        //publisher 1 9000 - 9299
        //publisher 2 9300 - 9599
        for (i = 9000; i < 9300; i++) {
            publisher_1_ports.add(i);
        }
        for (i = 9300; i < 9600; i++) {
            publisher_2_ports.add(i);
        }

        FileWriter writer = null;
        try {
            File file = new File("C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//publisher_counter");
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(f);
            oos.writeObject(publisher_counter);
            oos.close();
            file = new File("C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//broker_ports");
            f = new FileOutputStream(file);
            oos = new ObjectOutputStream(f);
            oos.writeObject(broker_ports);
            oos.close();
            file = new File("C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//broker_ports_1");
            f = new FileOutputStream(file);
            oos = new ObjectOutputStream(f);
            oos.writeObject(broker_1_ports);
            oos.close();
            file = new File("C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//broker_ports_2");
            f = new FileOutputStream(file);
            oos = new ObjectOutputStream(f);
            oos.writeObject(broker_2_ports);
            oos.close();
            file = new File("C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//broker_ports_3");
            f = new FileOutputStream(file);
            oos = new ObjectOutputStream(f);
            oos.writeObject(broker_3_ports);
            oos.close();
            file = new File("C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//publisher_ports");
            f = new FileOutputStream(file);
            oos = new ObjectOutputStream(f);
            oos.writeObject(publisher_ports);
            oos.close();
            file = new File("C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//publisher_ports_1");
            f = new FileOutputStream(file);
            oos = new ObjectOutputStream(f);
            oos.writeObject(publisher_1_ports);
            oos.close();
            file = new File("C://Users//Spanakopitas//IdeaProjects//project_katanemimena//files//publisher_ports_2");
            f = new FileOutputStream(file);
            oos = new ObjectOutputStream(f);
            oos.writeObject(publisher_2_ports);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}