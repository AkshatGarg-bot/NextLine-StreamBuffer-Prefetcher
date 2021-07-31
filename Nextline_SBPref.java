import java.util.*;
import java.io.*;

class Prefetcher {
    private Buffer[] buffers;
    private Deque<Integer> PDQ = new ArrayDeque<Integer>();
    private int size;

    Prefetcher() {
    }

    Prefetcher(int size) {
        this.size = size;

        buffers = new Buffer[size];

        for (int i = 0; i < size; i++)
            buffers[i] = new Buffer();

        PDQ = new LinkedList<>();

        for (int i = 0; i < size; i++)
            PDQ.add(i);
    }

    int Pref_hit(String address, int f) {

        long add_int = Long.parseLong(address, 2);

        for (int i = 0; i < size; i++) {
            if (buffers[i].get_address() != null) {
                long head_address = Long.parseLong(buffers[i].get_address(), 2);
                if (head_address <= add_int && head_address + 15 >= add_int) {
                    if (f == 1) {
                        PDQ.remove(i);
                        PDQ.add(i);
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    void Add(String address) {

        if (Pref_hit(address, 0) == -1) {
            int temp_buffer = PDQ.poll();

            long add_int = Long.parseLong(address, 2) + 4;

            buffers[temp_buffer].set_address(String.format("%32s", Long.toBinaryString(add_int)).replaceAll(" ", "0"));

            PDQ.add(temp_buffer);
        }
    }
}

class Buffer extends Prefetcher {
    private String address;
    private String[] data;

    Buffer() {
        super();
        data = new String[4];
    }

    void set_address(String a) {
        address = a;
    }

    String get_address() {
        return address;
    }

}

class Set {
    private way[] single_set;
    private Deque<Integer> DQ = new ArrayDeque<Integer>();
    private int n;
    private int Occupied;

    Set() {
    }

    Set(int n) {
        this.n = n;
        Occupied = 0;
        single_set = new way[n];

        for (int i = 0; i < n; i++)
            single_set[i] = new way();

        DQ = new LinkedList<>();
        DQ.add(0);
        DQ.add(1);
        DQ.add(2);
        DQ.add(3);
    }

    Deque<Integer> GetDQ() {
        return DQ;
    }

    way[] GetWay() {
        return single_set;
    }

    int Check_hit(String tag) {
        for (int i = 0; i < n; i++) {
            if (single_set[i].getValidBit() == 1 && single_set[i].getTag().equals(tag)) {
                DQ.remove(i);
                DQ.add(i);
                return i;
            }
        }
        return -1;
    }

    int getOcc() {
        return Occupied;
    }

    void Evict(int rem_way) {
        single_set[rem_way].setValidBit(0);
        Occupied--;
        DQ.remove(rem_way);
        DQ.addFirst(rem_way);
    }

    void Add(String tag, String data) {
        int temp_way = DQ.poll();
        if (single_set[temp_way].getValidBit() == 0) {
            Occupied++;
            single_set[temp_way].setValidBit(1);
        }
        single_set[temp_way].settag(tag);
        single_set[temp_way].setData(data);
        DQ.add(temp_way);
    }
}

class way extends Set {
    private String l1_tag; // stores l1_tag
    private int validbit; // stores valid bit
    private String data; // stores data

    way() // constructor
    {
        super();
        this.validbit = 0;
    }

    // getters and setters
    String getTag() {
        return l1_tag;
    }

    int getValidBit() {
        return validbit;
    }

    String getData() {
        return data;
    }

    void setData(String s) {
        this.data = s;
    }

    void settag(String s) {
        this.l1_tag = s;
    }

    void setValidBit(int p) {
        this.validbit = p;
    }
}

public class Nextline_SBPref {
    public static void main(String[] args) {
        try {
            System.out.println("File Name              C/P         |    Hits  |       Misses   ");
            String[] files = { "twolf_new.trace"};
            // ,"gzip.trace","twolf.trace","mcf.trace","swim.trace , l1miss_phit.trace , pmiss_l2hit.trace ,l1miss_l2hit.trace,pmiss_l2hit.trace"
            for (int m = 0; m < 1; m++) {
                String p = files[m];
                int l1_cachehits = 0; 
                int l1_cachemiss = 0; 
                int l2_cachemiss = 0;
                int l2_cachehits = 0;
                int prefetcherhits = 0;
                int prefetchermiss = 0;
                // contains the whole cache of 4 way set associative cache
                int l1_sets = 64;
                int l2_sets = 512;
                Set[] l1_cache = new Set[l1_sets]; //6 //2^12
                Set[] l2_cache = new Set[l2_sets];//8 //2^14
                Prefetcher Pref = new Prefetcher(16);
                // way[][] next_line_prefetcher = new way[8192][16];
                for (int i = 0; i < l1_sets; i++) {
                    l1_cache[i] = new Set(8); // allocating memory to cache
                }
                for (int i = 0; i < l2_sets; i++) {

                    l2_cache[i] = new Set(8);
                }

                File file = new File(p); // creates a new file instance
                FileReader fr = new FileReader(file); // reads the file
                BufferedReader br = new BufferedReader(fr); // creates a buffering character input stream
                String line;

                while ((line = br.readLine()) != null) {
 
                    String str = line.substring(2, 10); 
                    str = convert(str);
                    // }
                    int l1_index = Integer.parseInt(str.substring(23, 29), 2); //6 bit
                    int l2_index = Integer.parseInt(str.substring(20, 29), 2); // 9 bit
                    String l1_tag = str.substring(0, 23);
                    String l2_tag = str.substring(0, 20);
//                   Pref.Add(str);

                    if (l1_cache[l1_index].Check_hit(l1_tag) != -1)
                        l1_cachehits++;
                    else {
                        l1_cachemiss++;
                        if (Pref.Pref_hit(str, 1) != -1) {
                            prefetcherhits++;
                        } else {
                            prefetchermiss++;
                            if (l2_cache[l2_index].Check_hit(l2_tag) != -1)
                                l2_cachehits++;
                            else {
                                l2_cachemiss++;
                                l1_cache[l1_index].Add(l1_tag, "0");
                                add_in_l2_cache(l1_cache, l2_cache, l2_index, l2_tag, "0");

                            }
                        }
                    }
                }
                System.out.println(p + "      l1               " + l1_cachehits + "               " + l1_cachemiss); // ratio
                System.out.println(p + "      pref             " + prefetcherhits + "               " + prefetchermiss); // ratio
                System.out.println(p + "      l2               " + l2_cachehits + "               " + l2_cachemiss); // ratio

            }

        } catch (IOException e) {
            System.out.println("File Not present please enter the correct file name");
        }
    }

    public static String convert(String s) {
        long num = Long.parseLong(s, 16);
        String binary = Long.toBinaryString(num);
        binary = ("00000000000000000000000000000000" + binary).substring(binary.length());
        return binary;
    }

    public static void add_in_l2_cache(Set[] l1_cache, Set[] l2_cache, int l2_index, String l2_tag, String data) {

        if (l2_cache[l2_index].getOcc() == 4) {
            int rem = l2_cache[l2_index].GetDQ().peek();
            // l2_cache[l2_index].GetDQ.add(rem);

            String rem_tag = l2_cache[l2_index].GetWay()[rem].getTag();
            String ii = Integer.toBinaryString(l2_index);
            int zeros = 9 - ii.length();
            if (zeros > 0) {
                String z = "0";
                for (int q = 0; q < zeros - 1; q++) {
                    z = z.concat("0");
                }
                ii = z.concat(ii);
            }
            String full = rem_tag.concat(ii);
            int templ1_index = Integer.parseInt(full.substring(23,29), 2);
            String templ1_tag = full.substring(0, 23);
            int rem_way = l1_cache[templ1_index].Check_hit(templ1_tag);
            if (rem_way != -1) {
                l1_cache[templ1_index].Evict(rem_way);
            }
        }
        l2_cache[l2_index].Add(l2_tag, data);
    }
}
