package ar.edu.itba.pod.tpe1.client.events.actions;

import ar.edu.itba.pod.grpc.EventResponse;
import ar.edu.itba.pod.grpc.EventsServiceGrpc;
import ar.edu.itba.pod.tpe1.client.Action;
import ar.edu.itba.pod.tpe1.client.events.EventsClient;
import ar.edu.itba.pod.tpe1.client.events.EventsClientArguments;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jdk.jfr.Event;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class RegisterAction implements Action {
    ManagedChannel channel;
    EventsClientArguments arguments;

    public RegisterAction(ManagedChannel channel, EventsClientArguments arguments) {
        this.channel = channel;
        this.arguments = arguments;
    }

    public void execute() {
        if (arguments.getAirline().isPresent()) {
            try {
                register(channel, arguments.getAirline().get());
            } catch (Exception e) {
                handleRegistrationError(e);
            }
        } else {
            printRegistrationUsageInstructions();
        }
    }


    private void handleRegistrationError(Exception e) {
        System.out.println("Failed to register due to an error: " + e.getMessage());
        printRegistrationUsageInstructions();
    }

    private void printRegistrationUsageInstructions() {
        System.out.println("- ERROR - Invalid or missing parameters for registering airline.");
        System.out.println("- register - Required parameters: -Dairline=<airlineName>");
    }
    private void register(ManagedChannel channel, String airlineName) {
        EventsServiceGrpc.EventsServiceStub asyncStub = EventsServiceGrpc.newStub(channel);
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<EventResponse> responseObserver = new StreamObserver<EventResponse>() {
            @Override
            public void onNext(EventResponse response) {
                if (response.getStatus().getCode() == Status.OK.getCode().value()) {
                    System.out.println(response.getMessage());
                } else {
                    System.out.println(response.getStatus().getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println(t.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StringValue request = StringValue.newBuilder().setValue(airlineName).build();
        asyncStub.register(request, responseObserver);

        try {
            finishLatch.await();
            channel.shutdownNow();
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Channel did not terminate within the allowed time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted while waiting for channel to terminate");
        } finally {
            if (!channel.isShutdown()) {
                channel.shutdownNow();
            }
        }

    }
}
