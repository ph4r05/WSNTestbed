/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.messages;

/**
 * Message types as defined in RssiDemoMessages.h
 * It is needed to keep this class data consistent with nesC header file to work
 * properly.
 * 
 * @author ph4r05
 */
public class MessageTypes {
  public static final int AM_RSSIMSG = 10;
  public static final int AM_PINGMSG = 11;
  public static final int AM_MULTIPINGMSG = 12;
  public static final int AM_MULTIPINGRESPONSEMSG = 13;
  public static final int AM_COMMANDMSG = 14;
  public static final int AM_MULTIPINGRESPONSEREPORTMSG = 16;
  public static final int AM_MULTIPINGRESPONSETINYREPORTMSG = 17;

  // abort message types
  public static final int COMMAND_NONE=0;
  public static final int COMMAND_ABORT=1;
  public static final int COMMAND_IDENTIFY=2;
  public static final int COMMAND_RESET=3;
  public static final int COMMAND_SETTX=4;
  public static final int COMMAND_SETCHANNEL=5;
  public static final int COMMAND_ACK=6;
  public static final int COMMAND_NACK=7;
  public static final int COMMAND_SETBS=8;
  public static final int COMMAND_LOCK=9;
  public static final int COMMAND_GETREPORTINGSTATUS=10;
  public static final int COMMAND_SETREPORTINGSTATUS=11;
  public static final int COMMAND_SETDORANDOMIZEDTHRESHOLDING=12;
  public static final int COMMAND_SETQUEUEFLUSHTHRESHOLD=13;
  public static final int COMMAND_SETTINYREPORTS=14;
  public static final int COMMAND_SETOPERATIONMODE=15;
  public static final int COMMAND_SETREPORTPROTOCOL=16;
  public static final int COMMAND_FLUSHREPORTQUEUE=17;
  public static final int COMMAND_SETNOISEFLOORREADING=18;
  public static final int COMMAND_SETREPORTGAP=19;
  public static final int COMMAND_GETSENSORREADING=20;
  public static final int COMMAND_SENSORREADING=21;
  public static final int COMMAND_SETSAMPLESENSORREADING=24;
  
  //
  // pinning
  //
  public static final int COMMAND_SETPIN=22;
  public static final int COMMAND_GETPIN=23;
  
  //
  // settings
  //
  /**
   * Fetching is request sent to base station after booting node up. 
   * Base station will then re-send node settings from node register to 
   * booted node (can be after reset already)
   */
  public static final int COMMAND_FETCHSETTINGS=25;
  
  // base station settings - forwarding from radio to serial?
  public static final int COMMAND_FORWARDING_RADIO_ENABLED=26;
  // base station settings - forwarding from serial to radio?
  public static final int COMMAND_FORWARDING_SERIAL_ENABLED=27;
  
  // base station settings - forwarding from radio to serial? default=without specific wiring
  public static final int COMMAND_DEFAULT_FORWARDING_RADIO_ENABLED = 28;
  // base station settings - forwarding from serial to radio? setRadioSnoopEnabled
  public static final int COMMAND_DEFAULT_FORWARDING_SERIAL_ENABLED = 29;

  // base station settings - whether forward messages received on snoop interface?
  public static final int COMMAND_RADIO_SNOOPING_ENABLED = 30;
  // base station settings - address recognition? if false then mote will sniff foreign messages
  public static final int COMMAND_RADIO_ADDRESS_RECOGNITION_ENABLED = 31;
  
  // set node as CTP root
  public static final int COMMAND_SET_CTP_ROOT=32;
  
  // call CTP route recomputing command - depending on data, CtpInfo interface is used
  // data=1 -> CtpInfo.triggerRouteUpdate()
  // data=2 -> CtpInfo.triggerImmediateRouteUpdate()
  // data=3 -> CtpInfo.recomputeRoutes()
  public static final int COMMAND_CTP_ROUTE_UPDATE=33;
  
  // gets basic CTP info from CtpInfo interface
  // data=0 -> returns parent, etx, neighbors count in data[0], data[1], data[2]
  // data=1 -> info about neighbor specified in data[0]. Returned addr, link quality, route
  //				quality, congested bit
  public static final int COMMAND_CTP_GETINFO=34;
  
  // other CTP controling, can set TX power for packets
  // data=0 -> set tx power for OUTPUT messages for CTP protocol.
  // 				if data[0] == 1	-> set TXpower for ROUTE messages on data[1] level
  //			 	if data[0] == 2 -> set TXpower for DATA messages on data[1] level
  //				if data[0] == 3 -> set TXpower for both ROUTE, DATA messages on data[1] level
  public static final int COMMAND_CTP_CONTROL=35;
  
  // invokes request on global time for every node which heard this request
  public static final int COMMAND_TIMESYNC_GETGLOBAL=36;
  
  // request response protocol to meassure RTT of channel, should be as fast as possible                                                                                                   
  public static final int COMMAND_PING=37;

  // identity types
  public static final int NODE_STATIC=1;
  public static final int NODE_DYNAMIC=2;
  public static final int NODE_BS=3;
  public static final int NODE_DEAD=4;

  // report protocols
  public static final int REPORTING_MEDIUM=1;
  public static final int REPORTING_TINY=2;
  public static final int REPORTING_MASS=3;
  
  public static final int AM_BROADCAST_ADDR=0xFFFF;
}
