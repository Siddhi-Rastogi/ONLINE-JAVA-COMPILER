import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Compile extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Compile.class.getName());
    private static final String DEFAULT_JAVAC_COMMAND = "javac";
    private static final String UTF_8 = "UTF-8";

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(UTF_8);
        response.setCharacterEncoding(UTF_8);
        response.setContentType("text/plain; charset=UTF-8");

        PrintWriter writer = response.getWriter();
        String className = trimToEmpty(request.getParameter("className"));
        String sourceCode = request.getParameter("code");

        if (!isValidClassName(className)) {
            writer.println("Please enter a valid Java class name.");
            return;
        }

        if (sourceCode == null || sourceCode.trim().length() == 0) {
            writer.println("Please enter Java source code before compiling.");
            return;
        }

        String securityMessage = validateSourceCode(sourceCode);
        if (securityMessage != null) {
            writer.println(securityMessage);
            return;
        }

        File applicationRoot = new File(getServletContext().getRealPath("/"));
        File sourceDirectory = new File(applicationRoot, "Files");
        File classesDirectory = new File(sourceDirectory, "classes");

        if (!ensureDirectory(sourceDirectory) || !ensureDirectory(classesDirectory)) {
            writer.println("Unable to prepare compiler folders on the server.");
            return;
        }

        File sourceFile = new File(sourceDirectory, className + ".java");
        writeSourceFile(sourceFile, sourceCode);

        ProcessResult result = runProcess(new String[] {
            getCompilerCommand(),
            "-encoding",
            UTF_8,
            "-d",
            classesDirectory.getAbsolutePath(),
            sourceFile.getAbsolutePath()
        });

        if (result.exitCode == 0) {
            writer.println("Compiled Successfully");
            if (result.output.length() > 0) {
                writer.print(result.output);
            }
        } else {
            writer.print(nonEmpty(result.error, result.output,
                    "Compilation failed with exit code " + result.exitCode + "."));
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().println("Compile requests must use POST.");
    }

    private String getCompilerCommand() {
        String configuredCommand = getInitParameter("javacCommand");
        if (configuredCommand == null || configuredCommand.trim().length() == 0) {
            return DEFAULT_JAVAC_COMMAND;
        }
        return configuredCommand.trim();
    }

    private static boolean ensureDirectory(File directory) {
        return directory.exists() || directory.mkdirs();
    }

    private static void writeSourceFile(File sourceFile, String sourceCode) throws IOException {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(sourceFile), UTF_8);
            writer.write(sourceCode);
        } finally {
            closeQuietly(writer);
        }
    }

    private ProcessResult runProcess(String[] command) throws IOException {
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            process = builder.start();

            StreamCollector outputCollector = new StreamCollector(process.getInputStream());
            StreamCollector errorCollector = new StreamCollector(process.getErrorStream());
            outputCollector.start();
            errorCollector.start();

            int exitCode = process.waitFor();
            outputCollector.join();
            errorCollector.join();

            return new ProcessResult(exitCode, outputCollector.getContent(), errorCollector.getContent());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Compilation was interrupted.", ex);
            return new ProcessResult(1, "", "Compilation was interrupted. Please try again.");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to start javac process.", ex);
            throw ex;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static boolean isValidClassName(String className) {
        return className.matches("[A-Za-z_$][A-Za-z0-9_$]*");
    }

    private static String validateSourceCode(String sourceCode) {
        String compactCode = sourceCode.replaceAll("\\s+", "");

        if (compactCode.indexOf("Runtime.getRuntime(") >= 0) {
            return "Use of Runtime.getRuntime() is not allowed.";
        }
        if (sourceCode.indexOf("ProcessBuilder") >= 0) {
            return "Use of ProcessBuilder is not allowed.";
        }
        if (compactCode.indexOf("System.exit(") >= 0) {
            return "Use of System.exit() is not allowed.";
        }
        if (compactCode.indexOf(".delete(") >= 0 || sourceCode.indexOf("deleteOnExit") >= 0
                || sourceCode.indexOf("Files.delete") >= 0) {
            return "File deletion code is not allowed.";
        }
        if (sourceCode.indexOf("java.net") >= 0 || sourceCode.indexOf("Socket") >= 0
                || sourceCode.indexOf("ServerSocket") >= 0 || sourceCode.indexOf("URLConnection") >= 0
                || sourceCode.indexOf("HttpURLConnection") >= 0) {
            return "Network operations are not allowed.";
        }

        return null;
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String nonEmpty(String first, String second, String fallback) {
        if (first != null && first.trim().length() > 0) {
            return first;
        }
        if (second != null && second.trim().length() > 0) {
            return second;
        }
        return fallback;
    }

    private static void closeQuietly(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Unable to close file writer.", ex);
            }
        }
    }

    private static class StreamCollector extends Thread {
        private final InputStream inputStream;
        private final StringBuffer content = new StringBuffer();

        StreamCollector(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Unable to read process output.", ex);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Unable to close process output reader.", ex);
                    }
                }
            }
        }

        String getContent() {
            return content.toString();
        }
    }

    private static class ProcessResult {
        final int exitCode;
        final String output;
        final String error;

        ProcessResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }
    }
}
