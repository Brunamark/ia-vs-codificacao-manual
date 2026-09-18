package kata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContadorCalorias {

    public List<String> diasAcimaDaMeta(List<String> registros) {
        Map<String, Integer> totais = new HashMap<>();
        Set<String> processados = new HashSet<>();

        for (String registro : registros) {

            // Ignora registros duplicados
            if (!processados.add(registro)) {
                continue;
            }

            String[] campos = registro.split(";");

            String dia = campos[0];
            String tipo = campos[1];
            int valor = Integer.parseInt(campos[2]);
            int quantidade = Integer.parseInt(campos[3]);

            // Ignora registros inválidos
            if (quantidade <= 0 || valor < 0) {
                continue;
            }

            int calorias = valor * quantidade;

            // Treino desconta calorias
            if (tipo.equals("treino")) {
                calorias = -calorias;
            }

            // Soma ao total daquele dia
            if (totais.containsKey(dia)) {
                totais.put(dia, totais.get(dia) + calorias);
            } else {
                totais.put(dia, calorias);
            }
        }

        List<String> resultado = new ArrayList<>();

        for (Map.Entry<String, Integer> entrada : totais.entrySet()) {
            if (entrada.getValue() > 2000) {
                resultado.add(entrada.getKey() + ";" + entrada.getValue());
            }
        }

        resultado.sort((a, b) -> {
            String diaA = a.split(";")[0];
            String diaB = b.split(";")[0];
            return diaA.compareTo(diaB);
        });

        return resultado;
    }
}
