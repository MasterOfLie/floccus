package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.RedeDTO;
import br.dev.matheushenrique.floccus.dto.ResponseDTO;
import io.grpc.StatusRuntimeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;


import java.util.List;

public interface RedeService {

    RedeDTO criarRede (RedeDTO redeDTO, Authentication authentication ) throws StatusRuntimeException;
    void excluirRede(String redeId, Authentication authentication);
    Page<RedeDTO> listarRedes(Pageable pageable, Authentication authentication);
    List<RedeDTO> listarRedes(Authentication authentication);
}
