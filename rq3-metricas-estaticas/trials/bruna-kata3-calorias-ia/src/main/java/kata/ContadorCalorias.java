package kata;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ContadorCalorias {

    private static final int META_DIARIA = 2000;

    public List<String> diasAcimaDaMeta(List<String> registros) {
        // Remove duplicatas exatas de linha, mantendo cada registro único
        LinkedHashSet<String> unicos = new LinkedHashSet<>(registros);

        // Usa TreeMap para já manter os dias em ordem cronológica crescente
        Map<String, Integer> totaisPorDia = new TreeMap<>();

        for (String registro : unicos) {
            String[] campos = registro.split(";");
            String dia = campos[0];
            String tipo = campos[1];
            int valor = Integer.parseInt(campos[2]);
            int quantidade = Integer.parseInt(campos[3]);

            // Descarta registros inválidos
            if (quantidade <= 0 || valor < 0) {
                continue;
            }

            int subtotal = valor * quantidade;

            int atual = totaisPorDia.getOrDefault(dia, 0);

            if (tipo.equals("refeicao")) {
                atual += subtotal;
            } else if (tipo.equals("treino")) {
                atual -= subtotal;
            }

            totaisPorDia.put(dia, atual);
        }

        List<String> resultado = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : totaisPorDia.entrySet()) {
            if (entry.getValue() > META_DIARIA) {
                resultado.add(entry.getKey() + ";" + entry.getValue());
            }
        }

        return resultado;
    }
}