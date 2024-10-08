package ar.edu.itba.pod.tpe1.client.counter.actions;

import ar.edu.itba.pod.grpc.AssignCountersRequest;
import ar.edu.itba.pod.grpc.AssignCountersResponse;
import ar.edu.itba.pod.grpc.CounterServiceGrpc;
import ar.edu.itba.pod.tpe1.client.Action;
import ar.edu.itba.pod.tpe1.client.counter.CounterClientArguments;
import io.grpc.ManagedChannel;
import io.grpc.Status;

import java.util.List;
import java.util.Optional;

public class AssignCountersAction implements Action {
    ManagedChannel channel;
    CounterClientArguments arguments;

    public AssignCountersAction(ManagedChannel channel, CounterClientArguments arguments) {
        this.channel = channel;
        this.arguments = arguments;
    }

    @Override
    public void execute() {
        if(arguments.getSector().isPresent() && arguments.getFlights().isPresent() && arguments.getAirline().isPresent() && arguments.getCounterCount().isPresent()){
            try{
                assignCounters(channel, arguments.getSector().get(), arguments.getFlights().get(), arguments.getAirline().get(), arguments.getCounterCount().get());
            }
            catch (Exception e){
                handleAssignCountersError(e);
            }
        }
        else {
            printAssignCountersUsageInstructions();
        }
    }

    private void handleAssignCountersError(Exception e) {
        System.out.println("Failed to assign counters due to an error: " + e.getMessage());
        printAssignCountersUsageInstructions();
    }

    private void printAssignCountersUsageInstructions() {
        System.out.println("- ERROR - Invalid or missing parameters.");
        System.out.println("- assignCounters - Required parameters: -Dsector=<sector> -Dflights=<flights> -Dairline=<airline> -DcounterCount=<count>");
    }

    private void assignCounters(ManagedChannel channel, String sector, List<String> flights, String airline, Integer counterCount) {
        CounterServiceGrpc.CounterServiceBlockingStub stub = CounterServiceGrpc.newBlockingStub(channel);
        Optional<AssignCountersResponse> response = Optional.ofNullable(stub.assignCounters(
            AssignCountersRequest.newBuilder()
                    .setSectorName(sector)
                    .addAllFlightCodes(flights)
                    .setAirlineName(airline)
                    .setCounterCount(counterCount)
                    .build()));

        response.ifPresentOrElse(
            this::handleResponse,
            () -> System.out.println("Failed to assign counters")
        );
    }

    private void handleResponse(AssignCountersResponse response) {
        if (response.getStatus().getCode() == Status.OK.getCode().value()) {
            if(response.hasPendingAssignments()){
                System.out.printf("%d counters in Sector %s is pending with %d other pendings ahead.\n",
                        response.getCounterCount(),
                        response.getSectorName(),
                        response.getPendingAssignments());
            }
            else {
                System.out.printf("%d counters (%d-%d) in Sector %s are now checking passengers for airline %s %s flights.\n",
                        response.getCounterCount(),
                        response.getCounterFrom(),
                        response.getCounterTo(),
                        response.getSectorName(),
                        response.getAirlineName(),
                        String.join("|", response.getFlightCodesList())
                );
            }
        }
        else {
            System.out.println(response.getStatus().getMessage());
        }
    }
}
