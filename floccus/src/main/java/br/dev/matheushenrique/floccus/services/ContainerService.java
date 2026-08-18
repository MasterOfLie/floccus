package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.ContainerDTO;
import io.grpc.StatusRuntimeException;

public interface ContainerService {

    ContainerDTO iniciarContainer (String containerID) throws StatusRuntimeException;
    ContainerDTO pararContainer (String containerID) throws StatusRuntimeException;
    ContainerDTO reiniciarContainer (String containerID) throws StatusRuntimeException;

}
