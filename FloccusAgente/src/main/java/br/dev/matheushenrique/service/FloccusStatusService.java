package br.dev.matheushenrique.service;
import br.dev.matheushenrique.grpc.FloccusAgenteStatusGrpc;
import br.dev.matheushenrique.grpc.FloccusStatusInt;
import br.dev.matheushenrique.grpc.FloccusStatusOut;
import io.grpc.stub.StreamObserver;

public class FloccusStatusService extends FloccusAgenteStatusGrpc.FloccusAgenteStatusImplBase{

    @Override
    public void floccusStatus(FloccusStatusInt request, StreamObserver<FloccusStatusOut> responseObserver){
        FloccusStatusOut response = FloccusStatusOut.newBuilder()
                .setChave("Tudo Certo aqui")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
