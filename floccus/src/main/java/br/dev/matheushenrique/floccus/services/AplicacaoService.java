package br.dev.matheushenrique.floccus.services;

import br.dev.matheushenrique.floccus.dto.AplicacaoDTO;
import br.dev.matheushenrique.floccus.dto.ContainerDTO;
import br.dev.matheushenrique.floccus.dto.CriarAplicacaoDTO;
import io.grpc.StatusRuntimeException;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AplicacaoService {

    List<AplicacaoDTO> listarAplicacoes (Authentication authentication);
    ContainerDTO iniciarContainer (String containerID) throws StatusRuntimeException;
    ContainerDTO pararContainer (String containerID) throws StatusRuntimeException;
    ContainerDTO reiniciarContainer (String containerID) throws StatusRuntimeException;
    AplicacaoDTO detalharContainer (String containerID) throws StatusRuntimeException;
    AplicacaoDTO criarAplicacao(CriarAplicacaoDTO aplicacaoDTO, Authentication authentication);
    void iniciarDeploy(String aplicacaoId, Authentication authentication, boolean redeploy);
    void redeployWebhook(String aplicacaoId, boolean redeploy);
    AplicacaoDTO atualizaAplicacao (String aplicacaoId, AplicacaoDTO aplicacaoDTO, Authentication authentication);
    void removerAplicacao (String aplicacaoId, Authentication authentication);

}
