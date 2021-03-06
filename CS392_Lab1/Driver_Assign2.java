import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Driver_Assign2 {
    final static int SENT_BY_SOURCE = 1;
    final static int RECIEVED_AT_SWITCH = 2;
    final static int DISPATCH_FROM_SWITCH = 3;
    final static int RECIEVED_AT_SINK = 4;
    final static int NUMBER_OF_SOURCE = 3;

    static int glb_source_id = 0;
    static int glb_packet_id = 0;
    static int glb_event_id = 0;
    static double glb_time = 0;
    static PriorityQueue<Event> global_Queue = new PriorityQueue<>();

    public static void main(String[] args) {
        for (int u = 0; u < 500; u++) {
            glb_source_id =0 ; glb_time=0 ;glb_packet_id=0 ;glb_source_id =0 ;
            global_Queue.clear();    

       // Scanner sc = new Scanner(System.in);
        int[] packet_sending_rate = new int[NUMBER_OF_SOURCE];
        double time_to_process = 0;
        double[] d_source_to_switch = new double[NUMBER_OF_SOURCE];
        double d_time_to_sink = 4 ;
        double[] time_interval = new double[NUMBER_OF_SOURCE];
        double[] delay = new double[NUMBER_OF_SOURCE];

        //System.out.println("Enter the time of simulation");
        int time_of_simulation = 0;
        time_of_simulation = 1500;
       // sc.close();
        
        Source[] src = new Source[NUMBER_OF_SOURCE];
        Link [] src_2_switch = new Link[NUMBER_OF_SOURCE];
        Packet[] start_pack = new Packet[NUMBER_OF_SOURCE];
        ArrayList<Packet> sent_packet = new ArrayList<>();
        ArrayList<Packet> recieved_packets = new ArrayList<>();


        Switch swtch1 = new Switch(time_to_process, d_time_to_sink);
        Link switch_2_sink = new Link();
       
        //initilaise all the values 
        for(int l=0 ;l<NUMBER_OF_SOURCE;++l) {
            packet_sending_rate[l] = u+1 ;
            time_interval[l] = 1.0 /packet_sending_rate[l];
            d_source_to_switch[l] = 4;
            src[l] = new Source( packet_sending_rate[l],d_source_to_switch[l]);
            start_pack[l] = new Packet(src[l].id, 0.0);
            global_Queue.add(new Event(SENT_BY_SOURCE, start_pack[l], 0.0));
            sent_packet.add(start_pack[l]);
            src_2_switch[l] = new Link();
        }


        // System.out.println(time_interval);
        while (glb_time <= time_of_simulation) {

            if (!global_Queue.isEmpty()) {
                Event curr_process = global_Queue.poll();
                int curr_process_packet_src_id = curr_process.packet.source_id;
                curr_process_packet_src_id--;
                // System.out.println(curr_process.toString());
                glb_time = curr_process.scheduled_time;
                switch (curr_process.event_type) {
                case SENT_BY_SOURCE:

                    if (src_2_switch[curr_process_packet_src_id].curr_packet == null) {
                        curr_process.scheduled_time += (src[curr_process_packet_src_id].time_to_switch);
                        src_2_switch[curr_process_packet_src_id].curr_packet = curr_process.packet;
                        src_2_switch[curr_process_packet_src_id].checkpoint_reach = curr_process.scheduled_time;
                        curr_process.event_type = RECIEVED_AT_SWITCH;
                        global_Queue.add(curr_process);
                    } else {
                        delay[curr_process_packet_src_id]+=(src_2_switch[curr_process_packet_src_id].checkpoint_reach-curr_process.scheduled_time);
                        curr_process.scheduled_time = src_2_switch[curr_process_packet_src_id].checkpoint_reach;
                        src_2_switch[curr_process_packet_src_id].checkpoint_reach += src[curr_process_packet_src_id].time_to_switch;
                        global_Queue.add(curr_process);
                    }
                    break;
                case RECIEVED_AT_SWITCH:
                    src_2_switch[curr_process_packet_src_id].curr_packet = null;
                    curr_process.scheduled_time += swtch1.time_to_process;
                    curr_process.event_type = DISPATCH_FROM_SWITCH;
                    global_Queue.add(curr_process);
                    break;
                case DISPATCH_FROM_SWITCH:
                    if (switch_2_sink.curr_packet == null) {

                        curr_process.scheduled_time += swtch1.time_to_sink;
                        switch_2_sink.curr_packet = curr_process.packet;
                        switch_2_sink.checkpoint_reach = curr_process.scheduled_time;
                        curr_process.event_type = RECIEVED_AT_SINK;
                        global_Queue.add(curr_process);
                    } else {
                        delay[curr_process_packet_src_id]+=(switch_2_sink.checkpoint_reach-curr_process.scheduled_time);
                        curr_process.scheduled_time = switch_2_sink.checkpoint_reach;
                        switch_2_sink.checkpoint_reach += swtch1.time_to_sink;
                        global_Queue.add(curr_process);
                    }
                    break;
                case RECIEVED_AT_SINK:
                    switch_2_sink.curr_packet = null;
                    curr_process.packet.packet_reach_time = glb_time;
                    recieved_packets.add(curr_process.packet);
                    break;
                }
            }

            addNewPackets(sent_packet, src, time_interval);
        }

        int[] n = new int[NUMBER_OF_SOURCE];
      //  System.out.println("" + recieved_packets.size());
        //double[] time_delay = new double[NUMBER_OF_SOURCE];
        for (Packet pc : recieved_packets) {
            // System.out.println("Packet_id -> "+ pc.source_id +" Packet_start-> " +
            // pc.time_stamp + " Packet_end--> "+ pc.packet_reach_time);
            ++n[pc.source_id-1];
            // time_delay[pc.source_id - 1] += (pc.packet_reach_time
            //         - (pc.time_stamp + src[pc.source_id-1].time_to_switch + swtch1.time_to_sink + swtch1.time_to_process));
        }

        for(int i=0;i<NUMBER_OF_SOURCE;++i) {
            System.out.print(delay[i]/n[i] +",");
        }
        System.out.println();
    }

    }

    public static void addNewPackets(ArrayList<Packet> sent_packet, Source[]src, double[] time_interval) {
        int n = sent_packet.size();
        n/=NUMBER_OF_SOURCE;
        for(int i=0;i<NUMBER_OF_SOURCE;++i) {
            Packet newPack = new Packet(src[i].id, n*time_interval[i]);
            sent_packet.add(newPack);
            global_Queue.add(new Event(SENT_BY_SOURCE, newPack, newPack.time_stamp));
        }
    }

    static class Source {
        int id;
        int packet_sending_rate;
        double time_to_switch;

        public Source(int packet_sending_rate, double time_to_switch) {
            this.id = ++glb_source_id;
            this.packet_sending_rate = packet_sending_rate;
            this.time_to_switch = time_to_switch;
        }
    }

    static class Switch {
        double time_to_process;
        double time_to_sink;

        public Switch(double time_to_process, double time_to_sink) {
            this.time_to_process = time_to_process;
            this.time_to_sink = time_to_sink;
        }

    }

    static class Packet {
        int source_id;
        double time_stamp;
        int packet_id;
        double packet_reach_time;

        public Packet(int source_id, double time) {
            this.source_id = source_id;
            this.time_stamp = time;
            packet_id = ++glb_packet_id;
        }

        @Override
        public String toString() {
            return "packet id-> " + this.packet_id + "time_stamp -> " + this.time_stamp;
        }
    }

    static class Event implements Comparable<Event> {
        int event_id;
        int event_type;
        double scheduled_time;
        Packet packet;

        public Event(int event_type, Packet packet, double scheduled_time) {
            this.event_id = ++glb_event_id;
            this.event_type = event_type;
            this.packet = packet;
            this.scheduled_time = scheduled_time;

        }

        @Override
        public int compareTo(Event e) {
            if (this.scheduled_time < e.scheduled_time) {
                return -1;
            } else if (this.scheduled_time > e.scheduled_time) {
                return 1;
            } else if (this.event_type > e.event_type) {
                return -1;
            } else if (this.event_type < e.event_type) {
                 return 1;   
            }  else if(this.packet.packet_id > e.packet.packet_id) {
                return 1;
            } else {
                return -1;
            }
        }

        @Override
        public String toString() {
            return "Event id -> " + this.event_id + "Event type -> " + this.event_type + " Scheduled time -> "
                    + this.scheduled_time + this.packet.toString();
        }

    }

    static class Link {
        Packet curr_packet;
        double checkpoint_reach;
    }

}