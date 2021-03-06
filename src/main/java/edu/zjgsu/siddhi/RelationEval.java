package edu.zjgsu.siddhi;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

/**
 * Created by AH on 2016/12/21.
 */
public class RelationEval {

    public static void main ( String[] args ) throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();

        String bruteForceLoginSuccess = "" +
                "@plan:async " +
                "define stream rawStream ( catBehavior string, catOutcome string, srcAddress string, deviceCat string, srcUsername string, catObject string, destAddress string, appProtocol string ); " +
                "" +
                "@info(name = 'condition1') " +
                //catObject要有三种 以srcAddress, srcUsername, destAddress, appProtocol分组的事件个数不少于9个
                "from rawStream[ catBehavior == '/Authentication/Verify' and catOutcome == 'FAIL' and not( srcAddress is null ) ]#window.time(10 sec) " +
                "select srcAddress, catOutcome, deviceCat, srcUsername, destAddress, appProtocol, distinctcount( catObject ) as distinctMinCount, count() as groupCount " +
                "group by srcAddress, srcUsername, destAddress, appProtocol " +
                "having groupCount >= 9 and distinctMinCount >=1 " +
                "insert into e1_OutputStream;" ;//+
                //"" +
                //"@info(name = 'condition2') " +
                //"from rawStream[ catBehavior == '/Authentication/Verify' and catOutcome == 'OK' and not( srcAddress is null ) ]#window.timeBatch(10 sec) " +
                //"select srcAddress, catOutcome,  deviceCat, srcUsername, destAddress, appProtocol, count() as groupCount  " +
                //"group by srcAddress, srcUsername, destAddress, appProtocol " +
                ////"having groupCount >= 1 " +
                //"insert current events into e2_OutputStream;"
                //+ "" +
                //"@info(name = 'result') " +
                //"from every ( e1 = e1_OutputStream<9:> -> e2 = e2_OutputStream[ srcAddress == e1.srcAddress " +
                //                                                                 "and deviceCat == e1.deviceCat " +
                //                                                                 "and srcUsername == e1.srcUsername " +
                //                                                                 "and destAddress == e1.destAddress " +
                //                                                                 "and appProtocol == e1.appProtocol ] ) " +
                ////"within 1 second " +//每个事件之间的间隔
                //"select 'relationEvent' as event, e1.srcAddress, e1.deviceCat, e1.srcUsername, e1.destAddress, e1.appProtocol " +
                //"insert into resultOutputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime( bruteForceLoginSuccess );

        executionPlanRuntime.addCallback( "e1_OutputStream", new StreamCallback() {
            @Override
            public void receive ( Event[] events ) {
                for ( Event event : events ) {
                    System.out.println( event.toString() );
                }
            }
        } );

        InputHandler rawStreamHandler = executionPlanRuntime.getInputHandler( "rawStream" );
        executionPlanRuntime.start();
        //catBehavior, catOutcome, srcAddress, deviceCat, srcUsername, catObject, destAddress, appProtocol ;group by srcAddress, srcUsername, destAddress, appProtocol
        //第一个group
        for ( int i = 0 ; i < 15 ; i++ ) {
            rawStreamHandler.send( new Object[] { "/Authentication/Verify" , "FAIL" , "1.1.1.1" ,
                    "deviceCat" , "srcUsername" , "catObject1" , "destAddress" , "appProtocol" } );
        }
        //第二个group
        for ( int i = 0 ; i < 9 ; i++ ) {
            rawStreamHandler.send( new Object[] { "/Authentication/Verify" , "FAIL" , "2.2.2.2" ,
                    "deviceCat" , "srcUsername" , "catObject" , "destAddress" , "appProtocol" } );
        }

        Thread.sleep( 1000 * 200 );
        //Shutting down the runtime
        executionPlanRuntime.shutdown();

        //Shutting down Siddhi
        siddhiManager.shutdown();

    }
}
