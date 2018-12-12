
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jiri
 */
public class ReadData {
    String fileName;
    static String[] zones; //--- list of zones
    static ArrayList<String> airports; //--- list of airports
    static HashMap<String, Short> airportsAbsolute; //--- absolute index
    public static short[][] zoneAirports;    //--- table of airportsAbsolute; i-th row contains a list of the zone's airport absolute indexes
    static short[] airport2zone;    //--- each absolute airport index maps to its zone
    static String startAirportName;
    static String startZoneName;
    public static short startZone;
    public static short startAirport;   //--- absolute
    public static short nbZones;
    public static short nbAirports;   //--- total number of airportsAbsolute
    public static short[][][] flights;  //--- table of costs of flights day-from-to
    public static short noFlightCost = 30000;
    
    ReadData(){}
    
    ReadData(String name){
        fileName = name;
    }

    public void readSimple(){
        try{
            BufferedReader br = new BufferedReader(new FileReader(fileName));
//            BufferedReader br = new BufferedReader (new InputStreamReader (System.in));
            //--- first line: the number of areas and the airport from which the trip starts
            String line = br.readLine();
            String[] tokens = line.split("\\s+");
            nbZones = Short.valueOf(tokens[0]);
            nbAirports = 0;
            startAirportName = tokens[1];
//            System.out.println("nbZones: " + nbZones);
//            System.out.println("startAirport: " + startAirportName);
            //--- read zone airportsAbsolute
            zones = new String[nbZones];
            airports = new ArrayList<>();
            airportsAbsolute = new HashMap<String, Short>();
            zoneAirports = new short[nbZones][];
            short k = 0;
            while (k < nbZones) {
                //--- zone name
                line = br.readLine();
                zones[k] = line.trim();
                //--- zone airportsAbsolute
                line = br.readLine();
                tokens = line.split("\\s+");
                zoneAirports[k] = new short[tokens.length];
                for(short i=0; i<tokens.length; i++){
                    airports.add(tokens[i]);
                    airportsAbsolute.put(tokens[i], nbAirports);
                    zoneAirports[k][i] = nbAirports;
                    if(tokens[i].equals(startAirportName)){
                        startZoneName = zones[k];
                        startZone = k;
                        startAirport = nbAirports;
                    }
                    nbAirports++;
                }
                k++;
            }
            //--- airport2zone
            airport2zone = new short[nbAirports];
            short m = 0;    //--- actual number of airportsAbsolute
            for(k=0; k<nbZones; k++){
                for(short i=0; i<zoneAirports[k].length; i++){
                    airport2zone[m] = k;
                    m++;
                }
            }
            flights = new short[nbZones][nbAirports][nbAirports];
            for(short i=0; i<nbZones; i++){
                for(short j=0; j<nbAirports; j++){
                    for(k=0; k<nbAirports; k++){
                        flights[i][j][k] = noFlightCost;   //--- no flight on day i between airports j and k
                    }
                }
            }
            //---
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                short lastSpaceIndex = (short)line.indexOf(' ', 8);
                short day = Short.valueOf(line.substring(8, lastSpaceIndex));
                short price = Short.valueOf(line.substring(lastSpaceIndex+1, line.length()));
                short airportFromAbsolute = airportsAbsolute.get(line.substring(0,3));
                short airportToAbsolute = airportsAbsolute.get(line.substring(4,7));
                if(day == 0){
                    for(short dd=0; dd<nbZones; dd++){
                        if(price < flights[dd][airportFromAbsolute][airportToAbsolute]){
                            flights[dd][airportFromAbsolute][airportToAbsolute] = price;
                        }
                    }
                }
                else{
                    flights[day-1][airportFromAbsolute][airportToAbsolute] = price;
                }
            }
            br.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void printRandomFlightOnDay(int day){
        short apFrom = (short)Main.rnd.nextInt(nbAirports);
        short apTo = (short)Main.rnd.nextInt(nbAirports);
        if(flights[day][apFrom][apTo] != noFlightCost){
            System.out.println("\nflight: day=" + (day+1) + " " + airport2zone[apFrom] + "/" + apFrom + " -> " + airport2zone[apTo] + "/" + apTo + " cost=" + flights[day][apFrom][apTo]);
            System.out.println("flight: day=" + (day+1) + " " + zones[airport2zone[apFrom]] + "/" + airports.get(apFrom) + " -> " + zones[airport2zone[apTo]] + "/" + airports.get(apTo) + " cost=" + flights[day][apFrom][apTo]);
        }
    }
}
