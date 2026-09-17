package kata;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Kata 3 — Contador de Calorias (SOLUÇÃO DE REFERÊNCIA).
 *
 * <p>Este arquivo NÃO faz parte do esqueleto entregue ao participante: ele existe apenas para
 * o check_katas.py validar que os testes de aceitação são satisfazíveis. Não abra esta pasta
 * durante um trial.
 */
public class ContadorCalorias {

    private static final int META_DIARIA = 2000;

    public List<String> diasAcimaDaMeta(List<String> registros) {
        // TreeMap mantém os dias em ordem cronológica: o formato AAAA-MM-DD ordena como texto.
        Map<String, Integer> totalPorDia = new TreeMap<>();

        // LinkedHashSet descarta linhas exatamente iguais (envio duplicado).
        for (String registro : new LinkedHashSet<>(registros)) {
            acumular(totalPorDia, registro);
        }

        List<String> resultado = new ArrayList<>();
        for (Map.Entry<String, Integer> dia : totalPorDia.entrySet()) {
            if (dia.getValue() > META_DIARIA) {
                resultado.add(dia.getKey() + ";" + dia.getValue());
            }
        }
        return resultado;
    }

    private void acumular(Map<String, Integer> totalPorDia, String registro) {
        String[] campos = registro.split(";");
        String dia = campos[0];
        String tipo = campos[1];
        int valor = Integer.parseInt(campos[2]);
        int quantidade = Integer.parseInt(campos[3]);

        if (quantidade <= 0 || valor < 0) {
            return;
        }

        int calorias = valor * quantidade;
        int delta = tipo.equals("treino") ? -calorias : calorias;
        totalPorDia.merge(dia, delta, Integer::sum);
    }
}
