package io.github.betterclient.shaguess;

import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSPromise;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLInputElement;

import java.util.Random;

public class Main {
    private static int last = 0;
    private static int pointsCurrent = 0;

    public static void main(String[] args) {
        HTMLDocument document = HTMLDocument.current();

        startGame();

        document.getElementById("guess").addEventListener("click", evt -> {
            restartGame();
        });
    }

    private static void restartGame() {
        HTMLDocument document = HTMLDocument.current();
        int c = Integer.parseInt(((HTMLInputElement)document.getElementById("text")).getValue());

        int points = calculatePoints(c, last);
        document.getElementById("guessinfo").setInnerText(
                "Input: " + c + "\n" +
                        "Target: " + last + "\n" +
                        "Points: " + points + "/8\n" +
                        "Total: " + (pointsCurrent+=points)
        );

        startGame();
    }

    private static int calculatePoints(int p1, int p2) {
        String str1 = String.valueOf(Math.abs(p1));
        String str2 = String.valueOf(Math.abs(p2));

        str1 = String.format("%08d", Integer.parseInt(str1));
        str2 = String.format("%08d", Integer.parseInt(str2));

        int count = 0;
        for (int i = 0; i < 8; i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    public static void startGame() {
        String targetHash = generateHash();
        last = Integer.parseInt(targetHash);
        HTMLDocument document = HTMLDocument.current();
        hash(targetHash).then(s -> {
            document.getElementById("target").setInnerText(s.toString());

            return null;
        });
    }

    private static String generateHash() {
        Random r = new Random();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            s.append(r.nextInt(10));
        }
        return s.toString();
    }

    @JSBody(params = "to", script =
            """
            return (async function() {
                const encoder = new TextEncoder();
                const data = encoder.encode(to);
                const hashBuffer = await crypto.subtle.digest('SHA-256', data);
                const hashArray = Array.from(new Uint8Array(hashBuffer));
                const hashHex = hashArray.map(byte => byte.toString(16).padStart(2, '0')).join('');
                return hashHex;
            })();
            """)
    private static native JSPromise<Object> hash(String to);
}
