package org.commonprovenanceframework.cpm.template.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GraphvizChecker {
    private static Boolean isGraphvizInstalled;

    private static boolean checkGraphvizInstallation() {
        try {
            Process process = new ProcessBuilder("dot", "-V").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("dot - graphviz version")) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static boolean isGraphvizInstalled() {
        if (isGraphvizInstalled == null) {
            isGraphvizInstalled = checkGraphvizInstallation();
        }
        return isGraphvizInstalled;
    }
}