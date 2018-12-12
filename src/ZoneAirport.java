/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jiri
 */
public class ZoneAirport {
    public short zone;
    public short airport;
    public short airportFrom;
    public short airportTo;
    public short reachability = 0;
    public short cost;
    public ZoneAirport(short z, short a, short aFrom, short aTo, short c){
        this.zone = z;
        this.airport = a;
        this.airportFrom = aFrom;
        this.airportTo = aTo;
        this.cost = c;
    }
}
