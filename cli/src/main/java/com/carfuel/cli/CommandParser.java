package com.carfuel.cli;

import java.util.HashMap;
import java.util.Map;

public class CommandParser {
    
    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        
        if (args.length == 0) {
            return params;
        }
        
        String command = args[0];
        params.put("command", command);
        
        // Parse key-value pairs (--key value format)
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2); // Remove "--"
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    params.put(key, args[i + 1]);
                    i++; // Skip the value in next iteration
                } else {
                    params.put(key, "");
                }
            }
        }
        
        return params;
    }
    
    public static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  create-car --brand <brand> --model <model> --year <year>");
        System.out.println("  add-fuel --carId <id> --liters <liters> --price <price> --odometer <odometer>");
        System.out.println("  fuel-stats --carId <id>");
    }
}
