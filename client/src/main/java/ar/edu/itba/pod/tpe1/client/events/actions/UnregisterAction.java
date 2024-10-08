package ar.edu.itba.pod.tpe1.client.events.actions;

import ar.edu.itba.pod.grpc.EventResponse;
import ar.edu.itba.pod.grpc.EventsServiceGrpc;
import ar.edu.itba.pod.tpe1.client.Action;
import ar.edu.itba.pod.tpe1.client.events.EventsClient;
import ar.edu.itba.pod.tpe1.client.events.EventsClientArguments;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnregisterAction implements Action {
    ManagedChannel channel;
    EventsClientArguments arguments;


    public UnregisterAction(ManagedChannel channel, EventsClientArguments arguments) {
        this.channel = channel;
        this.arguments = arguments;
    }

    @Override
    public void execute() {
        if (arguments.getAirline().isPresent()) {
            try {
                unregister(channel, arguments.getAirline().get());
            } catch (Exception e) {
                handleError(e);
            }
        } else {
            printRegistrationUsageInstructions();
        }
    }

    private void handleError(Exception e) {
        System.out.println("Failed to unregister due to an error: " + e.getMessage());
        printRegistrationUsageInstructions();
    }

    private void printRegistrationUsageInstructions() {
        System.out.println("- ERROR - Invalid or missing parameters for unregistering airline.");
        System.out.println("- unregister - Required parameters: -Dairline=<airlineName>");
    }

    private void unregister(ManagedChannel channel, String airlineName) {
        EventsServiceGrpc.EventsServiceBlockingStub stub = EventsServiceGrpc.newBlockingStub(channel);
        EventResponse serverResponse = stub.unregister(StringValue.of(airlineName));

        if (serverResponse.getStatus().getCode() == Status.OK.getCode().value()) {
            System.out.println(serverResponse.getMessage());
        } else {
            System.out.println(serverResponse.getStatus().getMessage());
        }

    }
}
