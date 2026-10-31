package br.dev.matheushenrique.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitService {

    public void baixarRepositorio(String repositorio, String token, String diretorio, String branch) throws IOException, InterruptedException {

        Path destino = Path.of(diretorio);

        Files.createDirectories(destino);

        if (Files.list(destino).findAny().isPresent()) {
            throw new IOException(
                    "O diretório de destino não está vazio: " + destino
            );
        }

        Path askPass = Files.createTempFile(
                "floccus-git-",
                ".sh"
        );

        try {

            Files.writeString(
                    askPass,
                    """
                    #!/bin/sh
    
                    case "$1" in
                        *Username*)
                            echo "x-access-token"
                            ;;
                        *Password*)
                            echo "$GIT_TOKEN"
                            ;;
                        *)
                            echo "$GIT_TOKEN"
                            ;;
                    esac
                    """
            );

            askPass.toFile().setExecutable(true);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "git",
                    "clone",
                    "--branch",
                    branch,
                    repositorio,
                    diretorio
            );

            processBuilder.environment().put(
                    "GIT_ASKPASS",
                    askPass.toAbsolutePath().toString()
            );

            processBuilder.environment().put(
                    "GIT_TOKEN",
                    token
            );

            processBuilder.environment().put(
                    "GIT_TERMINAL_PROMPT",
                    "0"
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream()
                                    )
                            )
            ) {
                String linha;

                while ((linha = reader.readLine()) != null) {
                    output.append(linha).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException(
                        "Erro ao clonar repositório Git: "
                                + output
                );
            }

        } finally {
            Files.deleteIfExists(askPass);
        }
    }
}