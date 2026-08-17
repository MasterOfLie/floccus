package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.RedeDTO;
import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import io.grpc.StatusRuntimeException;

public interface NetworkService {

    RedeDTO criarRede (RedeDTO redeDTO) throws StatusRuntimeException;
    ResponseDTO excluirRede(RedeDTO redeDTO);

}
