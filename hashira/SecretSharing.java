import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.json.JSONObject;

public class SecretSharing {

    static class Share {
        int x;
        BigInteger y;
        Share(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Lagrange interpolation at x = 0 (constant term)
    static BigInteger lagrangeAtZero(List<Share> shares, int k) {
        BigInteger secret = BigInteger.ZERO;
        for (int i = 0; i < k; i++) {
            BigInteger term = shares.get(i).y;
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    term = term.multiply(BigInteger.valueOf(0 - shares.get(j).x))
                               .divide(BigInteger.valueOf(shares.get(i).x - shares.get(j).x));
                }
            }
            secret = secret.add(term);
        }
        return secret;
    }

    public static void main(String[] args) throws Exception {
        // Read JSON file
        String jsonStr = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("input2.json")));
        JSONObject obj = new JSONObject(jsonStr);

        int n = obj.getJSONObject("keys").getInt("n");
        int k = obj.getJSONObject("keys").getInt("k");

        List<Share> shares = new ArrayList<>();
        for (String key : obj.keySet()) {
            if (key.equals("keys")) continue;
            int x = Integer.parseInt(key);
            JSONObject point = obj.getJSONObject(key);
            int base = Integer.parseInt(point.getString("base"));
            String valueStr = point.getString("value");
            BigInteger y = new BigInteger(valueStr, base); // convert to decimal
            shares.add(new Share(x, y));
        }

        // Take first k shares to reconstruct
        BigInteger secret = lagrangeAtZero(shares, k);
        System.out.println("Secret (c) = " + secret);

        // Validation step
        System.out.println("Checking validity of all shares...");
        for (Share s : shares) {
            BigInteger expected = BigInteger.ZERO;
            for (int i = 0; i < k; i++) {
                BigInteger term = shares.get(i).y;
                for (int j = 0; j < k; j++) {
                    if (i != j) {
                        term = term.multiply(BigInteger.valueOf(s.x - shares.get(j).x))
                                   .divide(BigInteger.valueOf(shares.get(i).x - shares.get(j).x));
                    }
                }
                expected = expected.add(term);
            }
            if (!expected.equals(s.y)) {
                System.out.println("Invalid share at x=" + s.x + " with y=" + s.y);
            }
        }
    }
}
