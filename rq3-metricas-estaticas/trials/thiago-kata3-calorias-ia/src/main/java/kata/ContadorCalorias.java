package kata;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ContadorCalorias {

    private static final int META = 2000;

    public List<String> diasAcimaDaMeta(List<String> registros) {
        Set<String> linhasVistas = new LinkedHashSet<>();
        Map<String, Integer> totalPorDia = new TreeMap<>();

        for (String linha : registros) {
            if (!linhasVistas.add(linha)) {
                continue; // duplicata exata, ignora
            }

            String[] campos = linha.split(";");
            String dia = campos[0];
            String tipo = campos[1];
            int valor = Integer.parseInt(campos[2]);
            int quantidade = Integer.parseInt(campos[3]);

            if (quantidade <= 0 || valor < 0) {
                continue; // registro inválido, descarta
            }

            int calorias = valor * quantidade;
            if (tipo.equals("treino")) {
                calorias = -calorias;
            }

            totalPorDia.merge(dia, calorias, Integer::sum);
        }

        List<String> resultado = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : totalPorDia.entrySet()) {
            if (entry.getValue() > META) {
                resultado.add(entry.getKey() + ";" + entry.getValue());
            }
        }
        return resultado;
    }
}